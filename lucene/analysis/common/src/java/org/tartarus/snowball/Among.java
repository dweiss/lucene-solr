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
package org.tartarus.snowball;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Locale;

/** Internal class used by Snowball stemmers */
public class Among {
  public Among(String s, int substring_i, int result) {
    this.s = s.toCharArray();
    this.substring_i = substring_i;
    this.result = result;
    this.method = null;
  }

  public Among(
      String s, int substring_i, int result, String methodname, MethodHandles.Lookup methodobject) {
    this.s = s.toCharArray();
    this.substring_i = substring_i;
    this.result = result;
    final Class<? extends SnowballProgram> clazz =
        methodobject.lookupClass().asSubclass(SnowballProgram.class);
    if (methodname.length() > 0) {
      try {
        this.method =
            methodobject
                .findVirtual(clazz, methodname, MethodType.methodType(boolean.class))
                .asType(MethodType.methodType(boolean.class, SnowballProgram.class));
      } catch (NoSuchMethodException | IllegalAccessException e) {
        throw new RuntimeException(
            String.format(
                Locale.ENGLISH,
                "Snowball program '%s' is broken, cannot access method: boolean %s()",
                clazz.getSimpleName(),
                methodname),
            e);
      }
    } else {
      this.method = null;
    }
  }

  final char[] s; /* search string */
  final int substring_i; /* index to longest matching substring */
  final int result; /* result of the lookup */

  // Make sure this is not accessible outside package for Java security reasons!
  final MethodHandle method; /* method to use if substring matches */
}
;
