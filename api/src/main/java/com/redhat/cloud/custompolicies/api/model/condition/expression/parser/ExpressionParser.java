// Generated from com/redhat/cloud/custompolicies/api/model/condition/expression/parser/Expression.g4 by ANTLR 4.7.2
package com.redhat.cloud.custompolicies.api.model.condition.expression.parser;
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
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, OR=6, AND=7, NOT=8, EQUAL=9, NOTEQUAL=10, 
		GT=11, GTE=12, LT=13, LTE=14, IN=15, NUMBER=16, FLOAT=17, INTEGER=18, 
		SIMPLETEXT=19, STRING=20, WS=21;
	public static final int
		RULE_expression = 0, RULE_object = 1, RULE_expr = 2, RULE_logical_operator = 3, 
		RULE_boolean_operator = 4, RULE_compare_operator = 5, RULE_array_operator = 6, 
		RULE_array = 7, RULE_numerical_value = 8, RULE_value = 9, RULE_key = 10;
	private static String[] makeRuleNames() {
		return new String[] {
			"expression", "object", "expr", "logical_operator", "boolean_operator", 
			"compare_operator", "array_operator", "array", "numerical_value", "value", 
			"key"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'['", "','", "']'", null, null, null, "'='", "'!='", 
			"'>'", "'>='", "'<'", "'<='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, "OR", "AND", "NOT", "EQUAL", "NOTEQUAL", 
			"GT", "GTE", "LT", "LTE", "IN", "NUMBER", "FLOAT", "INTEGER", "SIMPLETEXT", 
			"STRING", "WS"
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
			setState(22);
			object(0);
			setState(23);
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
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public List<ObjectContext> object() {
			return getRuleContexts(ObjectContext.class);
		}
		public ObjectContext object(int i) {
			return getRuleContext(ObjectContext.class,i);
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
			setState(31);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
			case SIMPLETEXT:
				{
				setState(26);
				expr();
				}
				break;
			case T__0:
				{
				setState(27);
				match(T__0);
				setState(28);
				object(0);
				setState(29);
				match(T__1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(39);
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
					setState(33);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(34);
					logical_operator();
					setState(35);
					object(2);
					}
					} 
				}
				setState(41);
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
		public TerminalNode NOT() { return getToken(ExpressionParser.NOT, 0); }
		public Boolean_operatorContext boolean_operator() {
			return getRuleContext(Boolean_operatorContext.class,0);
		}
		public ValueContext value() {
			return getRuleContext(ValueContext.class,0);
		}
		public Compare_operatorContext compare_operator() {
			return getRuleContext(Compare_operatorContext.class,0);
		}
		public Numerical_valueContext numerical_value() {
			return getRuleContext(Numerical_valueContext.class,0);
		}
		public Array_operatorContext array_operator() {
			return getRuleContext(Array_operatorContext.class,0);
		}
		public ArrayContext array() {
			return getRuleContext(ArrayContext.class,0);
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
			setState(57);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(42);
				key();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(43);
				match(NOT);
				setState(44);
				key();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(45);
				key();
				setState(46);
				boolean_operator();
				setState(47);
				value();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(49);
				key();
				setState(50);
				compare_operator();
				setState(51);
				numerical_value();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(53);
				key();
				setState(54);
				array_operator();
				setState(55);
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
			setState(59);
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
			setState(61);
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

	public static class Compare_operatorContext extends ParserRuleContext {
		public TerminalNode GT() { return getToken(ExpressionParser.GT, 0); }
		public TerminalNode GTE() { return getToken(ExpressionParser.GTE, 0); }
		public TerminalNode LT() { return getToken(ExpressionParser.LT, 0); }
		public TerminalNode LTE() { return getToken(ExpressionParser.LTE, 0); }
		public Compare_operatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compare_operator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).enterCompare_operator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ExpressionListener ) ((ExpressionListener)listener).exitCompare_operator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ExpressionVisitor ) return ((ExpressionVisitor<? extends T>)visitor).visitCompare_operator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Compare_operatorContext compare_operator() throws RecognitionException {
		Compare_operatorContext _localctx = new Compare_operatorContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_compare_operator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(63);
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

	public static class Array_operatorContext extends ParserRuleContext {
		public TerminalNode IN() { return getToken(ExpressionParser.IN, 0); }
		public TerminalNode NOT() { return getToken(ExpressionParser.NOT, 0); }
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
		enterRule(_localctx, 12, RULE_array_operator);
		try {
			setState(68);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IN:
				enterOuterAlt(_localctx, 1);
				{
				setState(65);
				match(IN);
				}
				break;
			case NOT:
				enterOuterAlt(_localctx, 2);
				{
				setState(66);
				match(NOT);
				setState(67);
				match(IN);
				}
				break;
			default:
				throw new NoViableAltException(this);
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
		enterRule(_localctx, 14, RULE_array);
		int _la;
		try {
			setState(83);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(70);
				match(T__2);
				setState(71);
				value();
				setState(76);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__3) {
					{
					{
					setState(72);
					match(T__3);
					setState(73);
					value();
					}
					}
					setState(78);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(79);
				match(T__4);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(81);
				match(T__2);
				setState(82);
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
		enterRule(_localctx, 16, RULE_numerical_value);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			match(NUMBER);
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
		public TerminalNode SIMPLETEXT() { return getToken(ExpressionParser.SIMPLETEXT, 0); }
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
		enterRule(_localctx, 18, RULE_value);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NUMBER) | (1L << SIMPLETEXT) | (1L << STRING))) != 0)) ) {
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
		enterRule(_localctx, 20, RULE_key);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			match(SIMPLETEXT);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\27^\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t\13\4"+
		"\f\t\f\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\5\3\"\n\3\3\3\3\3\3\3\3\3\7"+
		"\3(\n\3\f\3\16\3+\13\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4"+
		"\3\4\3\4\3\4\5\4<\n\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\5\bG\n\b\3\t"+
		"\3\t\3\t\3\t\7\tM\n\t\f\t\16\tP\13\t\3\t\3\t\3\t\3\t\5\tV\n\t\3\n\3\n"+
		"\3\13\3\13\3\f\3\f\3\f\2\3\4\r\2\4\6\b\n\f\16\20\22\24\26\2\6\3\2\b\t"+
		"\3\2\13\f\3\2\r\20\4\2\22\22\25\26\2[\2\30\3\2\2\2\4!\3\2\2\2\6;\3\2\2"+
		"\2\b=\3\2\2\2\n?\3\2\2\2\fA\3\2\2\2\16F\3\2\2\2\20U\3\2\2\2\22W\3\2\2"+
		"\2\24Y\3\2\2\2\26[\3\2\2\2\30\31\5\4\3\2\31\32\7\2\2\3\32\3\3\2\2\2\33"+
		"\34\b\3\1\2\34\"\5\6\4\2\35\36\7\3\2\2\36\37\5\4\3\2\37 \7\4\2\2 \"\3"+
		"\2\2\2!\33\3\2\2\2!\35\3\2\2\2\")\3\2\2\2#$\f\3\2\2$%\5\b\5\2%&\5\4\3"+
		"\4&(\3\2\2\2\'#\3\2\2\2(+\3\2\2\2)\'\3\2\2\2)*\3\2\2\2*\5\3\2\2\2+)\3"+
		"\2\2\2,<\5\26\f\2-.\7\n\2\2.<\5\26\f\2/\60\5\26\f\2\60\61\5\n\6\2\61\62"+
		"\5\24\13\2\62<\3\2\2\2\63\64\5\26\f\2\64\65\5\f\7\2\65\66\5\22\n\2\66"+
		"<\3\2\2\2\678\5\26\f\289\5\16\b\29:\5\20\t\2:<\3\2\2\2;,\3\2\2\2;-\3\2"+
		"\2\2;/\3\2\2\2;\63\3\2\2\2;\67\3\2\2\2<\7\3\2\2\2=>\t\2\2\2>\t\3\2\2\2"+
		"?@\t\3\2\2@\13\3\2\2\2AB\t\4\2\2B\r\3\2\2\2CG\7\21\2\2DE\7\n\2\2EG\7\21"+
		"\2\2FC\3\2\2\2FD\3\2\2\2G\17\3\2\2\2HI\7\5\2\2IN\5\24\13\2JK\7\6\2\2K"+
		"M\5\24\13\2LJ\3\2\2\2MP\3\2\2\2NL\3\2\2\2NO\3\2\2\2OQ\3\2\2\2PN\3\2\2"+
		"\2QR\7\7\2\2RV\3\2\2\2ST\7\5\2\2TV\7\7\2\2UH\3\2\2\2US\3\2\2\2V\21\3\2"+
		"\2\2WX\7\22\2\2X\23\3\2\2\2YZ\t\5\2\2Z\25\3\2\2\2[\\\7\25\2\2\\\27\3\2"+
		"\2\2\b!);FNU";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}