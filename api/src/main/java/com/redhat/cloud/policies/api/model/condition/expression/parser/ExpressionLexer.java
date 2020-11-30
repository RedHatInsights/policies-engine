// Generated from com/redhat/cloud/policies/api/model/condition/expression/parser/Expression.g4 by ANTLR 4.7.2
package com.redhat.cloud.policies.api.model.condition.expression.parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ExpressionLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, OR=6, AND=7, NOT=8, EQUAL=9, NOTEQUAL=10, 
		CONTAINS=11, MATCHES=12, NEG=13, GT=14, GTE=15, LT=16, LTE=17, IN=18, 
		QUOTED_NUMBER=19, NUMBER=20, FLOAT=21, INTEGER=22, SIMPLETEXT=23, STRING=24, 
		WS=25;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "OR", "AND", "NOT", "EQUAL", 
			"NOTEQUAL", "CONTAINS", "MATCHES", "NEG", "GT", "GTE", "LT", "LTE", "IN", 
			"QUOTED_NUMBER", "NUMBER", "FLOAT", "INTEGER", "SIMPLETEXT", "STRING", 
			"WS", "ESC", "UNICODE", "HEX", "ESC_DOT", "A", "B", "C", "D", "E", "F", 
			"G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", 
			"U", "V", "W", "X", "Y", "Z", "NEG_OP"
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


	public ExpressionLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Expression.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\33\u012f\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\3\2\3\2\3\3\3\3\3\4\3"+
		"\4\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n"+
		"\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3"+
		"\r\3\r\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3\22\3\22"+
		"\3\22\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\5\24\u00b6"+
		"\n\24\3\25\3\25\3\25\5\25\u00bb\n\25\3\26\3\26\3\26\3\26\3\27\6\27\u00c2"+
		"\n\27\r\27\16\27\u00c3\3\30\3\30\3\30\7\30\u00c9\n\30\f\30\16\30\u00cc"+
		"\13\30\3\31\3\31\3\31\7\31\u00d1\n\31\f\31\16\31\u00d4\13\31\3\31\3\31"+
		"\3\31\3\31\7\31\u00da\n\31\f\31\16\31\u00dd\13\31\3\31\5\31\u00e0\n\31"+
		"\3\32\6\32\u00e3\n\32\r\32\16\32\u00e4\3\32\3\32\3\33\3\33\3\33\3\33\5"+
		"\33\u00ed\n\33\3\34\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3\36\3\36\3\36"+
		"\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3"+
		")\3)\3*\3*\3+\3+\3,\3,\3-\3-\3.\3.\3/\3/\3\60\3\60\3\61\3\61\3\62\3\62"+
		"\3\63\3\63\3\64\3\64\3\65\3\65\3\66\3\66\3\67\3\67\38\38\39\39\2\2:\3"+
		"\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37"+
		"\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\2\67\29\2;\2=\2?\2"+
		"A\2C\2E\2G\2I\2K\2M\2O\2Q\2S\2U\2W\2Y\2[\2]\2_\2a\2c\2e\2g\2i\2k\2m\2"+
		"o\2q\2\3\2$\3\2\62;\7\2\60\60\62;C\\aac|\7\2/\60\62;C\\aac|\4\2))^^\4"+
		"\2$$^^\5\2\13\f\17\17\"\"\n\2))\61\61^^ddhhppttvv\5\2\62;CHch\4\2CCcc"+
		"\4\2DDdd\4\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4\2L"+
		"Lll\4\2MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2TTtt\4"+
		"\2UUuu\4\2VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\2\u011c"+
		"\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2"+
		"\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2"+
		"\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2"+
		"\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2"+
		"\2\2\61\3\2\2\2\2\63\3\2\2\2\3s\3\2\2\2\5u\3\2\2\2\7w\3\2\2\2\ty\3\2\2"+
		"\2\13{\3\2\2\2\r}\3\2\2\2\17\u0080\3\2\2\2\21\u0084\3\2\2\2\23\u0088\3"+
		"\2\2\2\25\u008a\3\2\2\2\27\u008d\3\2\2\2\31\u0096\3\2\2\2\33\u009e\3\2"+
		"\2\2\35\u00a0\3\2\2\2\37\u00a2\3\2\2\2!\u00a5\3\2\2\2#\u00a7\3\2\2\2%"+
		"\u00aa\3\2\2\2\'\u00b5\3\2\2\2)\u00b7\3\2\2\2+\u00bc\3\2\2\2-\u00c1\3"+
		"\2\2\2/\u00c5\3\2\2\2\61\u00df\3\2\2\2\63\u00e2\3\2\2\2\65\u00e8\3\2\2"+
		"\2\67\u00ee\3\2\2\29\u00f4\3\2\2\2;\u00f6\3\2\2\2=\u00f9\3\2\2\2?\u00fb"+
		"\3\2\2\2A\u00fd\3\2\2\2C\u00ff\3\2\2\2E\u0101\3\2\2\2G\u0103\3\2\2\2I"+
		"\u0105\3\2\2\2K\u0107\3\2\2\2M\u0109\3\2\2\2O\u010b\3\2\2\2Q\u010d\3\2"+
		"\2\2S\u010f\3\2\2\2U\u0111\3\2\2\2W\u0113\3\2\2\2Y\u0115\3\2\2\2[\u0117"+
		"\3\2\2\2]\u0119\3\2\2\2_\u011b\3\2\2\2a\u011d\3\2\2\2c\u011f\3\2\2\2e"+
		"\u0121\3\2\2\2g\u0123\3\2\2\2i\u0125\3\2\2\2k\u0127\3\2\2\2m\u0129\3\2"+
		"\2\2o\u012b\3\2\2\2q\u012d\3\2\2\2st\7*\2\2t\4\3\2\2\2uv\7+\2\2v\6\3\2"+
		"\2\2wx\7]\2\2x\b\3\2\2\2yz\7.\2\2z\n\3\2\2\2{|\7_\2\2|\f\3\2\2\2}~\5Y"+
		"-\2~\177\5_\60\2\177\16\3\2\2\2\u0080\u0081\5=\37\2\u0081\u0082\5W,\2"+
		"\u0082\u0083\5C\"\2\u0083\20\3\2\2\2\u0084\u0085\5W,\2\u0085\u0086\5Y"+
		"-\2\u0086\u0087\5c\62\2\u0087\22\3\2\2\2\u0088\u0089\7?\2\2\u0089\24\3"+
		"\2\2\2\u008a\u008b\7#\2\2\u008b\u008c\7?\2\2\u008c\26\3\2\2\2\u008d\u008e"+
		"\5A!\2\u008e\u008f\5Y-\2\u008f\u0090\5W,\2\u0090\u0091\5c\62\2\u0091\u0092"+
		"\5=\37\2\u0092\u0093\5M\'\2\u0093\u0094\5W,\2\u0094\u0095\5a\61\2\u0095"+
		"\30\3\2\2\2\u0096\u0097\5U+\2\u0097\u0098\5=\37\2\u0098\u0099\5c\62\2"+
		"\u0099\u009a\5A!\2\u009a\u009b\5K&\2\u009b\u009c\5E#\2\u009c\u009d\5a"+
		"\61\2\u009d\32\3\2\2\2\u009e\u009f\5q9\2\u009f\34\3\2\2\2\u00a0\u00a1"+
		"\7@\2\2\u00a1\36\3\2\2\2\u00a2\u00a3\7@\2\2\u00a3\u00a4\7?\2\2\u00a4 "+
		"\3\2\2\2\u00a5\u00a6\7>\2\2\u00a6\"\3\2\2\2\u00a7\u00a8\7>\2\2\u00a8\u00a9"+
		"\7?\2\2\u00a9$\3\2\2\2\u00aa\u00ab\5M\'\2\u00ab\u00ac\5W,\2\u00ac&\3\2"+
		"\2\2\u00ad\u00ae\7)\2\2\u00ae\u00af\5)\25\2\u00af\u00b0\7)\2\2\u00b0\u00b6"+
		"\3\2\2\2\u00b1\u00b2\7$\2\2\u00b2\u00b3\5)\25\2\u00b3\u00b4\7$\2\2\u00b4"+
		"\u00b6\3\2\2\2\u00b5\u00ad\3\2\2\2\u00b5\u00b1\3\2\2\2\u00b6(\3\2\2\2"+
		"\u00b7\u00ba\5-\27\2\u00b8\u00b9\7\60\2\2\u00b9\u00bb\5-\27\2\u00ba\u00b8"+
		"\3\2\2\2\u00ba\u00bb\3\2\2\2\u00bb*\3\2\2\2\u00bc\u00bd\5-\27\2\u00bd"+
		"\u00be\7\60\2\2\u00be\u00bf\5-\27\2\u00bf,\3\2\2\2\u00c0\u00c2\t\2\2\2"+
		"\u00c1\u00c0\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3\u00c1\3\2\2\2\u00c3\u00c4"+
		"\3\2\2\2\u00c4.\3\2\2\2\u00c5\u00ca\t\3\2\2\u00c6\u00c9\t\4\2\2\u00c7"+
		"\u00c9\5;\36\2\u00c8\u00c6\3\2\2\2\u00c8\u00c7\3\2\2\2\u00c9\u00cc\3\2"+
		"\2\2\u00ca\u00c8\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb\60\3\2\2\2\u00cc\u00ca"+
		"\3\2\2\2\u00cd\u00d2\7)\2\2\u00ce\u00d1\5\65\33\2\u00cf\u00d1\n\5\2\2"+
		"\u00d0\u00ce\3\2\2\2\u00d0\u00cf\3\2\2\2\u00d1\u00d4\3\2\2\2\u00d2\u00d0"+
		"\3\2\2\2\u00d2\u00d3\3\2\2\2\u00d3\u00d5\3\2\2\2\u00d4\u00d2\3\2\2\2\u00d5"+
		"\u00e0\7)\2\2\u00d6\u00db\7$\2\2\u00d7\u00da\5\65\33\2\u00d8\u00da\n\6"+
		"\2\2\u00d9\u00d7\3\2\2\2\u00d9\u00d8\3\2\2\2\u00da\u00dd\3\2\2\2\u00db"+
		"\u00d9\3\2\2\2\u00db\u00dc\3\2\2\2\u00dc\u00de\3\2\2\2\u00dd\u00db\3\2"+
		"\2\2\u00de\u00e0\7$\2\2\u00df\u00cd\3\2\2\2\u00df\u00d6\3\2\2\2\u00e0"+
		"\62\3\2\2\2\u00e1\u00e3\t\7\2\2\u00e2\u00e1\3\2\2\2\u00e3\u00e4\3\2\2"+
		"\2\u00e4\u00e2\3\2\2\2\u00e4\u00e5\3\2\2\2\u00e5\u00e6\3\2\2\2\u00e6\u00e7"+
		"\b\32\2\2\u00e7\64\3\2\2\2\u00e8\u00ec\7^\2\2\u00e9\u00ed\t\b\2\2\u00ea"+
		"\u00ed\5\67\34\2\u00eb\u00ed\5q9\2\u00ec\u00e9\3\2\2\2\u00ec\u00ea\3\2"+
		"\2\2\u00ec\u00eb\3\2\2\2\u00ed\66\3\2\2\2\u00ee\u00ef\7w\2\2\u00ef\u00f0"+
		"\59\35\2\u00f0\u00f1\59\35\2\u00f1\u00f2\59\35\2\u00f2\u00f3\59\35\2\u00f3"+
		"8\3\2\2\2\u00f4\u00f5\t\t\2\2\u00f5:\3\2\2\2\u00f6\u00f7\7^\2\2\u00f7"+
		"\u00f8\7\60\2\2\u00f8<\3\2\2\2\u00f9\u00fa\t\n\2\2\u00fa>\3\2\2\2\u00fb"+
		"\u00fc\t\13\2\2\u00fc@\3\2\2\2\u00fd\u00fe\t\f\2\2\u00feB\3\2\2\2\u00ff"+
		"\u0100\t\r\2\2\u0100D\3\2\2\2\u0101\u0102\t\16\2\2\u0102F\3\2\2\2\u0103"+
		"\u0104\t\17\2\2\u0104H\3\2\2\2\u0105\u0106\t\20\2\2\u0106J\3\2\2\2\u0107"+
		"\u0108\t\21\2\2\u0108L\3\2\2\2\u0109\u010a\t\22\2\2\u010aN\3\2\2\2\u010b"+
		"\u010c\t\23\2\2\u010cP\3\2\2\2\u010d\u010e\t\24\2\2\u010eR\3\2\2\2\u010f"+
		"\u0110\t\25\2\2\u0110T\3\2\2\2\u0111\u0112\t\26\2\2\u0112V\3\2\2\2\u0113"+
		"\u0114\t\27\2\2\u0114X\3\2\2\2\u0115\u0116\t\30\2\2\u0116Z\3\2\2\2\u0117"+
		"\u0118\t\31\2\2\u0118\\\3\2\2\2\u0119\u011a\t\32\2\2\u011a^\3\2\2\2\u011b"+
		"\u011c\t\33\2\2\u011c`\3\2\2\2\u011d\u011e\t\34\2\2\u011eb\3\2\2\2\u011f"+
		"\u0120\t\35\2\2\u0120d\3\2\2\2\u0121\u0122\t\36\2\2\u0122f\3\2\2\2\u0123"+
		"\u0124\t\37\2\2\u0124h\3\2\2\2\u0125\u0126\t \2\2\u0126j\3\2\2\2\u0127"+
		"\u0128\t!\2\2\u0128l\3\2\2\2\u0129\u012a\t\"\2\2\u012an\3\2\2\2\u012b"+
		"\u012c\t#\2\2\u012cp\3\2\2\2\u012d\u012e\7#\2\2\u012er\3\2\2\2\17\2\u00b5"+
		"\u00ba\u00c3\u00c8\u00ca\u00d0\u00d2\u00d9\u00db\u00df\u00e4\u00ec\3\b"+
		"\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}