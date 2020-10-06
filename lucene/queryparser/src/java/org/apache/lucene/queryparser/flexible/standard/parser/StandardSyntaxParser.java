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
package org.apache.lucene.queryparser.flexible.standard.parser;

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

import static org.apache.lucene.queryparser.flexible.standard.parser.EscapeQuerySyntaxImpl.discardEscapeChar;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.lucene.queryparser.charstream.CharStream;
import org.apache.lucene.queryparser.charstream.FastCharStream;
import org.apache.lucene.queryparser.flexible.core.QueryNodeParseException;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.nodes.AndQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.BoostQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QuotedFieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.SlopQueryNode;
import org.apache.lucene.queryparser.flexible.core.parser.SyntaxParser;
import org.apache.lucene.queryparser.flexible.messages.Message;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.nodes.RegexpQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;

/** Parser for the standard Lucene syntax */
public class StandardSyntaxParser implements SyntaxParser, StandardSyntaxParserConstants {
  public StandardSyntaxParser() {
    this(new FastCharStream(Reader.nullReader()));
  }

  /**
   * Parses a query string, returning a {@link
   * org.apache.lucene.queryparser.flexible.core.nodes.QueryNode}.
   *
   * @param query the query string to be parsed.
   * @throws ParseException if the parsing fails
   */
  public QueryNode parse(CharSequence query, CharSequence field) throws QueryNodeParseException {
    ReInit(new FastCharStream(new StringReader(query.toString())));
    try {
      return TopLevelQuery(field);
    } catch (ParseException tme) {
      tme.setQuery(query);
      throw tme;
    } catch (Error tme) {
      Message message =
          new MessageImpl(QueryParserMessages.INVALID_SYNTAX_CANNOT_PARSE, query, tme.getMessage());
      QueryNodeParseException e = new QueryNodeParseException(tme);
      e.setQuery(query);
      e.setNonLocalizedMessage(message);
      throw e;
    }
  }

  public final QueryNode TopLevelQuery(CharSequence field) throws ParseException {
    QueryNode q;
    q = Query(field);
    jj_consume_token(0);
    {
      if ("" != null) return q;
    }
    throw new Error("Missing return statement in function");
  }

  private final QueryNode Query(CharSequence field) throws ParseException {
    ArrayList<QueryNode> clauses = new ArrayList<>();
    QueryNode node;
    label_1:
    while (true) {
      node = DisjQuery(field);
      clauses.add(node);
      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case NOT:
        case PLUS:
        case MINUS:
        case LPAREN:
        case QUOTED:
        case NUMBER:
        case TERM:
        case REGEXPTERM:
        case RANGEIN_START:
        case RANGEEX_START:
          {
            ;
            break;
          }
        default:
          jj_la1[0] = jj_gen;
          break label_1;
      }
    }
    // Handle the case of a "pure" negation query which
    // needs to be wrapped as a boolean query, otherwise
    // the returned result drops the negation.
    if (clauses.size() == 1) {
      QueryNode first = clauses.get(0);
      if (first instanceof ModifierQueryNode
          && ((ModifierQueryNode) first).getModifier() == ModifierQueryNode.Modifier.MOD_NOT) {
        clauses.set(0, new BooleanQueryNode(Collections.singletonList(first)));
      }
    }

