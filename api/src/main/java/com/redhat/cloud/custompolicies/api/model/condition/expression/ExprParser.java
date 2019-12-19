package com.redhat.cloud.custompolicies.api.model.condition.expression;

import com.redhat.cloud.custompolicies.api.model.condition.expression.parser.ExpressionBaseListener;
import com.redhat.cloud.custompolicies.api.model.condition.expression.parser.ExpressionLexer;
import com.redhat.cloud.custompolicies.api.model.condition.expression.parser.ExpressionParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.BitSet;

public class ExprParser extends ExpressionBaseListener implements ANTLRErrorListener {
    boolean error = false;
    String errorMsg = null;

    public void validate(String expression) {
        parse(expression);
    }

    public void parse(String expression) {
        CharStream cs = CharStreams.fromString(expression);
        ExpressionLexer lexer = new ExpressionLexer(cs);
        lexer.removeErrorListeners();
        lexer.addErrorListener(this);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ExpressionParser parser = new ExpressionParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(this);
        ParseTree parseTree = parser.expression();
        ParseTreeWalker.DEFAULT.walk(this, parseTree);

        if(error) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int line, int charPos, String msg, RecognitionException e) {
        error = true;
        errorMsg = String.format("Error: %s at line %d position %d\n", msg, line, charPos);
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
        System.out.println("reportAmbiguity");
        error = true;
    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {
        System.out.println("reportAttemptingFullContext");
        error = true;
    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {
        System.out.println("reportContextSensitivity");
        error = true;
    }
}
