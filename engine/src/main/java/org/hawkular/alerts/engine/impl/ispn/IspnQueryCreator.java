package org.hawkular.alerts.engine.impl.ispn;

import com.redhat.cloud.policies.api.model.condition.expression.ExprParser;
import com.redhat.cloud.policies.api.model.condition.expression.parser.ExpressionBaseVisitor;
import com.redhat.cloud.policies.api.model.condition.expression.parser.ExpressionLexer;
import com.redhat.cloud.policies.api.model.condition.expression.parser.ExpressionParser;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.infinispan.query.dsl.FilterConditionContext;
import org.infinispan.query.dsl.FilterConditionContextQueryBuilder;
import org.infinispan.query.dsl.FilterConditionEndContext;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryBuilder;
import org.infinispan.query.dsl.QueryFactory;

import java.util.BitSet;

public class IspnQueryCreator extends ExpressionBaseVisitor<Query> {
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

    public static Query evaluate(QueryFactory queryFactory, FilterConditionContextQueryBuilder queryBuilder, String expression) {
        ThrowingErrorHandler errorListener = new ThrowingErrorHandler();
        ParseTree parseTree = createParserTree(expression, errorListener);
        QueryVisitor visitor = new QueryVisitor(queryFactory, queryBuilder);
        FilterConditionContext visit = visitor.visit(parseTree);
        return queryBuilder.and(visit).build();
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

    static class QueryVisitor extends ExpressionBaseVisitor<FilterConditionContext> {
        QueryBuilder builder;
        QueryFactory queryFactory;

        public QueryVisitor(QueryFactory queryFactory, QueryBuilder builder) {
            this.builder = builder;
            this.queryFactory = queryFactory;
        }

        @Override
        public FilterConditionContext visitExpression(ExpressionParser.ExpressionContext ctx) {
            if (ctx.object() != null) {
                return visitObject(ctx.object());
            }
            return super.visitExpression(ctx);
        }

        @Override
        public FilterConditionContext visitObject(ExpressionParser.ObjectContext ctx) {
            // Brackets when necessary?

            if (ctx.expr() != null) {
                if (ctx.negative_expr() != null) {
                    // Needs negation in the Ickle
                    return builder.not(visitExpr(ctx.expr()));
                }
                return visitExpr(ctx.expr());
            }

            if (ctx.logical_operator() != null) {
                ExpressionParser.Logical_operatorContext op = ctx.logical_operator();
                ExpressionParser.ObjectContext left = ctx.object(0);
                ExpressionParser.ObjectContext right = ctx.object(1);

                if (op.AND() != null) {
                    // Needs AND keyword
                    return visitObject(left).and(visitObject(right));
                } else if (op.OR() != null) {
                    // Is there OR keyword?
                    return visitObject(left).or(visitObject(right));
                }
            }

            if (ctx.object() != null && ctx.object().size() == 1) {
                if (ctx.negative_expr() != null) {
                    return builder.not(visitObject(ctx.object(0)));
                }
                return visitObject(ctx.object(0));
            }
            return super.visitObject(ctx);
        }

        @Override
        public FilterConditionContext visitExpr(ExpressionParser.ExprContext ctx) {
            ExpressionParser.ValueContext value = ctx.value();

            FilterConditionEndContext endContext;

            // This is the tagKey
            if (ctx.key() != null) {
                endContext = queryFactory.having(ctx.key().SIMPLETEXT().getSymbol().getText());
            } else {
                return null;
            }

            // Process operators here
            /*
            if (ctx.boolean_operator() != null) {
                final ExpressionParser.Boolean_operatorContext op = ctx.boolean_operator();
                if (op.EQUAL() != null) {
                    builder.append("= ");
                } else if (op.NOTEQUAL() != null) {
                    builder.append("!= ");
                }
            }
            */

            // Insert query value
            String strValue = null;
            TerminalNode number = null;
            if (value != null) {
                strValue = ExprParser.valueToString(value);
                if (value.NUMBER() != null) {
                    number = value.NUMBER();
                }
            } else if (ctx.numerical_value() != null) {
                number = ctx.numerical_value().NUMBER();
            }

            if (number != null) {
                strValue = number.getSymbol().getText();
            }

            FilterConditionContextQueryBuilder like = endContext.like(strValue);
            return like;

//            return builder.toString();
        }
    }
}
