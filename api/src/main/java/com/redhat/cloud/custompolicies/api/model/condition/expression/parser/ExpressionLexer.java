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
		GT=11, GTE=12, LT=13, LTE=14, IN=15, NUMBER=16, DIGIT=17, SIMPLETEXT=18, 
		STRING=19, WS=20;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "OR", "AND", "NOT", "EQUAL", 
			"NOTEQUAL", "GT", "GTE", "LT", "LTE", "IN", "NUMBER", "DIGIT", "SIMPLETEXT", 
			"STRING", "WS", "ESC", "UNICODE", "HEX", "A", "B", "C", "D", "E", "F", 
			"G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", 
			"U", "V", "W", "X", "Y", "Z", "NEG_OP"
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
			"GT", "GTE", "LT", "LTE", "IN", "NUMBER", "DIGIT", "SIMPLETEXT", "STRING", 
			"WS"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\26\u00fe\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\3\2"+
		"\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\t\3"+
		"\t\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\17\3"+
		"\17\3\17\3\20\3\20\3\20\3\21\3\21\3\21\5\21\u0092\n\21\3\22\6\22\u0095"+
		"\n\22\r\22\16\22\u0096\3\23\3\23\7\23\u009b\n\23\f\23\16\23\u009e\13\23"+
		"\3\24\3\24\3\24\7\24\u00a3\n\24\f\24\16\24\u00a6\13\24\3\24\3\24\3\24"+
		"\3\24\7\24\u00ac\n\24\f\24\16\24\u00af\13\24\3\24\5\24\u00b2\n\24\3\25"+
		"\6\25\u00b5\n\25\r\25\16\25\u00b6\3\25\3\25\3\26\3\26\3\26\3\26\5\26\u00bf"+
		"\n\26\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33"+
		"\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3"+
		"#\3$\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3.\3."+
		"\3/\3/\3\60\3\60\3\61\3\61\3\62\3\62\3\63\3\63\2\2\64\3\3\5\4\7\5\t\6"+
		"\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24"+
		"\'\25)\26+\2-\2/\2\61\2\63\2\65\2\67\29\2;\2=\2?\2A\2C\2E\2G\2I\2K\2M"+
		"\2O\2Q\2S\2U\2W\2Y\2[\2]\2_\2a\2c\2e\2\3\2$\3\2\62;\7\2\60\60\62;C\\a"+
		"ac|\7\2/\60\62;C\\aac|\4\2))^^\4\2$$^^\5\2\13\f\17\17\"\"\n\2))\61\61"+
		"^^ddhhppttvv\5\2\62;CHch\4\2CCcc\4\2DDdd\4\2EEee\4\2FFff\4\2GGgg\4\2H"+
		"Hhh\4\2IIii\4\2JJjj\4\2KKkk\4\2LLll\4\2MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4"+
		"\2QQqq\4\2RRrr\4\2SSss\4\2TTtt\4\2UUuu\4\2VVvv\4\2WWww\4\2XXxx\4\2YYy"+
		"y\4\2ZZzz\4\2[[{{\4\2\\\\||\2\u00ea\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2"+
		"\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23"+
		"\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2"+
		"\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2"+
		"\2\2\3g\3\2\2\2\5i\3\2\2\2\7k\3\2\2\2\tm\3\2\2\2\13o\3\2\2\2\rq\3\2\2"+
		"\2\17t\3\2\2\2\21x\3\2\2\2\23|\3\2\2\2\25~\3\2\2\2\27\u0081\3\2\2\2\31"+
		"\u0083\3\2\2\2\33\u0086\3\2\2\2\35\u0088\3\2\2\2\37\u008b\3\2\2\2!\u008e"+
		"\3\2\2\2#\u0094\3\2\2\2%\u0098\3\2\2\2\'\u00b1\3\2\2\2)\u00b4\3\2\2\2"+
		"+\u00ba\3\2\2\2-\u00c0\3\2\2\2/\u00c6\3\2\2\2\61\u00c8\3\2\2\2\63\u00ca"+
		"\3\2\2\2\65\u00cc\3\2\2\2\67\u00ce\3\2\2\29\u00d0\3\2\2\2;\u00d2\3\2\2"+
		"\2=\u00d4\3\2\2\2?\u00d6\3\2\2\2A\u00d8\3\2\2\2C\u00da\3\2\2\2E\u00dc"+
		"\3\2\2\2G\u00de\3\2\2\2I\u00e0\3\2\2\2K\u00e2\3\2\2\2M\u00e4\3\2\2\2O"+
		"\u00e6\3\2\2\2Q\u00e8\3\2\2\2S\u00ea\3\2\2\2U\u00ec\3\2\2\2W\u00ee\3\2"+
		"\2\2Y\u00f0\3\2\2\2[\u00f2\3\2\2\2]\u00f4\3\2\2\2_\u00f6\3\2\2\2a\u00f8"+
		"\3\2\2\2c\u00fa\3\2\2\2e\u00fc\3\2\2\2gh\7*\2\2h\4\3\2\2\2ij\7+\2\2j\6"+
		"\3\2\2\2kl\7]\2\2l\b\3\2\2\2mn\7.\2\2n\n\3\2\2\2op\7_\2\2p\f\3\2\2\2q"+
		"r\5M\'\2rs\5S*\2s\16\3\2\2\2tu\5\61\31\2uv\5K&\2vw\5\67\34\2w\20\3\2\2"+
		"\2xy\5K&\2yz\5M\'\2z{\5W,\2{\22\3\2\2\2|}\7?\2\2}\24\3\2\2\2~\177\7#\2"+
		"\2\177\u0080\7?\2\2\u0080\26\3\2\2\2\u0081\u0082\7@\2\2\u0082\30\3\2\2"+
		"\2\u0083\u0084\7@\2\2\u0084\u0085\7?\2\2\u0085\32\3\2\2\2\u0086\u0087"+
		"\7>\2\2\u0087\34\3\2\2\2\u0088\u0089\7>\2\2\u0089\u008a\7?\2\2\u008a\36"+
		"\3\2\2\2\u008b\u008c\5A!\2\u008c\u008d\5K&\2\u008d \3\2\2\2\u008e\u0091"+
		"\5#\22\2\u008f\u0090\7\60\2\2\u0090\u0092\5#\22\2\u0091\u008f\3\2\2\2"+
		"\u0091\u0092\3\2\2\2\u0092\"\3\2\2\2\u0093\u0095\t\2\2\2\u0094\u0093\3"+
		"\2\2\2\u0095\u0096\3\2\2\2\u0096\u0094\3\2\2\2\u0096\u0097\3\2\2\2\u0097"+
		"$\3\2\2\2\u0098\u009c\t\3\2\2\u0099\u009b\t\4\2\2\u009a\u0099\3\2\2\2"+
		"\u009b\u009e\3\2\2\2\u009c\u009a\3\2\2\2\u009c\u009d\3\2\2\2\u009d&\3"+
		"\2\2\2\u009e\u009c\3\2\2\2\u009f\u00a4\7)\2\2\u00a0\u00a3\5+\26\2\u00a1"+
		"\u00a3\n\5\2\2\u00a2\u00a0\3\2\2\2\u00a2\u00a1\3\2\2\2\u00a3\u00a6\3\2"+
		"\2\2\u00a4\u00a2\3\2\2\2\u00a4\u00a5\3\2\2\2\u00a5\u00a7\3\2\2\2\u00a6"+
		"\u00a4\3\2\2\2\u00a7\u00b2\7)\2\2\u00a8\u00ad\7$\2\2\u00a9\u00ac\5+\26"+
		"\2\u00aa\u00ac\n\6\2\2\u00ab\u00a9\3\2\2\2\u00ab\u00aa\3\2\2\2\u00ac\u00af"+
		"\3\2\2\2\u00ad\u00ab\3\2\2\2\u00ad\u00ae\3\2\2\2\u00ae\u00b0\3\2\2\2\u00af"+
		"\u00ad\3\2\2\2\u00b0\u00b2\7$\2\2\u00b1\u009f\3\2\2\2\u00b1\u00a8\3\2"+
		"\2\2\u00b2(\3\2\2\2\u00b3\u00b5\t\7\2\2\u00b4\u00b3\3\2\2\2\u00b5\u00b6"+
		"\3\2\2\2\u00b6\u00b4\3\2\2\2\u00b6\u00b7\3\2\2\2\u00b7\u00b8\3\2\2\2\u00b8"+
		"\u00b9\b\25\2\2\u00b9*\3\2\2\2\u00ba\u00be\7^\2\2\u00bb\u00bf\t\b\2\2"+
		"\u00bc\u00bf\5-\27\2\u00bd\u00bf\5e\63\2\u00be\u00bb\3\2\2\2\u00be\u00bc"+
		"\3\2\2\2\u00be\u00bd\3\2\2\2\u00bf,\3\2\2\2\u00c0\u00c1\7w\2\2\u00c1\u00c2"+
		"\5/\30\2\u00c2\u00c3\5/\30\2\u00c3\u00c4\5/\30\2\u00c4\u00c5\5/\30\2\u00c5"+
		".\3\2\2\2\u00c6\u00c7\t\t\2\2\u00c7\60\3\2\2\2\u00c8\u00c9\t\n\2\2\u00c9"+
		"\62\3\2\2\2\u00ca\u00cb\t\13\2\2\u00cb\64\3\2\2\2\u00cc\u00cd\t\f\2\2"+
		"\u00cd\66\3\2\2\2\u00ce\u00cf\t\r\2\2\u00cf8\3\2\2\2\u00d0\u00d1\t\16"+
		"\2\2\u00d1:\3\2\2\2\u00d2\u00d3\t\17\2\2\u00d3<\3\2\2\2\u00d4\u00d5\t"+
		"\20\2\2\u00d5>\3\2\2\2\u00d6\u00d7\t\21\2\2\u00d7@\3\2\2\2\u00d8\u00d9"+
		"\t\22\2\2\u00d9B\3\2\2\2\u00da\u00db\t\23\2\2\u00dbD\3\2\2\2\u00dc\u00dd"+
		"\t\24\2\2\u00ddF\3\2\2\2\u00de\u00df\t\25\2\2\u00dfH\3\2\2\2\u00e0\u00e1"+
		"\t\26\2\2\u00e1J\3\2\2\2\u00e2\u00e3\t\27\2\2\u00e3L\3\2\2\2\u00e4\u00e5"+
		"\t\30\2\2\u00e5N\3\2\2\2\u00e6\u00e7\t\31\2\2\u00e7P\3\2\2\2\u00e8\u00e9"+
		"\t\32\2\2\u00e9R\3\2\2\2\u00ea\u00eb\t\33\2\2\u00ebT\3\2\2\2\u00ec\u00ed"+
		"\t\34\2\2\u00edV\3\2\2\2\u00ee\u00ef\t\35\2\2\u00efX\3\2\2\2\u00f0\u00f1"+
		"\t\36\2\2\u00f1Z\3\2\2\2\u00f2\u00f3\t\37\2\2\u00f3\\\3\2\2\2\u00f4\u00f5"+
		"\t \2\2\u00f5^\3\2\2\2\u00f6\u00f7\t!\2\2\u00f7`\3\2\2\2\u00f8\u00f9\t"+
		"\"\2\2\u00f9b\3\2\2\2\u00fa\u00fb\t#\2\2\u00fbd\3\2\2\2\u00fc\u00fd\7"+
		"\u0080\2\2\u00fdf\3\2\2\2\r\2\u0091\u0096\u009c\u00a2\u00a4\u00ab\u00ad"+
		"\u00b1\u00b6\u00be\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}