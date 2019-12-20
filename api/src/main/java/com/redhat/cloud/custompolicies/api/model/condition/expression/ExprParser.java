package com.redhat.cloud.custompolicies.api.model.condition.expression;

import com.redhat.cloud.custompolicies.api.model.condition.expression.parser.ExpressionBaseVisitor;
import com.redhat.cloud.custompolicies.api.model.condition.expression.parser.ExpressionLexer;
import com.redhat.cloud.custompolicies.api.model.condition.expression.parser.ExpressionParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;

import java.math.BigDecimal;
import java.util.BitSet;
import java.util.Map;

public class ExprParser extends ExpressionBaseVisitor<Boolean> {

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

    public static void validate(String expression) {
        ThrowingErrorHandler errorListener = new ThrowingErrorHandler();
        createParserTree(expression, errorListener);
    }

    public static boolean evaluate(Map<String, String> facts, String expression) {
        ThrowingErrorHandler errorListener = new ThrowingErrorHandler();
        ParseTree parseTree = createParserTree(expression, errorListener);
        ExprVisitor visitor = new ExprVisitor(facts);
        return visitor.visit(parseTree);
    }

    private static class ThrowingErrorHandler implements ANTLRErrorListener {

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object o, int line, int charPos, String msg, RecognitionException e) {
            String errorMsg = String.format("Error: %s at line %d position %d\n", msg, line, charPos);
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

    // ExpressionBaseVisitor

    static class ExprVisitor extends ExpressionBaseVisitor<Boolean> {
        private Map<String, String> facts;

        ExprVisitor(Map<String, String> facts) {
            this.facts = facts;
        }

        @Override
        public Boolean visitExpression(ExpressionParser.ExpressionContext ctx) {
            if(ctx.object() != null) {
                return visitObject(ctx.object());
            }
            return super.visitExpression(ctx);
        }

        @Override
        public Boolean visitObject(ExpressionParser.ObjectContext ctx) {
            System.out.println("visitObject called");
            if(ctx.expr() != null) {
                return visitExpr(ctx.expr());
            }

            if(ctx.logical_operator() != null) {
                System.out.println("VISIT ctx.logical_operator()");
                ExpressionParser.Logical_operatorContext op = ctx.logical_operator();
                ExpressionParser.ObjectContext left = ctx.object(0);
                ExpressionParser.ObjectContext right = ctx.object(1);

                if(op.AND() != null) {
                    return visitObject(left) && visitObject(right);
                } else if (op.OR() != null) {
                    return visitObject(left) || visitObject(right);
                }

                return false;
            }
            return false;
        }

        /**
         * @param ctx Expr from left or right
         * @return Return boolean if the comparison was successful
         */
        @Override
        public Boolean visitExpr(ExpressionParser.ExprContext ctx) {
            System.out.println("visitExpr called");
            String key = null;
            String strValue = null;
            BigDecimal decimalValue = null;

            ExpressionParser.ValueContext value = ctx.value();

            // This is the factKey
            if(ctx.key() != null) {
                key = ctx.key().SIMPLETEXT().getSymbol().getText();
            } else {
                return false;
            }

            final String targetValueStr = facts.get(key);
            if(targetValueStr == null) {
                return false;
            }

            BigDecimal targetValueDecimal = null;

            // Fact comparison value
            try {
                if(value != null) {
                    if(value.STRING() != null) {
                        // This is String value
                        strValue = value.STRING().getSymbol().getText();
                        strValue = strValue.replaceAll("^(['\"])(.*)\\1$", "$2");
                    } else if(value.SIMPLETEXT() != null) {
                        strValue = value.SIMPLETEXT().getSymbol().getText();
                        strValue = strValue.replaceAll("^(['\"])(.*)\\1$", "$2");
                    } else if(value.numerical_value() != null) {
                        if(value.numerical_value().FLOAT() != null) {
                            decimalValue = new BigDecimal(Double.parseDouble(value.numerical_value().FLOAT().getSymbol().getText()));
                        } else if(value.numerical_value().INTEGER() != null) {
                            decimalValue = new BigDecimal(Long.parseLong(value.numerical_value().INTEGER().getSymbol().getText()));
                        }
                        targetValueDecimal = new BigDecimal(targetValueStr);
                    }
                } else {
                    return false;
                }
            } catch(NumberFormatException e) {
                return false;
            }

            // Equality checks
            if(ctx.boolean_operator() != null) {
                final ExpressionParser.Boolean_operatorContext op = ctx.boolean_operator();
                boolean compareResult = false;
                if(decimalValue != null) {
                    compareResult = decimalValue.compareTo(targetValueDecimal) == 0;
                } else if(strValue != null) {
                    compareResult = targetValueStr.equals(strValue);
                }

                if(op.EQUAL() != null) {
                    return compareResult;
                } else if(op.NOTEQUAL() != null) {
                    return !compareResult;
                }

                return false;
            }

            // These need to be integers or doubles
            if(ctx.compare_operator() != null) {
                ExpressionParser.Compare_operatorContext op = ctx.compare_operator();
                if(op.GT() != null) {
                    return decimalValue.compareTo(targetValueDecimal) > 0;
                } else if(op.GTE() != null) {
                    return decimalValue.compareTo(targetValueDecimal) >= 0;
                } else if(op.LT() != null) {
                    return decimalValue.compareTo(targetValueDecimal) < 0;
                } else if(op.LTE() != null) {
                    return decimalValue.compareTo(targetValueDecimal) <= 0;
                }

                return false;
            }

            return false;
        }
    }
}
