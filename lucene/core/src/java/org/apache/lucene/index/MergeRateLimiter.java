/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.index;


import org.apache.lucene.store.RateLimiter;
import org.apache.lucene.util.ThreadInterruptedException;

import static org.apache.lucene.store.RateLimiter.SimpleRateLimiter;

import org.apache.lucene.index.MergePolicy.MergeProgress;

/** This is the {@link RateLimiter} that {@link IndexWriter} assigns to each running merge, to 
 *  give {@link MergeScheduler}s ionice like control.
 *
 *  This is similar to {@link SimpleRateLimiter}, except it's merge-private,
 *  it will wake up if its rate changes while it's paused, it tracks how
 *  much time it spent stopped and paused, and it supports aborting.
 *
 *  @lucene.internal */

public class MergeRateLimiter extends RateLimiter {

  private final static int MIN_PAUSE_CHECK_MSEC = 25;

  private volatile double mbPerSec;
  private volatile long minPauseCheckBytes;

  private long lastNS;

  // Not volatile on purpose.
  private long totalBytesWritten;
  private long totalPausedNS;
  private long totalStoppedNS;

  private final MergeProgress mergeProgress;

  /** Returned by {@link #maybePause}. */
  private static enum PauseResult {NO, STOPPED, PAUSED};

  /** Sole constructor. */
  public MergeRateLimiter(MergeProgress mergeProgress) {
    // Initially no IO limit; use setter here so minPauseCheckBytes is set:
    this.mergeProgress = mergeProgress;
    setMBPerSec(Double.POSITIVE_INFINITY);
  }

  @Override
  public void setMBPerSec(double mbPerSec) {
    // Synchronized to make updates to mbPerSec and minPauseCheckBytes atomic. 
    synchronized (this) {
      // 0.0 is allowed: it means the merge is paused
      if (mbPerSec < 0.0) {
        throw new IllegalArgumentException("mbPerSec must be positive; got: " + mbPerSec);
      }
      this.mbPerSec = mbPerSec;
  
      // NOTE: Double.POSITIVE_INFINITY casts to Long.MAX_VALUE
      this.minPauseCheckBytes = Math.min(1024*1024, (long) ((MIN_PAUSE_CHECK_MSEC / 1000.0) * mbPerSec * 1024 * 1024));
      assert minPauseCheckBytes >= 0;
    }

    mergeProgress.wakeup();
  }

  @Override
  public double getMBPerSec() {
    return mbPerSec;
  }

  /** Returns total bytes written by this merge. */
  public long getTotalBytesWritten() {
    return totalBytesWritten;
  }

  @Override
  public long pause(long bytes) throws MergePolicy.MergeAbortedException {
    totalBytesWritten += bytes;

    long startNS = System.nanoTime();
    long curNS = startNS;

    // While loop because we may wake up and check again when our rate limit
    // is changed while we were pausing:
    long pausedNS = 0;
    while (true) {
      PauseResult result = maybePause(bytes, curNS);
      if (result == PauseResult.NO) {
        break;
      }
      curNS = System.nanoTime();
      long ns = curNS - startNS;
      startNS = curNS;

      // Separately track when merge was stopped vs rate limited:
      if (result == PauseResult.STOPPED) {
        totalStoppedNS += ns;
      } else {
        assert result == PauseResult.PAUSED;
        totalPausedNS += ns;
      }
      pausedNS += ns;
    }

    return pausedNS;
  }

  /** Total NS merge was stopped. */
  public long getTotalStoppedNS() {
    return totalStoppedNS;
  } 

  /** Total NS merge was paused to rate limit IO. */
  public long getTotalPausedNS() {
    return totalPausedNS;
  } 

  /** Returns NO if no pause happened, STOPPED if pause because rate was 0.0 (merge is stopped), PAUSED if paused with a normal rate limit. */
  private PauseResult maybePause(long bytes, long curNS) throws MergePolicy.MergeAbortedException {
    // Now is a good time to abort the merge:
    if (mergeProgress.isAborted()) {
      throw new MergePolicy.MergeAbortedException("Merge aborted.");
    }

    double rate = mbPerSec; // read from volatile rate once.
    double secondsToPause = (bytes/1024./1024.) / rate;

    // Time we should sleep until; this is purely instantaneous
    // rate (just adds seconds onto the last time we had paused to);
    // maybe we should also offer decayed recent history one?
    long targetNS = lastNS + (long) (1000000000 * secondsToPause);

    long curPauseNS = targetNS - curNS;

    // NOTE: except maybe on real-time JVMs, minimum realistic
    // wait/sleep time is 1 msec; if you pass just 1 nsec the impl
    // rounds up to 1 msec, so we don't bother unless it's > 2 msec:

    if (curPauseNS <= 2000000) {
      // Set to curNS, not targetNS, to enforce the instant rate, not
      // the "averaged over all history" rate:
      lastNS = curNS;
      return PauseResult.NO;
    }

    // Defensive: sleep for at most 250 msec; the loop above will call us again if we should keep sleeping:
    if (curPauseNS > 250L*1000000) {
      curPauseNS = 250L*1000000;
    }

    try {
      mergeProgress.pauseNanos(curPauseNS);
    } catch (InterruptedException ie) {
      throw new ThreadInterruptedException(ie);
    }

    if (rate == 0.0) {
      return PauseResult.STOPPED;
    } else {
      return PauseResult.PAUSED;
    }
  }

  @Override
  public long getMinPauseCheckBytes() {
    return minPauseCheckBytes;
  }
}
