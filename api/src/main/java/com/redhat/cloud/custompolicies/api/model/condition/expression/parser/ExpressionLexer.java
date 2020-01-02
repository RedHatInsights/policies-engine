// Generated from com/redhat/cloud/custompolicies/api/model/condition/expression/parser/Expression.g4 by ANTLR 4.7.2
package com.redhat.cloud.custompolicies.api.model.condition.expression.parser;
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
		CONTAINS=11, NEG=12, GT=13, GTE=14, LT=15, LTE=16, IN=17, NUMBER=18, FLOAT=19, 
		INTEGER=20, SIMPLETEXT=21, STRING=22, WS=23;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "OR", "AND", "NOT", "EQUAL", 
			"NOTEQUAL", "CONTAINS", "NEG", "GT", "GTE", "LT", "LTE", "IN", "NUMBER", 
			"FLOAT", "INTEGER", "SIMPLETEXT", "STRING", "WS", "ESC", "UNICODE", "HEX", 
			"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", 
			"O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "NEG_OP"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'['", "','", "']'", null, null, null, "'='", "'!='", 
			null, null, "'>'", "'>='", "'<'", "'<='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, "OR", "AND", "NOT", "EQUAL", "NOTEQUAL", 
			"CONTAINS", "NEG", "GT", "GTE", "LT", "LTE", "IN", "NUMBER", "FLOAT", 
			"INTEGER", "SIMPLETEXT", "STRING", "WS"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\31\u0113\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\4\65\t\65\4\66\t\66\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7"+
		"\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3\f\3"+
		"\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\17\3\20\3"+
		"\20\3\21\3\21\3\21\3\22\3\22\3\22\3\23\3\23\3\23\5\23\u00a3\n\23\3\24"+
		"\3\24\3\24\3\24\3\25\6\25\u00aa\n\25\r\25\16\25\u00ab\3\26\3\26\7\26\u00b0"+
		"\n\26\f\26\16\26\u00b3\13\26\3\27\3\27\3\27\7\27\u00b8\n\27\f\27\16\27"+
		"\u00bb\13\27\3\27\3\27\3\27\3\27\7\27\u00c1\n\27\f\27\16\27\u00c4\13\27"+
		"\3\27\5\27\u00c7\n\27\3\30\6\30\u00ca\n\30\r\30\16\30\u00cb\3\30\3\30"+
		"\3\31\3\31\3\31\3\31\5\31\u00d4\n\31\3\32\3\32\3\32\3\32\3\32\3\32\3\33"+
		"\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3"+
		"#\3$\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3.\3."+
		"\3/\3/\3\60\3\60\3\61\3\61\3\62\3\62\3\63\3\63\3\64\3\64\3\65\3\65\3\66"+
		"\3\66\2\2\67\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16"+
		"\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\2\63\2\65\2\67"+
		"\29\2;\2=\2?\2A\2C\2E\2G\2I\2K\2M\2O\2Q\2S\2U\2W\2Y\2[\2]\2_\2a\2c\2e"+
		"\2g\2i\2k\2\3\2$\3\2\62;\7\2\60\60\62;C\\aac|\7\2/\60\62;C\\aac|\4\2)"+
		")^^\4\2$$^^\5\2\13\f\17\17\"\"\n\2))\61\61^^ddhhppttvv\5\2\62;CHch\4\2"+
		"CCcc\4\2DDdd\4\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4"+
		"\2LLll\4\2MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2TTt"+
		"t\4\2UUuu\4\2VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\2"+
		"\u00ff\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2"+
		"\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3"+
		"\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2"+
		"\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2"+
		"/\3\2\2\2\3m\3\2\2\2\5o\3\2\2\2\7q\3\2\2\2\ts\3\2\2\2\13u\3\2\2\2\rw\3"+
		"\2\2\2\17z\3\2\2\2\21~\3\2\2\2\23\u0082\3\2\2\2\25\u0084\3\2\2\2\27\u0087"+
		"\3\2\2\2\31\u0090\3\2\2\2\33\u0092\3\2\2\2\35\u0094\3\2\2\2\37\u0097\3"+
		"\2\2\2!\u0099\3\2\2\2#\u009c\3\2\2\2%\u009f\3\2\2\2\'\u00a4\3\2\2\2)\u00a9"+
		"\3\2\2\2+\u00ad\3\2\2\2-\u00c6\3\2\2\2/\u00c9\3\2\2\2\61\u00cf\3\2\2\2"+
		"\63\u00d5\3\2\2\2\65\u00db\3\2\2\2\67\u00dd\3\2\2\29\u00df\3\2\2\2;\u00e1"+
		"\3\2\2\2=\u00e3\3\2\2\2?\u00e5\3\2\2\2A\u00e7\3\2\2\2C\u00e9\3\2\2\2E"+
		"\u00eb\3\2\2\2G\u00ed\3\2\2\2I\u00ef\3\2\2\2K\u00f1\3\2\2\2M\u00f3\3\2"+
		"\2\2O\u00f5\3\2\2\2Q\u00f7\3\2\2\2S\u00f9\3\2\2\2U\u00fb\3\2\2\2W\u00fd"+
		"\3\2\2\2Y\u00ff\3\2\2\2[\u0101\3\2\2\2]\u0103\3\2\2\2_\u0105\3\2\2\2a"+
		"\u0107\3\2\2\2c\u0109\3\2\2\2e\u010b\3\2\2\2g\u010d\3\2\2\2i\u010f\3\2"+
		"\2\2k\u0111\3\2\2\2mn\7*\2\2n\4\3\2\2\2op\7+\2\2p\6\3\2\2\2qr\7]\2\2r"+
		"\b\3\2\2\2st\7.\2\2t\n\3\2\2\2uv\7_\2\2v\f\3\2\2\2wx\5S*\2xy\5Y-\2y\16"+
		"\3\2\2\2z{\5\67\34\2{|\5Q)\2|}\5=\37\2}\20\3\2\2\2~\177\5Q)\2\177\u0080"+
		"\5S*\2\u0080\u0081\5]/\2\u0081\22\3\2\2\2\u0082\u0083\7?\2\2\u0083\24"+
		"\3\2\2\2\u0084\u0085\7#\2\2\u0085\u0086\7?\2\2\u0086\26\3\2\2\2\u0087"+
		"\u0088\5;\36\2\u0088\u0089\5S*\2\u0089\u008a\5Q)\2\u008a\u008b\5]/\2\u008b"+
		"\u008c\5\67\34\2\u008c\u008d\5G$\2\u008d\u008e\5Q)\2\u008e\u008f\5[.\2"+
		"\u008f\30\3\2\2\2\u0090\u0091\5k\66\2\u0091\32\3\2\2\2\u0092\u0093\7@"+
		"\2\2\u0093\34\3\2\2\2\u0094\u0095\7@\2\2\u0095\u0096\7?\2\2\u0096\36\3"+
		"\2\2\2\u0097\u0098\7>\2\2\u0098 \3\2\2\2\u0099\u009a\7>\2\2\u009a\u009b"+
		"\7?\2\2\u009b\"\3\2\2\2\u009c\u009d\5G$\2\u009d\u009e\5Q)\2\u009e$\3\2"+
		"\2\2\u009f\u00a2\5)\25\2\u00a0\u00a1\7\60\2\2\u00a1\u00a3\5)\25\2\u00a2"+
		"\u00a0\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3&\3\2\2\2\u00a4\u00a5\5)\25\2"+
		"\u00a5\u00a6\7\60\2\2\u00a6\u00a7\5)\25\2\u00a7(\3\2\2\2\u00a8\u00aa\t"+
		"\2\2\2\u00a9\u00a8\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab\u00a9\3\2\2\2\u00ab"+
		"\u00ac\3\2\2\2\u00ac*\3\2\2\2\u00ad\u00b1\t\3\2\2\u00ae\u00b0\t\4\2\2"+
		"\u00af\u00ae\3\2\2\2\u00b0\u00b3\3\2\2\2\u00b1\u00af\3\2\2\2\u00b1\u00b2"+
		"\3\2\2\2\u00b2,\3\2\2\2\u00b3\u00b1\3\2\2\2\u00b4\u00b9\7)\2\2\u00b5\u00b8"+
		"\5\61\31\2\u00b6\u00b8\n\5\2\2\u00b7\u00b5\3\2\2\2\u00b7\u00b6\3\2\2\2"+
		"\u00b8\u00bb\3\2\2\2\u00b9\u00b7\3\2\2\2\u00b9\u00ba\3\2\2\2\u00ba\u00bc"+
		"\3\2\2\2\u00bb\u00b9\3\2\2\2\u00bc\u00c7\7)\2\2\u00bd\u00c2\7$\2\2\u00be"+
		"\u00c1\5\61\31\2\u00bf\u00c1\n\6\2\2\u00c0\u00be\3\2\2\2\u00c0\u00bf\3"+
		"\2\2\2\u00c1\u00c4\3\2\2\2\u00c2\u00c0\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3"+
		"\u00c5\3\2\2\2\u00c4\u00c2\3\2\2\2\u00c5\u00c7\7$\2\2\u00c6\u00b4\3\2"+
		"\2\2\u00c6\u00bd\3\2\2\2\u00c7.\3\2\2\2\u00c8\u00ca\t\7\2\2\u00c9\u00c8"+
		"\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb\u00c9\3\2\2\2\u00cb\u00cc\3\2\2\2\u00cc"+
		"\u00cd\3\2\2\2\u00cd\u00ce\b\30\2\2\u00ce\60\3\2\2\2\u00cf\u00d3\7^\2"+
		"\2\u00d0\u00d4\t\b\2\2\u00d1\u00d4\5\63\32\2\u00d2\u00d4\5k\66\2\u00d3"+
		"\u00d0\3\2\2\2\u00d3\u00d1\3\2\2\2\u00d3\u00d2\3\2\2\2\u00d4\62\3\2\2"+
		"\2\u00d5\u00d6\7w\2\2\u00d6\u00d7\5\65\33\2\u00d7\u00d8\5\65\33\2\u00d8"+
		"\u00d9\5\65\33\2\u00d9\u00da\5\65\33\2\u00da\64\3\2\2\2\u00db\u00dc\t"+
		"\t\2\2\u00dc\66\3\2\2\2\u00dd\u00de\t\n\2\2\u00de8\3\2\2\2\u00df\u00e0"+
		"\t\13\2\2\u00e0:\3\2\2\2\u00e1\u00e2\t\f\2\2\u00e2<\3\2\2\2\u00e3\u00e4"+
		"\t\r\2\2\u00e4>\3\2\2\2\u00e5\u00e6\t\16\2\2\u00e6@\3\2\2\2\u00e7\u00e8"+
		"\t\17\2\2\u00e8B\3\2\2\2\u00e9\u00ea\t\20\2\2\u00eaD\3\2\2\2\u00eb\u00ec"+
		"\t\21\2\2\u00ecF\3\2\2\2\u00ed\u00ee\t\22\2\2\u00eeH\3\2\2\2\u00ef\u00f0"+
		"\t\23\2\2\u00f0J\3\2\2\2\u00f1\u00f2\t\24\2\2\u00f2L\3\2\2\2\u00f3\u00f4"+
		"\t\25\2\2\u00f4N\3\2\2\2\u00f5\u00f6\t\26\2\2\u00f6P\3\2\2\2\u00f7\u00f8"+
		"\t\27\2\2\u00f8R\3\2\2\2\u00f9\u00fa\t\30\2\2\u00faT\3\2\2\2\u00fb\u00fc"+
		"\t\31\2\2\u00fcV\3\2\2\2\u00fd\u00fe\t\32\2\2\u00feX\3\2\2\2\u00ff\u0100"+
		"\t\33\2\2\u0100Z\3\2\2\2\u0101\u0102\t\34\2\2\u0102\\\3\2\2\2\u0103\u0104"+
		"\t\35\2\2\u0104^\3\2\2\2\u0105\u0106\t\36\2\2\u0106`\3\2\2\2\u0107\u0108"+
		"\t\37\2\2\u0108b\3\2\2\2\u0109\u010a\t \2\2\u010ad\3\2\2\2\u010b\u010c"+
		"\t!\2\2\u010cf\3\2\2\2\u010d\u010e\t\"\2\2\u010eh\3\2\2\2\u010f\u0110"+
		"\t#\2\2\u0110j\3\2\2\2\u0111\u0112\7#\2\2\u0112l\3\2\2\2\r\2\u00a2\u00ab"+
		"\u00b1\u00b7\u00b9\u00c0\u00c2\u00c6\u00cb\u00d3\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}