    {
      if ("" != null) return clauses.size() == 1 ? clauses.get(0) : new BooleanQueryNode(clauses);
    }
    throw new Error("Missing return statement in function");
  }

  private final QueryNode DisjQuery(CharSequence field) throws ParseException {
    ArrayList<QueryNode> clauses = new ArrayList<>();
    QueryNode node;
    node = ConjQuery(field);
    clauses.add(node);
    label_2:
    while (true) {
      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case OR:
          {
            ;
            break;
          }
        default:
          jj_la1[1] = jj_gen;
          break label_2;
      }
      jj_consume_token(OR);
      node = ConjQuery(field);
      clauses.add(node);
    }
    {
      if ("" != null) return clauses.size() == 1 ? clauses.get(0) : new OrQueryNode(clauses);
    }
    throw new Error("Missing return statement in function");
  }

  private final QueryNode ConjQuery(CharSequence field) throws ParseException {
    ArrayList<QueryNode> clauses = new ArrayList<>();
    QueryNode node;
    node = ModClause(field);
    clauses.add(node);
    label_3:
    while (true) {
      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case AND:
          {
            ;
            break;
          }
        default:
          jj_la1[2] = jj_gen;
          break label_3;
      }
      jj_consume_token(AND);
      node = ModClause(field);
      clauses.add(node);
    }
    {
      if ("" != null) return clauses.size() == 1 ? clauses.get(0) : new AndQueryNode(clauses);
    }
    throw new Error("Missing return statement in function");
  }

  private final QueryNode ModClause(CharSequence field) throws ParseException {
    QueryNode q;
    ModifierQueryNode.Modifier modifier = ModifierQueryNode.Modifier.MOD_NONE;
    switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
      case NOT:
      case PLUS:
      case MINUS:
        {
          switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case PLUS:
              {
                jj_consume_token(PLUS);
                modifier = ModifierQueryNode.Modifier.MOD_REQ;
                break;
              }
            case NOT:
            case MINUS:
              {
                switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                  case MINUS:
                    {
                      jj_consume_token(MINUS);
                      break;
                    }
                  case NOT:
                    {
                      jj_consume_token(NOT);
                      break;
                    }
                  default:
                    jj_la1[3] = jj_gen;
                    jj_consume_token(-1);
                    throw new ParseException();
                }
                modifier = ModifierQueryNode.Modifier.MOD_NOT;
                break;
              }
            default:
              jj_la1[4] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
          }
          break;
        }
      default:
        jj_la1[5] = jj_gen;
        ;
    }
    q = Clause(field);
    if (modifier != ModifierQueryNode.Modifier.MOD_NONE) {
      q = new ModifierQueryNode(q, modifier);
    }
    {
      if ("" != null) return q;
    }
    throw new Error("Missing return statement in function");
  }

  private final QueryNode Clause(CharSequence field) throws ParseException {
    QueryNode q;
    if (jj_2_2(2)) {
      q = FieldRangeExpr(field);
    } else {
      switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
        case LPAREN:
        case QUOTED:
        case NUMBER:
        case TERM:
        case REGEXPTERM:
        case RANGEIN_START:
        case RANGEEX_START:
          {
            if (jj_2_1(2)) {
              field = FieldName();
              switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
                case OP_COLON:
                  {
                    jj_consume_token(OP_COLON);
                    break;
                  }
                case OP_EQUAL:
                  {
                    jj_consume_token(OP_EQUAL);
                    break;
                  }
                default:
                  jj_la1[6] = jj_gen;
                  jj_consume_token(-1);
                  throw new ParseException();
              }
            } else {
              ;
            }
            switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
              case QUOTED:
              case NUMBER:
              case TERM:
              case REGEXPTERM:
              case RANGEIN_START:
              case RANGEEX_START:
                {
                  q = Term(field);
                  break;
                }
              case LPAREN:
                {
                  q = GroupingExpr(field);
                  break;
                }
              default:
                jj_la1[7] = jj_gen;
                jj_consume_token(-1);
                throw new ParseException();
            }
            break;
          }
        default:
          jj_la1[8] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
      }
    }
    {
      if ("" != null) return q;
    }
    throw new Error("Missing return statement in function");
  }

  private final CharSequence FieldName() throws ParseException {
    Token name;
    name = jj_consume_token(TERM);
    {
      if ("" != null) return discardEscapeChar(name.image);
    }
    throw new Error("Missing return statement in function");
  }

  private final GroupQueryNode GroupingExpr(CharSequence field) throws ParseException {
    QueryNode q;
    Token boost;
    jj_consume_token(LPAREN);
    q = Query(field);
    jj_consume_token(RPAREN);
    switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
      case CARAT:
        {
          q = Boost(q);
          break;
        }
      default:
        jj_la1[9] = jj_gen;
        ;
    }
    {
      if ("" != null) return new GroupQueryNode(q);
    }
    throw new Error("Missing return statement in function");
  }

  private final QueryNode Boost(QueryNode node) throws ParseException {
    Token boost;
    jj_consume_token(CARAT);
    boost = jj_consume_token(NUMBER);
    {
      if ("" != null)
        return node == null ? node : new BoostQueryNode(node, Float.parseFloat(boost.image));
    }
    throw new Error("Missing return statement in function");
  }

  private final QueryNode FuzzyOp(CharSequence field, Token term, QueryNode node)
      throws ParseException {
    Token similarity = null;
    jj_consume_token(TILDE);
    if (jj_2_3(2)) {
      similarity = jj_consume_token(NUMBER);
    } else {
      ;
    }
    float fms = org.apache.lucene.search.FuzzyQuery.defaultMaxEdits;
    if (similarity != null) {
      fms = Float.parseFloat(similarity.image);
      if (fms < 0.0f) {
        {
          if (true)
            throw new ParseException(
                new MessageImpl(QueryParserMessages.INVALID_SYNTAX_FUZZY_LIMITS));
        }
      } else if (fms >= 1.0f && fms != (int) fms) {
        {
          if (true)
            throw new ParseException(
                new MessageImpl(QueryParserMessages.INVALID_SYNTAX_FUZZY_EDITS));
        }
      }
    }
    {
      if ("" != null)
        return new FuzzyQueryNode(
            field, discardEscapeChar(term.image), fms, term.beginColumn, term.endColumn);
    }
    throw new Error("Missing return statement in function");
  }

  private final TermRangeQueryNode FieldRangeExpr(CharSequence field) throws ParseException {
    Token operator, term;
    FieldQueryNode qLower, qUpper;
    boolean lowerInclusive, upperInclusive;
    field = FieldName();
    switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
      case OP_LESSTHAN:
        {
          jj_consume_token(OP_LESSTHAN);
          break;
        }
      case OP_LESSTHANEQ:
        {
          jj_consume_token(OP_LESSTHANEQ);
          break;
        }
      case OP_MORETHAN:
        {
          jj_consume_token(OP_MORETHAN);
          break;
        }
      case OP_MORETHANEQ:
        {
          jj_consume_token(OP_MORETHANEQ);
          break;
        }
      default:
        jj_la1[10] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
    }
    operator = token;
    switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
      case TERM:
        {
          jj_consume_token(TERM);
          break;
        }
      case QUOTED:
        {
          jj_consume_token(QUOTED);
          break;
        }
      case NUMBER:
        {
          jj_consume_token(NUMBER);
          break;
        }
      default:
        jj_la1[11] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
    }
    term = token;
    if (term.kind == QUOTED) {
      term.image = term.image.substring(1, term.image.length() - 1);
    }
    switch (operator.kind) {
      case OP_LESSTHAN:
        lowerInclusive = true;
        upperInclusive = false;
        qLower = new FieldQueryNode(field, "*", term.beginColumn, term.endColumn);
        qUpper =
            new FieldQueryNode(
                field, discardEscapeChar(term.image), term.beginColumn, term.endColumn);
        break;
      case OP_LESSTHANEQ:
        lowerInclusive = true;
        upperInclusive = true;
        qLower = new FieldQueryNode(field, "*", term.beginColumn, term.endColumn);
        qUpper =
            new FieldQueryNode(
                field, discardEscapeChar(term.image), term.beginColumn, term.endColumn);
        break;
      case OP_MORETHAN:
        lowerInclusive = false;
        upperInclusive = true;
        qLower =
            new FieldQueryNode(
                field, discardEscapeChar(term.image), term.beginColumn, term.endColumn);
        qUpper = new FieldQueryNode(field, "*", term.beginColumn, term.endColumn);
        break;
      case OP_MORETHANEQ:
        lowerInclusive = true;
        upperInclusive = true;
        qLower =
            new FieldQueryNode(
                field, discardEscapeChar(term.image), term.beginColumn, term.endColumn);
        qUpper = new FieldQueryNode(field, "*", term.beginColumn, term.endColumn);
        break;
      default:
        {
          if (true) throw new Error("Unhandled case, operator=" + operator);
        }
    }
    {
      if ("" != null) return new TermRangeQueryNode(qLower, qUpper, lowerInclusive, upperInclusive);
    }
    throw new Error("Missing return statement in function");
  }

  private final QueryNode Term(CharSequence field) throws ParseException {
    QueryNode q;
    Token term, fuzzySlop = null;
    switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
      case REGEXPTERM:
        {
          term = jj_consume_token(REGEXPTERM);
          q = new RegexpQueryNode(field, term.image.substring(1, term.image.length() - 1));
          break;
        }
      case NUMBER:
      case TERM:
        {
          switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case TERM:
              {
                term = jj_consume_token(TERM);
                break;
              }
            case NUMBER:
              {
                term = jj_consume_token(NUMBER);
                break;
              }
            default:
              jj_la1[12] = jj_gen;
              jj_consume_token(-1);
              throw new ParseException();
          }
          q =
              new FieldQueryNode(
                  field, discardEscapeChar(term.image), term.beginColumn, term.endColumn);
          switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
            case TILDE:
              {
                q = FuzzyOp(field, term, q);
                break;
              }
            default:
              jj_la1[13] = jj_gen;
              ;
          }
          break;
        }
      case RANGEIN_START:
      case RANGEEX_START:
        {
          q = TermRangeExpr(field);
          break;
        }
      case QUOTED:
        {
          q = QuotedTerm(field);
          break;
        }
      default:
        jj_la1[14] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
    }
    switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
      case CARAT:
        {
          q = Boost(q);
          break;
        }
      default:
        jj_la1[15] = jj_gen;
        ;
    }
    {
      if ("" != null) return q;
    }
    throw new Error("Missing return statement in function");
  }

  private final QueryNode QuotedTerm(CharSequence field) throws ParseException {
    QueryNode q;
    Token term, slop;
    term = jj_consume_token(QUOTED);
    String image = term.image.substring(1, term.image.length() - 1);
    q =
        new QuotedFieldQueryNode(
            field, discardEscapeChar(image), term.beginColumn + 1, term.endColumn - 1);
    switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
      case TILDE:
        {
          jj_consume_token(TILDE);
          slop = jj_consume_token(NUMBER);
          q = new SlopQueryNode(q, (int) Float.parseFloat(slop.image));
          break;
        }
      default:
        jj_la1[16] = jj_gen;
        ;
    }
    {
      if ("" != null) return q;
    }
    throw new Error("Missing return statement in function");
  }

  private final TermRangeQueryNode TermRangeExpr(CharSequence field) throws ParseException {
    Token left, right;
    boolean leftInclusive = false;
    boolean rightInclusive = false;
    switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
      case RANGEIN_START:
        {
          jj_consume_token(RANGEIN_START);
          leftInclusive = true;
          break;
        }
      case RANGEEX_START:
        {
          jj_consume_token(RANGEEX_START);
          break;
        }
      default:
        jj_la1[17] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
    }
    switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
      case RANGE_GOOP:
        {
          jj_consume_token(RANGE_GOOP);
          break;
        }
      case RANGE_QUOTED:
        {
          jj_consume_token(RANGE_QUOTED);
          break;
        }
      case RANGE_TO:
        {
          jj_consume_token(RANGE_TO);
          break;
        }
      default:
        jj_la1[18] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
    }
    left = token;
    jj_consume_token(RANGE_TO);
    switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
      case RANGE_GOOP:
        {
          jj_consume_token(RANGE_GOOP);
          break;
        }
      case RANGE_QUOTED:
        {
          jj_consume_token(RANGE_QUOTED);
          break;
        }
      case RANGE_TO:
        {
          jj_consume_token(RANGE_TO);
          break;
        }
      default:
        jj_la1[19] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
    }
    right = token;
    switch ((jj_ntk == -1) ? jj_ntk_f() : jj_ntk) {
      case RANGEIN_END:
        {
          jj_consume_token(RANGEIN_END);
          rightInclusive = true;
          break;
        }
      case RANGEEX_END:
        {
          jj_consume_token(RANGEEX_END);
          break;
        }
      default:
        jj_la1[20] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
    }
    if (left.kind == RANGE_QUOTED) {
      left.image = left.image.substring(1, left.image.length() - 1);
    }
    if (right.kind == RANGE_QUOTED) {
      right.image = right.image.substring(1, right.image.length() - 1);
    }

    FieldQueryNode qLower =
        new FieldQueryNode(field, discardEscapeChar(left.image), left.beginColumn, left.endColumn);
    FieldQueryNode qUpper =
        new FieldQueryNode(
            field, discardEscapeChar(right.image), right.beginColumn, right.endColumn);

    {
      if ("" != null) return new TermRangeQueryNode(qLower, qUpper, leftInclusive, rightInclusive);
    }
    throw new Error("Missing return statement in function");
  }

  private boolean jj_2_1(int xla) {
    jj_la = xla;
    jj_lastpos = jj_scanpos = token;
    try {
      return (!jj_3_1());
    } catch (LookaheadSuccess ls) {
      return true;
    } finally {
      jj_save(0, xla);
    }
  }

  private boolean jj_2_2(int xla) {
    jj_la = xla;
    jj_lastpos = jj_scanpos = token;
    try {
      return (!jj_3_2());
    } catch (LookaheadSuccess ls) {
      return true;
    } finally {
      jj_save(1, xla);
    }
  }

  private boolean jj_2_3(int xla) {
    jj_la = xla;
    jj_lastpos = jj_scanpos = token;
    try {
      return (!jj_3_3());
    } catch (LookaheadSuccess ls) {
      return true;
    } finally {
      jj_save(2, xla);
    }
  }

  private boolean jj_3R_4() {
    if (jj_scan_token(TERM)) return true;
    return false;
  }

  private boolean jj_3_2() {
    if (jj_3R_5()) return true;
    return false;
  }

  private boolean jj_3R_5() {
    if (jj_3R_4()) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(17)) {
      jj_scanpos = xsp;
      if (jj_scan_token(18)) {
        jj_scanpos = xsp;
        if (jj_scan_token(19)) {
          jj_scanpos = xsp;
          if (jj_scan_token(20)) return true;
        }
      }
    }
    return false;
  }

  private boolean jj_3_3() {
    if (jj_scan_token(NUMBER)) return true;
    return false;
  }

  private boolean jj_3_1() {
    if (jj_3R_4()) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(15)) {
      jj_scanpos = xsp;
      if (jj_scan_token(16)) return true;
    }
    return false;
  }

  /** Generated Token Manager. */
  public StandardSyntaxParserTokenManager token_source;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;

  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  private final int[] jj_la1 = new int[21];
  private static int[] jj_la1_0;
  private static int[] jj_la1_1;

  static {
    jj_la1_init_0();
    jj_la1_init_1();
  }

  private static void jj_la1_init_0() {
    jj_la1_0 =
        new int[] {
          0x1f803c00,
          0x200,
          0x100,
          0x1400,
          0x1c00,
          0x1c00,
          0x18000,
          0x1f802000,
          0x1f802000,
          0x200000,
          0x1e0000,
          0x3800000,
          0x3000000,
          0x400000,
          0x1f800000,
          0x200000,
          0x400000,
          0x18000000,
          0x20000000,
          0x20000000,
          0xc0000000,
        };
  }

  private static void jj_la1_init_1() {
    jj_la1_1 =
        new int[] {
          0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
          0x3, 0x3, 0x0,
        };
  }

  private final JJCalls[] jj_2_rtns = new JJCalls[3];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with user supplied CharStream. */
  public StandardSyntaxParser(CharStream stream) {
    token_source = new StandardSyntaxParserTokenManager(stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 21; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(CharStream stream) {
    token_source.ReInit(stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 21; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public StandardSyntaxParser(StandardSyntaxParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 21; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(StandardSyntaxParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 21; i++) jj_la1[i] = -1;
    for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      if (++jj_gc > 100) {
        jj_gc = 0;
        for (int i = 0; i < jj_2_rtns.length; i++) {
          JJCalls c = jj_2_rtns[i];
          while (c != null) {
            if (c.gen < jj_gen) c.first = null;
            c = c.next;
          }
        }
      }
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  @SuppressWarnings("serial")
  private static final class LookaheadSuccess extends java.lang.Error {}

  private final LookaheadSuccess jj_ls = new LookaheadSuccess();

  private boolean jj_scan_token(int kind) {
    if (jj_scanpos == jj_lastpos) {
      jj_la--;
      if (jj_scanpos.next == null) {
        jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
      } else {
        jj_lastpos = jj_scanpos = jj_scanpos.next;
      }
    } else {
      jj_scanpos = jj_scanpos.next;
    }
    if (jj_rescan) {
      int i = 0;
      Token tok = token;
      while (tok != null && tok != jj_scanpos) {
        i++;
        tok = tok.next;
      }
      if (tok != null) jj_add_error_token(kind, i);
    }
    if (jj_scanpos.kind != kind) return true;
    if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
    return false;
  }

  /** Get the next Token. */
  public final Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  /** Get the specific Token. */
  public final Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk_f() {
    if ((jj_nt = token.next) == null)
      return (jj_ntk = (token.next = token_source.getNextToken()).kind);
    else return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
    if (pos >= 100) {
      return;
    }

    if (pos == jj_endpos + 1) {
      jj_lasttokens[jj_endpos++] = kind;
    } else if (jj_endpos != 0) {
      jj_expentry = new int[jj_endpos];

      for (int i = 0; i < jj_endpos; i++) {
        jj_expentry[i] = jj_lasttokens[i];
      }

      for (int[] oldentry : jj_expentries) {
        if (oldentry.length == jj_expentry.length) {
          boolean isMatched = true;

          for (int i = 0; i < jj_expentry.length; i++) {
            if (oldentry[i] != jj_expentry[i]) {
              isMatched = false;
              break;
            }
          }
          if (isMatched) {
            jj_expentries.add(jj_expentry);
            break;
          }
        }
      }

      if (pos != 0) {
        jj_lasttokens[(jj_endpos = pos) - 1] = kind;
      }
    }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[34];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 21; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1 << j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1 << j)) != 0) {
            la1tokens[32 + j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 34; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    jj_endpos = 0;
    jj_rescan_token();
    jj_add_error_token(0, 0);
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  private int trace_indent = 0;
  private boolean trace_enabled;

  /** Trace enabled. */
  public final boolean trace_enabled() {
    return trace_enabled;
  }

  /** Enable tracing. */
  public final void enable_tracing() {}

  /** Disable tracing. */
  public final void disable_tracing() {}

  private void jj_rescan_token() {
    jj_rescan = true;
    for (int i = 0; i < 3; i++) {
      try {
        JJCalls p = jj_2_rtns[i];

        do {
          if (p.gen > jj_gen) {
            jj_la = p.arg;
            jj_lastpos = jj_scanpos = p.first;
            switch (i) {
              case 0:
                jj_3_1();
                break;
              case 1:
                jj_3_2();
                break;
              case 2:
                jj_3_3();
                break;
            }
          }
          p = p.next;
        } while (p != null);

      } catch (LookaheadSuccess ls) {
      }
    }
    jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
    JJCalls p = jj_2_rtns[index];
    while (p.gen > jj_gen) {
      if (p.next == null) {
        p = p.next = new JJCalls();
        break;
      }
      p = p.next;
    }

    p.gen = jj_gen + xla - jj_la;
    p.first = token;
    p.arg = xla;
  }

  static final class JJCalls {
    int gen;
    Token first;
    int arg;
    JJCalls next;
  }
}
