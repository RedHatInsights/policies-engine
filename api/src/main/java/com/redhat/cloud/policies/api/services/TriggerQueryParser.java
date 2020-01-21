package com.redhat.cloud.policies.api.services;

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

import java.util.BitSet;

/**
 * TriggerQueryParser is using the same ANTLR4 model as the ExprParser (Expression.g4), but we only use the
 * string parts of it. This class transforms the query to Ickle format for Infinispan.
 */
public class TriggerQueryParser extends ExpressionBaseVisitor<Boolean> {

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

    public static String transformToIckle(String expression) {
        ThrowingErrorHandler errorListener = new ThrowingErrorHandler();
        ParseTree parseTree = createParserTree(expression, errorListener);
        QueryVisitor visitor = new QueryVisitor();
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

    static class QueryVisitor extends ExpressionBaseVisitor<String> {

        private static String AND_OP = " && ";
        private static String OR_OP = " || ";
        private static String NEG_OP = "NOT ";

        @Override
        public String visitExpression(ExpressionParser.ExpressionContext ctx) {
            if(ctx.object() != null) {
                return visitObject(ctx.object());
            }
            return super.visitExpression(ctx);
        }

        @Override
        public String visitObject(ExpressionParser.ObjectContext ctx) {
            // Brackets when necessary?

            if(ctx.expr() != null) {
                if(ctx.negative_expr() != null) {
                    // Needs negation in the Ickle
                    return NEG_OP + visitExpr(ctx.expr());
                }
                return visitExpr(ctx.expr());
            }

            if(ctx.logical_operator() != null) {
                ExpressionParser.Logical_operatorContext op = ctx.logical_operator();
                ExpressionParser.ObjectContext left = ctx.object(0);
                ExpressionParser.ObjectContext right = ctx.object(1);

                if(op.AND() != null) {
                    // Needs AND keyword
                    return visitObject(left) + AND_OP + visitObject(right);
                } else if (op.OR() != null) {
                    // Is there OR keyword?
                    return visitObject(left) + OR_OP + visitObject(right);
                }
            }

            if(ctx.object() != null && ctx.object().size() == 1) {
                if(ctx.negative_expr() != null) {
                    return NEG_OP + visitObject(ctx.object(0));
                }
                return visitObject(ctx.object(0));
            }
            return "";
        }

        @Override
        public String visitExpr(ExpressionParser.ExprContext ctx) {
            StringBuilder builder = new StringBuilder();

            // TODO Test description = 'something long'
            // TODO We should allow "enabled AND" type of query, in this case evaluating it with boolean true (since it is always present)

            ExpressionParser.ValueContext value = ctx.value();

            // This is the tagKey
            if(ctx.key() != null) {
                builder.append(ctx.key().SIMPLETEXT().getSymbol().getText()).append(" ");
            } else {
                return "";
            }

            // Process operators here
            if(ctx.boolean_operator() != null) {
                final ExpressionParser.Boolean_operatorContext op = ctx.boolean_operator();
                if(op.EQUAL() != null) {
                    builder.append("= ");
                } else if(op.NOTEQUAL() != null) {
                    builder.append("!= ");
                }
            }

            // Insert query value
            TerminalNode number = null;
            if (value != null) {
                builder.append('\'').append(ExprParser.valueToString(value)).append('\'');
                if (value.NUMBER() != null) {
                    number = value.NUMBER();
                }
            } else if (ctx.numerical_value() != null) {
                number = ctx.numerical_value().NUMBER();
            }

            if (number != null) {
                builder.append(number.getSymbol().getText());
            }

            return builder.toString();
        }
    }

}
