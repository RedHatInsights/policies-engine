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
		NUMBER=19, FLOAT=20, INTEGER=21, SIMPLETEXT=22, STRING=23, WS=24;
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
			"NUMBER", "FLOAT", "INTEGER", "SIMPLETEXT", "STRING", "WS", "ESC", "UNICODE", 
			"HEX", "ESC_DOT", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", 
			"L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", 
			"Z", "NEG_OP"
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
			"CONTAINS", "MATCHES", "NEG", "GT", "GTE", "LT", "LTE", "IN", "NUMBER", 
			"FLOAT", "INTEGER", "SIMPLETEXT", "STRING", "WS"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\32\u0123\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\3\2\3\2\3\3\3\3\3\4\3\4\3\5"+
		"\3\5\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n\3\13"+
		"\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r"+
		"\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3\22\3\22\3\22\3"+
		"\23\3\23\3\23\3\24\3\24\3\24\5\24\u00af\n\24\3\25\3\25\3\25\3\25\3\26"+
		"\6\26\u00b6\n\26\r\26\16\26\u00b7\3\27\3\27\3\27\7\27\u00bd\n\27\f\27"+
		"\16\27\u00c0\13\27\3\30\3\30\3\30\7\30\u00c5\n\30\f\30\16\30\u00c8\13"+
		"\30\3\30\3\30\3\30\3\30\7\30\u00ce\n\30\f\30\16\30\u00d1\13\30\3\30\5"+
		"\30\u00d4\n\30\3\31\6\31\u00d7\n\31\r\31\16\31\u00d8\3\31\3\31\3\32\3"+
		"\32\3\32\3\32\5\32\u00e1\n\32\3\33\3\33\3\33\3\33\3\33\3\33\3\34\3\34"+
		"\3\35\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3"+
		"%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3.\3.\3/\3/\3\60"+
		"\3\60\3\61\3\61\3\62\3\62\3\63\3\63\3\64\3\64\3\65\3\65\3\66\3\66\3\67"+
		"\3\67\38\38\2\29\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31"+
		"\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\2\65\2"+
		"\67\29\2;\2=\2?\2A\2C\2E\2G\2I\2K\2M\2O\2Q\2S\2U\2W\2Y\2[\2]\2_\2a\2c"+
		"\2e\2g\2i\2k\2m\2o\2\3\2$\3\2\62;\7\2\60\60\62;C\\aac|\7\2/\60\62;C\\"+
		"aac|\4\2))^^\4\2$$^^\5\2\13\f\17\17\"\"\n\2))\61\61^^ddhhppttvv\5\2\62"+
		";CHch\4\2CCcc\4\2DDdd\4\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj"+
		"\4\2KKkk\4\2LLll\4\2MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2S"+
		"Sss\4\2TTtt\4\2UUuu\4\2VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4"+
		"\2\\\\||\2\u010f\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13"+
		"\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2"+
		"\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2"+
		"!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3"+
		"\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\3q\3\2\2\2\5s\3\2\2\2\7u\3\2\2\2\tw\3\2"+
		"\2\2\13y\3\2\2\2\r{\3\2\2\2\17~\3\2\2\2\21\u0082\3\2\2\2\23\u0086\3\2"+
		"\2\2\25\u0088\3\2\2\2\27\u008b\3\2\2\2\31\u0094\3\2\2\2\33\u009c\3\2\2"+
		"\2\35\u009e\3\2\2\2\37\u00a0\3\2\2\2!\u00a3\3\2\2\2#\u00a5\3\2\2\2%\u00a8"+
		"\3\2\2\2\'\u00ab\3\2\2\2)\u00b0\3\2\2\2+\u00b5\3\2\2\2-\u00b9\3\2\2\2"+
		"/\u00d3\3\2\2\2\61\u00d6\3\2\2\2\63\u00dc\3\2\2\2\65\u00e2\3\2\2\2\67"+
		"\u00e8\3\2\2\29\u00ea\3\2\2\2;\u00ed\3\2\2\2=\u00ef\3\2\2\2?\u00f1\3\2"+
		"\2\2A\u00f3\3\2\2\2C\u00f5\3\2\2\2E\u00f7\3\2\2\2G\u00f9\3\2\2\2I\u00fb"+
		"\3\2\2\2K\u00fd\3\2\2\2M\u00ff\3\2\2\2O\u0101\3\2\2\2Q\u0103\3\2\2\2S"+
		"\u0105\3\2\2\2U\u0107\3\2\2\2W\u0109\3\2\2\2Y\u010b\3\2\2\2[\u010d\3\2"+
		"\2\2]\u010f\3\2\2\2_\u0111\3\2\2\2a\u0113\3\2\2\2c\u0115\3\2\2\2e\u0117"+
		"\3\2\2\2g\u0119\3\2\2\2i\u011b\3\2\2\2k\u011d\3\2\2\2m\u011f\3\2\2\2o"+
		"\u0121\3\2\2\2qr\7*\2\2r\4\3\2\2\2st\7+\2\2t\6\3\2\2\2uv\7]\2\2v\b\3\2"+
		"\2\2wx\7.\2\2x\n\3\2\2\2yz\7_\2\2z\f\3\2\2\2{|\5W,\2|}\5]/\2}\16\3\2\2"+
		"\2~\177\5;\36\2\177\u0080\5U+\2\u0080\u0081\5A!\2\u0081\20\3\2\2\2\u0082"+
		"\u0083\5U+\2\u0083\u0084\5W,\2\u0084\u0085\5a\61\2\u0085\22\3\2\2\2\u0086"+
		"\u0087\7?\2\2\u0087\24\3\2\2\2\u0088\u0089\7#\2\2\u0089\u008a\7?\2\2\u008a"+
		"\26\3\2\2\2\u008b\u008c\5? \2\u008c\u008d\5W,\2\u008d\u008e\5U+\2\u008e"+
		"\u008f\5a\61\2\u008f\u0090\5;\36\2\u0090\u0091\5K&\2\u0091\u0092\5U+\2"+
		"\u0092\u0093\5_\60\2\u0093\30\3\2\2\2\u0094\u0095\5S*\2\u0095\u0096\5"+
		";\36\2\u0096\u0097\5a\61\2\u0097\u0098\5? \2\u0098\u0099\5I%\2\u0099\u009a"+
		"\5C\"\2\u009a\u009b\5_\60\2\u009b\32\3\2\2\2\u009c\u009d\5o8\2\u009d\34"+
		"\3\2\2\2\u009e\u009f\7@\2\2\u009f\36\3\2\2\2\u00a0\u00a1\7@\2\2\u00a1"+
		"\u00a2\7?\2\2\u00a2 \3\2\2\2\u00a3\u00a4\7>\2\2\u00a4\"\3\2\2\2\u00a5"+
		"\u00a6\7>\2\2\u00a6\u00a7\7?\2\2\u00a7$\3\2\2\2\u00a8\u00a9\5K&\2\u00a9"+
		"\u00aa\5U+\2\u00aa&\3\2\2\2\u00ab\u00ae\5+\26\2\u00ac\u00ad\7\60\2\2\u00ad"+
		"\u00af\5+\26\2\u00ae\u00ac\3\2\2\2\u00ae\u00af\3\2\2\2\u00af(\3\2\2\2"+
		"\u00b0\u00b1\5+\26\2\u00b1\u00b2\7\60\2\2\u00b2\u00b3\5+\26\2\u00b3*\3"+
		"\2\2\2\u00b4\u00b6\t\2\2\2\u00b5\u00b4\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7"+
		"\u00b5\3\2\2\2\u00b7\u00b8\3\2\2\2\u00b8,\3\2\2\2\u00b9\u00be\t\3\2\2"+
		"\u00ba\u00bd\t\4\2\2\u00bb\u00bd\59\35\2\u00bc\u00ba\3\2\2\2\u00bc\u00bb"+
		"\3\2\2\2\u00bd\u00c0\3\2\2\2\u00be\u00bc\3\2\2\2\u00be\u00bf\3\2\2\2\u00bf"+
		".\3\2\2\2\u00c0\u00be\3\2\2\2\u00c1\u00c6\7)\2\2\u00c2\u00c5\5\63\32\2"+
		"\u00c3\u00c5\n\5\2\2\u00c4\u00c2\3\2\2\2\u00c4\u00c3\3\2\2\2\u00c5\u00c8"+
		"\3\2\2\2\u00c6\u00c4\3\2\2\2\u00c6\u00c7\3\2\2\2\u00c7\u00c9\3\2\2\2\u00c8"+
		"\u00c6\3\2\2\2\u00c9\u00d4\7)\2\2\u00ca\u00cf\7$\2\2\u00cb\u00ce\5\63"+
		"\32\2\u00cc\u00ce\n\6\2\2\u00cd\u00cb\3\2\2\2\u00cd\u00cc\3\2\2\2\u00ce"+
		"\u00d1\3\2\2\2\u00cf\u00cd\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0\u00d2\3\2"+
		"\2\2\u00d1\u00cf\3\2\2\2\u00d2\u00d4\7$\2\2\u00d3\u00c1\3\2\2\2\u00d3"+
		"\u00ca\3\2\2\2\u00d4\60\3\2\2\2\u00d5\u00d7\t\7\2\2\u00d6\u00d5\3\2\2"+
		"\2\u00d7\u00d8\3\2\2\2\u00d8\u00d6\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9\u00da"+
		"\3\2\2\2\u00da\u00db\b\31\2\2\u00db\62\3\2\2\2\u00dc\u00e0\7^\2\2\u00dd"+
		"\u00e1\t\b\2\2\u00de\u00e1\5\65\33\2\u00df\u00e1\5o8\2\u00e0\u00dd\3\2"+
		"\2\2\u00e0\u00de\3\2\2\2\u00e0\u00df\3\2\2\2\u00e1\64\3\2\2\2\u00e2\u00e3"+
		"\7w\2\2\u00e3\u00e4\5\67\34\2\u00e4\u00e5\5\67\34\2\u00e5\u00e6\5\67\34"+
		"\2\u00e6\u00e7\5\67\34\2\u00e7\66\3\2\2\2\u00e8\u00e9\t\t\2\2\u00e98\3"+
		"\2\2\2\u00ea\u00eb\7^\2\2\u00eb\u00ec\7\60\2\2\u00ec:\3\2\2\2\u00ed\u00ee"+
		"\t\n\2\2\u00ee<\3\2\2\2\u00ef\u00f0\t\13\2\2\u00f0>\3\2\2\2\u00f1\u00f2"+
		"\t\f\2\2\u00f2@\3\2\2\2\u00f3\u00f4\t\r\2\2\u00f4B\3\2\2\2\u00f5\u00f6"+
		"\t\16\2\2\u00f6D\3\2\2\2\u00f7\u00f8\t\17\2\2\u00f8F\3\2\2\2\u00f9\u00fa"+
		"\t\20\2\2\u00faH\3\2\2\2\u00fb\u00fc\t\21\2\2\u00fcJ\3\2\2\2\u00fd\u00fe"+
		"\t\22\2\2\u00feL\3\2\2\2\u00ff\u0100\t\23\2\2\u0100N\3\2\2\2\u0101\u0102"+
		"\t\24\2\2\u0102P\3\2\2\2\u0103\u0104\t\25\2\2\u0104R\3\2\2\2\u0105\u0106"+
		"\t\26\2\2\u0106T\3\2\2\2\u0107\u0108\t\27\2\2\u0108V\3\2\2\2\u0109\u010a"+
		"\t\30\2\2\u010aX\3\2\2\2\u010b\u010c\t\31\2\2\u010cZ\3\2\2\2\u010d\u010e"+
		"\t\32\2\2\u010e\\\3\2\2\2\u010f\u0110\t\33\2\2\u0110^\3\2\2\2\u0111\u0112"+
		"\t\34\2\2\u0112`\3\2\2\2\u0113\u0114\t\35\2\2\u0114b\3\2\2\2\u0115\u0116"+
		"\t\36\2\2\u0116d\3\2\2\2\u0117\u0118\t\37\2\2\u0118f\3\2\2\2\u0119\u011a"+
		"\t \2\2\u011ah\3\2\2\2\u011b\u011c\t!\2\2\u011cj\3\2\2\2\u011d\u011e\t"+
		"\"\2\2\u011el\3\2\2\2\u011f\u0120\t#\2\2\u0120n\3\2\2\2\u0121\u0122\7"+
		"#\2\2\u0122p\3\2\2\2\16\2\u00ae\u00b7\u00bc\u00be\u00c4\u00c6\u00cd\u00cf"+
		"\u00d3\u00d8\u00e0\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}