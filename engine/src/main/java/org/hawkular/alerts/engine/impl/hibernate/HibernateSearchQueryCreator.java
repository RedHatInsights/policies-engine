package org.hawkular.alerts.engine.impl.hibernate;

import com.redhat.cloud.policies.api.model.condition.expression.ExprParser;
import com.redhat.cloud.policies.api.model.condition.expression.parser.ExpressionBaseVisitor;
import com.redhat.cloud.policies.api.model.condition.expression.parser.ExpressionLexer;
import com.redhat.cloud.policies.api.model.condition.expression.parser.ExpressionParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.*;

import java.util.BitSet;

import static com.redhat.cloud.policies.api.model.condition.expression.ExprParser.valueToString;

public class HibernateSearchQueryCreator extends ExpressionBaseVisitor<Query> {
    static ParseTree createParserTree(String expression, ANTLRErrorListener errorListener) {
        CharStream cs = CharStreams.fromString(expression);
        ExpressionLexer lexer = new ExpressionLexer(cs);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExpressionParser parser = new ExpressionParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return parser.expression();
    }

    public static Query evaluate(QueryBuilder queryBuilder, String expression) {
        ThrowingErrorHandler errorListener = new ThrowingErrorHandler();
        ParseTree parseTree = createParserTree(expression, errorListener);
        QueryVisitor visitor = new QueryVisitor(queryBuilder);
        return visitor.visit(parseTree);
    }

    private static class ThrowingErrorHandler implements ANTLRErrorListener {

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object o, int line, int charPos, String msg, RecognitionException e) {
            String errorMsg = String.format("Invalid query: %s at line %d position %d", msg, line, charPos);
            throw new IllegalArgumentException(errorMsg);
        }

        @Override
        public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {

        }

        @Override
        public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

        }

        @Override
        public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

        }
    }

    static class QueryVisitor extends ExpressionBaseVisitor<Query> {
        QueryBuilder builder;

        public QueryVisitor(QueryBuilder builder) {
            this.builder = builder;
        }

        @Override
        public Query visitExpression(ExpressionParser.ExpressionContext ctx) {
            if (ctx.object() != null) {
                return visitObject(ctx.object());
            }
            return super.visitExpression(ctx);
        }

        @Override
        public Query visitObject(ExpressionParser.ObjectContext ctx) {
            // Brackets when necessary?

            if (ctx.expr() != null) {
                if (ctx.negative_expr() != null) {
                    // Needs negation in the Query
                    return builder.bool().must(visitExpr(ctx.expr())).not().createQuery();
                }
                return visitExpr(ctx.expr());
            }

            if (ctx.logical_operator() != null) {
                ExpressionParser.Logical_operatorContext op = ctx.logical_operator();
                ExpressionParser.ObjectContext left = ctx.object(0);
                ExpressionParser.ObjectContext right = ctx.object(1);

                if (op.AND() != null) {
                    // Needs AND keyword
                    return builder.bool().must(visitObject(left)).must(visitObject(right)).createQuery();
                } else if (op.OR() != null) {
                    // Is there OR keyword?
                    return builder.bool().should(visitObject(left)).should(visitObject(right)).createQuery();
                }
            }

            if (ctx.object() != null && ctx.object().size() == 1) {
                if (ctx.negative_expr() != null) {
                    return builder.bool().must(visitObject(ctx.object(0))).not().createQuery();
                }
                return visitObject(ctx.object(0));
            }
            return super.visitObject(ctx);
        }

        @Override
        public Query visitExpr(ExpressionParser.ExprContext ctx) {
            ExpressionParser.ValueContext value = ctx.value();

            String field = null;

            // This is the tagKey
            if (ctx.key() != null) {
                field = ctx.key().SIMPLETEXT().getSymbol().getText();
            } else {
                return null;
            }

            // Insert query value
            String strValue = null;
            TerminalNode number = null;
            if (value != null) {
                strValue = valueToString(value);
                if (value.NUMBER() != null) {
                    number = value.NUMBER();
                }
            } else if (ctx.numerical_value() != null) {
                number = ctx.numerical_value().NUMBER();
            }

            if (number != null) {
                strValue = number.getSymbol().getText();
            }

            // Process operators here
            if (ctx.boolean_operator() != null) {
                final ExpressionParser.Boolean_operatorContext op = ctx.boolean_operator();
                if (op.EQUAL() != null) {
                    return builder.keyword().onField(field).ignoreFieldBridge().matching(strValue).createQuery();
                } else if (op.NOTEQUAL() != null) {
                    return builder.bool().must(builder.keyword().onField(field).ignoreFieldBridge().matching(strValue).createQuery()).not().createQuery();
                }
            }

            if(ctx.array_operator() != null) {
                if (ctx.array() != null) {
                    if(ctx.array_operator().IN() != null) {
                        BooleanJunction<BooleanJunction> bool = builder.bool();
                        for (ExpressionParser.ValueContext valueContext : ctx.array().value()) {
                            String val = valueToString(valueContext);
                            bool = bool.should(builder.keyword().onField(field).ignoreFieldBridge().matching(val).createQuery());
                        }
                        return bool.createQuery();
                    }
                }
            }

            if(ctx.string_compare_operator() != null) {
                ExpressionParser.String_compare_operatorContext op = ctx.string_compare_operator();
                if (op.MATCHES() != null) {
                    return builder.keyword().wildcard().onField(field).ignoreFieldBridge().matching(strValue).createQuery();
                }
            }

            // In must be transformed to multiple or

            return builder.keyword().onField(field).matching("true").createQuery();
        }
    }
}
