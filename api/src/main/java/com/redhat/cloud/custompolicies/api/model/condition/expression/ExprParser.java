package com.redhat.cloud.custompolicies.api.model.condition.expression;

import com.redhat.cloud.custompolicies.api.model.condition.expression.parser.ExpressionBaseVisitor;
import com.redhat.cloud.custompolicies.api.model.condition.expression.parser.ExpressionLexer;
import com.redhat.cloud.custompolicies.api.model.condition.expression.parser.ExpressionParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.BitSet;
import java.util.Map;

public class ExprParser extends ExpressionBaseVisitor<Boolean> {
    private String expression;
    ThrowingErrorHandler errorListener;

    public ExprParser(Map<String, String> facts, String expression) {
        this.expression = expression;
        errorListener = new ThrowingErrorHandler();
    }

    static ExpressionParser createParser(String expression, ANTLRErrorListener errorListener) {
        CharStream cs = CharStreams.fromString(expression);
        ExpressionLexer lexer = new ExpressionLexer(cs);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExpressionParser parser = new ExpressionParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return parser;
    }

    public static void validate(String expression) {
        ThrowingErrorHandler errorListener = new ThrowingErrorHandler();
        ExpressionParser parser = createParser(expression, errorListener);
        parser.expression();
    }

    public boolean evaluate() {
        ExpressionParser parser = createParser(expression, errorListener);
        ParseTree parseTree = parser.expression();
        Boolean visit = this.visit(parseTree);
        return visit;
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

    @Override
    public Boolean visitObject(ExpressionParser.ObjectContext ctx) {
        if(ctx.expr() != null) {
            System.out.println("VISIT ctx.expr()");
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

            }

            return visitObject(left);
        }
        return super.visitObject(ctx);
    }

    /**
     * @param ctx Expr from left or right
     * @return Return boolean if the comparison was successful
     */
    @Override
    public Boolean visitExpr(ExpressionParser.ExprContext ctx) {
        System.out.println("visitExpr");
        System.out.println(ctx.toString());
        String key = null;
        String strValue = null;
        Long longVal = null;
        Double doubleVal = null;

        ExpressionParser.ValueContext value = ctx.value();

        // This is the factKey
        if(ctx.key() != null) {
            key = ctx.key().SIMPLETEXT().getSymbol().getText();
        }

        // Fact comparison value
        if(value != null) {
            if(value.STRING() != null) {
                // This is String value
                strValue = value.STRING().getSymbol().getText();
            } else if(value.SIMPLETEXT() != null) {
                strValue = value.SIMPLETEXT().getSymbol().getText();
            } else if(value.numerical_value() != null) {
                if(value.numerical_value().FLOAT() != null) {
                    doubleVal = Double.valueOf(value.numerical_value().FLOAT().getSymbol().getText());
                } else if(value.numerical_value().INTEGER() != null) {
                    longVal = Long.valueOf(value.numerical_value().INTEGER().getSymbol().getText());
                }
            }
        }

        // Exact matches for Strings
        if(ctx.boolean_operator() != null) {
            final ExpressionParser.Boolean_operatorContext op = ctx.boolean_operator();
            if(op.EQUAL() != null) {
                System.out.println("Entering EQUAL comparison");
            } else if(op.NOTEQUAL() != null) {
                System.out.println("Entering NOTEQUAL comparison");
            }
        }

        // These need to be integers or doubles
        if(ctx.compare_operator() != null) {
            ExpressionParser.Compare_operatorContext op = ctx.compare_operator();
            if(op.GT() != null) {

            } else if(op.GTE() != null) {

            } else if(op.LT() != null) {

            } else if(op.LTE() != null) {

            }
        }

        System.out.printf("key: %s ; value: %s\n", key, value);

        return super.visitExpr(ctx);
    }
}
