// Generated from com/redhat/cloud/policies/engine/condition/Expression.g4 by ANTLR 4.10.1
package com.redhat.cloud.policies.engine.condition;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ExpressionParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, OR=6, AND=7, NOT=8, EQUAL=9, NOTEQUAL=10, 
		CONTAINS=11, MATCHES=12, NEG=13, GT=14, GTE=15, LT=16, LTE=17, IN=18, 
		QUOTED_NUMBER=19, NUMBER=20, FLOAT=21, INTEGER=22, SIMPLETEXT=23, STRING=24, 
		WS=25;
	public static final int
		RULE_expression = 0, RULE_object = 1, RULE_expr = 2, RULE_logical_operator = 3, 
		RULE_boolean_operator = 4, RULE_numeric_compare_operator = 5, RULE_string_compare_operator = 6, 
		RULE_array_operator = 7, RULE_array = 8, RULE_numerical_value = 9, RULE_value = 10, 
		RULE_negative_expr = 11, RULE_key = 12;
	private static String[] makeRuleNames() {
		return new String[] {
			"expression", "object", "expr", "logical_operator", "boolean_operator", 
			"numeric_compare_operator", "string_compare_operator", "array_operator", 
			"array", "numerical_value", "value", "negative_expr", "key"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'['", "','", "']'", null, null, null, "'='", "'!='", 
			null, null, null, "'>'", "'>='", "'<'", "'<='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, "OR", "AND", "NOT", "EQUAL", "NOTEQUAL", 
			"CONTAINS", "MATCHES", "NEG", "GT", "GTE", "LT", "LTE", "IN", "QUOTED_NUMBER", 
			"NUMBER", "FLOAT", "INTEGER", "SIMPLETEXT", "STRING", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Expression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ExpressionParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class ExpressionContext extends ParserRuleContext {
		public ObjectContext object() {
			return getRuleContext(ObjectContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ExpressionParser.EOF, 0); }
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(26);
			object(0);
			setState(27);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ObjectContext extends ParserRuleContext {
		public Negative_exprContext negative_expr() {
			return getRuleContext(Negative_exprContext.class,0);
		}
		public List<ObjectContext> object() {
			return getRuleContexts(ObjectContext.class);
		}
		public ObjectContext object(int i) {
			return getRuleContext(ObjectContext.class,i);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public Logical_operatorContext logical_operator() {
			return getRuleContext(Logical_operatorContext.class,0);
		}
		public ObjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_object; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterObject(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitObject(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitObject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectContext object() throws RecognitionException {
		return object(0);
	}

	private ObjectContext object(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ObjectContext _localctx = new ObjectContext(_ctx, _parentState);
		ObjectContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_object, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
			case NEG:
				{
				setState(30);
				negative_expr();
				setState(31);
				object(4);
				}
				break;
			case SIMPLETEXT:
			case STRING:
				{
				setState(33);
				expr();
				}
				break;
			case T__0:
				{
				setState(34);
				match(T__0);
				setState(35);
				object(0);
				setState(36);
				match(T__1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(46);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ObjectContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_object);
					setState(40);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(41);
					logical_operator();
					setState(42);
					object(2);
					}
					} 
				}
				setState(48);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public KeyContext key() {
			return getRuleContext(KeyContext.class,0);
		}
		public Boolean_operatorContext boolean_operator() {
			return getRuleContext(Boolean_operatorContext.class,0);
		}
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public Numeric_compare_operatorContext numeric_compare_operator() {
			return getRuleContext(Numeric_compare_operatorContext.class,0);
		}
		public Numerical_valueContext numerical_value() {
			return getRuleContext(Numerical_valueContext.class,0);
		}
		public String_compare_operatorContext string_compare_operator() {
			return getRuleContext(String_compare_operatorContext.class,0);
		}
		public ArrayContext array() {
			return getRuleContext(ArrayContext.class,0);
		}
		public Array_operatorContext array_operator() {
			return getRuleContext(Array_operatorContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_expr);
		try {
			setState(70);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(49);
				key();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(50);
				key();
				setState(51);
				boolean_operator();
				setState(52);
				value();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(54);
				key();
				setState(55);
				numeric_compare_operator();
				setState(56);
				numerical_value();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(58);
				key();
				setState(59);
				string_compare_operator();
				setState(60);
				array();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(62);
				key();
				setState(63);
				string_compare_operator();
				setState(64);
				value();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(66);
				key();
				setState(67);
				array_operator();
				setState(68);
				array();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Logical_operatorContext extends ParserRuleContext {
		public TerminalNode AND() { return getToken(ExpressionParser.AND, 0); }
		public TerminalNode OR() { return getToken(ExpressionParser.OR, 0); }
		public Logical_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logical_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterLogical_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitLogical_operator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitLogical_operator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Logical_operatorContext logical_operator() throws RecognitionException {
		Logical_operatorContext _localctx = new Logical_operatorContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_logical_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(72);
			_la = _input.LA(1);
			if ( !(_la==OR || _la==AND) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Boolean_operatorContext extends ParserRuleContext {
		public TerminalNode EQUAL() { return getToken(ExpressionParser.EQUAL, 0); }
		public TerminalNode NOTEQUAL() { return getToken(ExpressionParser.NOTEQUAL, 0); }
		public Boolean_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolean_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterBoolean_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitBoolean_operator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitBoolean_operator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Boolean_operatorContext boolean_operator() throws RecognitionException {
		Boolean_operatorContext _localctx = new Boolean_operatorContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_boolean_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			_la = _input.LA(1);
			if ( !(_la==EQUAL || _la==NOTEQUAL) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Numeric_compare_operatorContext extends ParserRuleContext {
		public TerminalNode GT() { return getToken(ExpressionParser.GT, 0); }
		public TerminalNode GTE() { return getToken(ExpressionParser.GTE, 0); }
		public TerminalNode LT() { return getToken(ExpressionParser.LT, 0); }
		public TerminalNode LTE() { return getToken(ExpressionParser.LTE, 0); }
		public Numeric_compare_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numeric_compare_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterNumeric_compare_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitNumeric_compare_operator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitNumeric_compare_operator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Numeric_compare_operatorContext numeric_compare_operator() throws RecognitionException {
		Numeric_compare_operatorContext _localctx = new Numeric_compare_operatorContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_numeric_compare_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << GT) | (1L << GTE) | (1L << LT) | (1L << LTE))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class String_compare_operatorContext extends ParserRuleContext {
		public TerminalNode CONTAINS() { return getToken(ExpressionParser.CONTAINS, 0); }
		public TerminalNode MATCHES() { return getToken(ExpressionParser.MATCHES, 0); }
		public String_compare_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string_compare_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterString_compare_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitString_compare_operator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitString_compare_operator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final String_compare_operatorContext string_compare_operator() throws RecognitionException {
		String_compare_operatorContext _localctx = new String_compare_operatorContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_string_compare_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78);
			_la = _input.LA(1);
			if ( !(_la==CONTAINS || _la==MATCHES) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Array_operatorContext extends ParserRuleContext {
		public TerminalNode IN() { return getToken(ExpressionParser.IN, 0); }
		public TerminalNode CONTAINS() { return getToken(ExpressionParser.CONTAINS, 0); }
		public Array_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterArray_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitArray_operator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitArray_operator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Array_operatorContext array_operator() throws RecognitionException {
		Array_operatorContext _localctx = new Array_operatorContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_array_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			_la = _input.LA(1);
			if ( !(_la==CONTAINS || _la==IN) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ArrayContext extends ParserRuleContext {
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public ArrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterArray(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitArray(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitArray(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayContext array() throws RecognitionException {
		ArrayContext _localctx = new ArrayContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_array);
		int _la;
		try {
			setState(95);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(82);
				match(T__2);
				setState(83);
				value();
				setState(88);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(84);
					match(T__3);
					setState(85);
					value();
					}
					}
					setState(90);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(91);
				match(T__4);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(93);
				match(T__2);
				setState(94);
				match(T__4);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Numerical_valueContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(ExpressionParser.NUMBER, 0); }
		public TerminalNode QUOTED_NUMBER() { return getToken(ExpressionParser.QUOTED_NUMBER, 0); }
		public Numerical_valueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numerical_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterNumerical_value(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitNumerical_value(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitNumerical_value(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Numerical_valueContext numerical_value() throws RecognitionException {
		Numerical_valueContext _localctx = new Numerical_valueContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_numerical_value);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(97);
			_la = _input.LA(1);
			if ( !(_la==QUOTED_NUMBER || _la==NUMBER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public TerminalNode NUMBER() { return getToken(ExpressionParser.NUMBER, 0); }
		public TerminalNode QUOTED_NUMBER() { return getToken(ExpressionParser.QUOTED_NUMBER, 0); }
		public TerminalNode STRING() { return getToken(ExpressionParser.STRING, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_value);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << QUOTED_NUMBER) | (1L << NUMBER) | (1L << STRING))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Negative_exprContext extends ParserRuleContext {
		public TerminalNode NEG() { return getToken(ExpressionParser.NEG, 0); }
		public TerminalNode NOT() { return getToken(ExpressionParser.NOT, 0); }
		public Negative_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_negative_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterNegative_expr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitNegative_expr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitNegative_expr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Negative_exprContext negative_expr() throws RecognitionException {
		Negative_exprContext _localctx = new Negative_exprContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_negative_expr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
			_la = _input.LA(1);
			if ( !(_la==NOT || _la==NEG) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeyContext extends ParserRuleContext {
		public TerminalNode SIMPLETEXT() { return getToken(ExpressionParser.SIMPLETEXT, 0); }
		public TerminalNode STRING() { return getToken(ExpressionParser.STRING, 0); }
		public KeyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_key; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitKey(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitKey(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeyContext key() throws RecognitionException {
		KeyContext _localctx = new KeyContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_key);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			_la = _input.LA(1);
			if ( !(_la==SIMPLETEXT || _la==STRING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return object_sempred((ObjectContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean object_sempred(ObjectContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0019j\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0003\u0001\'\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0005\u0001-\b\u0001\n\u0001\f\u00010\t\u0001\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0003\u0002G\b\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006"+
		"\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0005\bW\b\b"+
		"\n\b\f\bZ\t\b\u0001\b\u0001\b\u0001\b\u0001\b\u0003\b`\b\b\u0001\t\u0001"+
		"\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0000"+
		"\u0001\u0002\r\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u0000\t\u0001\u0000\u0006\u0007\u0001\u0000\t\n\u0001\u0000\u000e"+
		"\u0011\u0001\u0000\u000b\f\u0002\u0000\u000b\u000b\u0012\u0012\u0001\u0000"+
		"\u0013\u0014\u0002\u0000\u0013\u0014\u0018\u0018\u0002\u0000\b\b\r\r\u0001"+
		"\u0000\u0017\u0018f\u0000\u001a\u0001\u0000\u0000\u0000\u0002&\u0001\u0000"+
		"\u0000\u0000\u0004F\u0001\u0000\u0000\u0000\u0006H\u0001\u0000\u0000\u0000"+
		"\bJ\u0001\u0000\u0000\u0000\nL\u0001\u0000\u0000\u0000\fN\u0001\u0000"+
		"\u0000\u0000\u000eP\u0001\u0000\u0000\u0000\u0010_\u0001\u0000\u0000\u0000"+
		"\u0012a\u0001\u0000\u0000\u0000\u0014c\u0001\u0000\u0000\u0000\u0016e"+
		"\u0001\u0000\u0000\u0000\u0018g\u0001\u0000\u0000\u0000\u001a\u001b\u0003"+
		"\u0002\u0001\u0000\u001b\u001c\u0005\u0000\u0000\u0001\u001c\u0001\u0001"+
		"\u0000\u0000\u0000\u001d\u001e\u0006\u0001\uffff\uffff\u0000\u001e\u001f"+
		"\u0003\u0016\u000b\u0000\u001f \u0003\u0002\u0001\u0004 \'\u0001\u0000"+
		"\u0000\u0000!\'\u0003\u0004\u0002\u0000\"#\u0005\u0001\u0000\u0000#$\u0003"+
		"\u0002\u0001\u0000$%\u0005\u0002\u0000\u0000%\'\u0001\u0000\u0000\u0000"+
		"&\u001d\u0001\u0000\u0000\u0000&!\u0001\u0000\u0000\u0000&\"\u0001\u0000"+
		"\u0000\u0000\'.\u0001\u0000\u0000\u0000()\n\u0001\u0000\u0000)*\u0003"+
		"\u0006\u0003\u0000*+\u0003\u0002\u0001\u0002+-\u0001\u0000\u0000\u0000"+
		",(\u0001\u0000\u0000\u0000-0\u0001\u0000\u0000\u0000.,\u0001\u0000\u0000"+
		"\u0000./\u0001\u0000\u0000\u0000/\u0003\u0001\u0000\u0000\u00000.\u0001"+
		"\u0000\u0000\u00001G\u0003\u0018\f\u000023\u0003\u0018\f\u000034\u0003"+
		"\b\u0004\u000045\u0003\u0014\n\u00005G\u0001\u0000\u0000\u000067\u0003"+
		"\u0018\f\u000078\u0003\n\u0005\u000089\u0003\u0012\t\u00009G\u0001\u0000"+
		"\u0000\u0000:;\u0003\u0018\f\u0000;<\u0003\f\u0006\u0000<=\u0003\u0010"+
		"\b\u0000=G\u0001\u0000\u0000\u0000>?\u0003\u0018\f\u0000?@\u0003\f\u0006"+
		"\u0000@A\u0003\u0014\n\u0000AG\u0001\u0000\u0000\u0000BC\u0003\u0018\f"+
		"\u0000CD\u0003\u000e\u0007\u0000DE\u0003\u0010\b\u0000EG\u0001\u0000\u0000"+
		"\u0000F1\u0001\u0000\u0000\u0000F2\u0001\u0000\u0000\u0000F6\u0001\u0000"+
		"\u0000\u0000F:\u0001\u0000\u0000\u0000F>\u0001\u0000\u0000\u0000FB\u0001"+
		"\u0000\u0000\u0000G\u0005\u0001\u0000\u0000\u0000HI\u0007\u0000\u0000"+
		"\u0000I\u0007\u0001\u0000\u0000\u0000JK\u0007\u0001\u0000\u0000K\t\u0001"+
		"\u0000\u0000\u0000LM\u0007\u0002\u0000\u0000M\u000b\u0001\u0000\u0000"+
		"\u0000NO\u0007\u0003\u0000\u0000O\r\u0001\u0000\u0000\u0000PQ\u0007\u0004"+
		"\u0000\u0000Q\u000f\u0001\u0000\u0000\u0000RS\u0005\u0003\u0000\u0000"+
		"SX\u0003\u0014\n\u0000TU\u0005\u0004\u0000\u0000UW\u0003\u0014\n\u0000"+
		"VT\u0001\u0000\u0000\u0000WZ\u0001\u0000\u0000\u0000XV\u0001\u0000\u0000"+
		"\u0000XY\u0001\u0000\u0000\u0000Y[\u0001\u0000\u0000\u0000ZX\u0001\u0000"+
		"\u0000\u0000[\\\u0005\u0005\u0000\u0000\\`\u0001\u0000\u0000\u0000]^\u0005"+
		"\u0003\u0000\u0000^`\u0005\u0005\u0000\u0000_R\u0001\u0000\u0000\u0000"+
		"_]\u0001\u0000\u0000\u0000`\u0011\u0001\u0000\u0000\u0000ab\u0007\u0005"+
		"\u0000\u0000b\u0013\u0001\u0000\u0000\u0000cd\u0007\u0006\u0000\u0000"+
		"d\u0015\u0001\u0000\u0000\u0000ef\u0007\u0007\u0000\u0000f\u0017\u0001"+
		"\u0000\u0000\u0000gh\u0007\b\u0000\u0000h\u0019\u0001\u0000\u0000\u0000"+
		"\u0005&.FX_";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}