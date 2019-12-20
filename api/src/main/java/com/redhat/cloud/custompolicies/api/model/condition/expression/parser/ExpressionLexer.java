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
		GT=11, GTE=12, LT=13, LTE=14, IN=15, NUMBER=16, FLOAT=17, INTEGER=18, 
		SIMPLETEXT=19, STRING=20, WS=21;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "OR", "AND", "NOT", "EQUAL", 
			"NOTEQUAL", "GT", "GTE", "LT", "LTE", "IN", "NUMBER", "FLOAT", "INTEGER", 
			"SIMPLETEXT", "STRING", "WS", "ESC", "UNICODE", "HEX", "A", "B", "C", 
			"D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", 
			"R", "S", "T", "U", "V", "W", "X", "Y", "Z", "NEG_OP"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\27\u0104\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b"+
		"\3\b\3\t\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\r\3\r\3\r\3\16\3"+
		"\16\3\17\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3\21\5\21\u0094\n\21\3\22"+
		"\3\22\3\22\3\22\3\23\6\23\u009b\n\23\r\23\16\23\u009c\3\24\3\24\7\24\u00a1"+
		"\n\24\f\24\16\24\u00a4\13\24\3\25\3\25\3\25\7\25\u00a9\n\25\f\25\16\25"+
		"\u00ac\13\25\3\25\3\25\3\25\3\25\7\25\u00b2\n\25\f\25\16\25\u00b5\13\25"+
		"\3\25\5\25\u00b8\n\25\3\26\6\26\u00bb\n\26\r\26\16\26\u00bc\3\26\3\26"+
		"\3\27\3\27\3\27\3\27\5\27\u00c5\n\27\3\30\3\30\3\30\3\30\3\30\3\30\3\31"+
		"\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3"+
		" \3!\3!\3\"\3\"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3"+
		"+\3,\3,\3-\3-\3.\3.\3/\3/\3\60\3\60\3\61\3\61\3\62\3\62\3\63\3\63\3\64"+
		"\3\64\2\2\65\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16"+
		"\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\2/\2\61\2\63\2\65\2\67\2"+
		"9\2;\2=\2?\2A\2C\2E\2G\2I\2K\2M\2O\2Q\2S\2U\2W\2Y\2[\2]\2_\2a\2c\2e\2"+
		"g\2\3\2$\3\2\62;\7\2\60\60\62;C\\aac|\7\2/\60\62;C\\aac|\4\2))^^\4\2$"+
		"$^^\5\2\13\f\17\17\"\"\n\2))\61\61^^ddhhppttvv\5\2\62;CHch\4\2CCcc\4\2"+
		"DDdd\4\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4\2LLll\4"+
		"\2MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2TTtt\4\2UUu"+
		"u\4\2VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\2\u00f0\2"+
		"\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2"+
		"\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2"+
		"\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2"+
		"\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\3i\3\2\2\2\5k\3\2\2\2"+
		"\7m\3\2\2\2\to\3\2\2\2\13q\3\2\2\2\rs\3\2\2\2\17v\3\2\2\2\21z\3\2\2\2"+
		"\23~\3\2\2\2\25\u0080\3\2\2\2\27\u0083\3\2\2\2\31\u0085\3\2\2\2\33\u0088"+
		"\3\2\2\2\35\u008a\3\2\2\2\37\u008d\3\2\2\2!\u0090\3\2\2\2#\u0095\3\2\2"+
		"\2%\u009a\3\2\2\2\'\u009e\3\2\2\2)\u00b7\3\2\2\2+\u00ba\3\2\2\2-\u00c0"+
		"\3\2\2\2/\u00c6\3\2\2\2\61\u00cc\3\2\2\2\63\u00ce\3\2\2\2\65\u00d0\3\2"+
		"\2\2\67\u00d2\3\2\2\29\u00d4\3\2\2\2;\u00d6\3\2\2\2=\u00d8\3\2\2\2?\u00da"+
		"\3\2\2\2A\u00dc\3\2\2\2C\u00de\3\2\2\2E\u00e0\3\2\2\2G\u00e2\3\2\2\2I"+
		"\u00e4\3\2\2\2K\u00e6\3\2\2\2M\u00e8\3\2\2\2O\u00ea\3\2\2\2Q\u00ec\3\2"+
		"\2\2S\u00ee\3\2\2\2U\u00f0\3\2\2\2W\u00f2\3\2\2\2Y\u00f4\3\2\2\2[\u00f6"+
		"\3\2\2\2]\u00f8\3\2\2\2_\u00fa\3\2\2\2a\u00fc\3\2\2\2c\u00fe\3\2\2\2e"+
		"\u0100\3\2\2\2g\u0102\3\2\2\2ij\7*\2\2j\4\3\2\2\2kl\7+\2\2l\6\3\2\2\2"+
		"mn\7]\2\2n\b\3\2\2\2op\7.\2\2p\n\3\2\2\2qr\7_\2\2r\f\3\2\2\2st\5O(\2t"+
		"u\5U+\2u\16\3\2\2\2vw\5\63\32\2wx\5M\'\2xy\59\35\2y\20\3\2\2\2z{\5M\'"+
		"\2{|\5O(\2|}\5Y-\2}\22\3\2\2\2~\177\7?\2\2\177\24\3\2\2\2\u0080\u0081"+
		"\7#\2\2\u0081\u0082\7?\2\2\u0082\26\3\2\2\2\u0083\u0084\7@\2\2\u0084\30"+
		"\3\2\2\2\u0085\u0086\7@\2\2\u0086\u0087\7?\2\2\u0087\32\3\2\2\2\u0088"+
		"\u0089\7>\2\2\u0089\34\3\2\2\2\u008a\u008b\7>\2\2\u008b\u008c\7?\2\2\u008c"+
		"\36\3\2\2\2\u008d\u008e\5C\"\2\u008e\u008f\5M\'\2\u008f \3\2\2\2\u0090"+
		"\u0093\5%\23\2\u0091\u0092\7\60\2\2\u0092\u0094\5%\23\2\u0093\u0091\3"+
		"\2\2\2\u0093\u0094\3\2\2\2\u0094\"\3\2\2\2\u0095\u0096\5%\23\2\u0096\u0097"+
		"\7\60\2\2\u0097\u0098\5%\23\2\u0098$\3\2\2\2\u0099\u009b\t\2\2\2\u009a"+
		"\u0099\3\2\2\2\u009b\u009c\3\2\2\2\u009c\u009a\3\2\2\2\u009c\u009d\3\2"+
		"\2\2\u009d&\3\2\2\2\u009e\u00a2\t\3\2\2\u009f\u00a1\t\4\2\2\u00a0\u009f"+
		"\3\2\2\2\u00a1\u00a4\3\2\2\2\u00a2\u00a0\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3"+
		"(\3\2\2\2\u00a4\u00a2\3\2\2\2\u00a5\u00aa\7)\2\2\u00a6\u00a9\5-\27\2\u00a7"+
		"\u00a9\n\5\2\2\u00a8\u00a6\3\2\2\2\u00a8\u00a7\3\2\2\2\u00a9\u00ac\3\2"+
		"\2\2\u00aa\u00a8\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab\u00ad\3\2\2\2\u00ac"+
		"\u00aa\3\2\2\2\u00ad\u00b8\7)\2\2\u00ae\u00b3\7$\2\2\u00af\u00b2\5-\27"+
		"\2\u00b0\u00b2\n\6\2\2\u00b1\u00af\3\2\2\2\u00b1\u00b0\3\2\2\2\u00b2\u00b5"+
		"\3\2\2\2\u00b3\u00b1\3\2\2\2\u00b3\u00b4\3\2\2\2\u00b4\u00b6\3\2\2\2\u00b5"+
		"\u00b3\3\2\2\2\u00b6\u00b8\7$\2\2\u00b7\u00a5\3\2\2\2\u00b7\u00ae\3\2"+
		"\2\2\u00b8*\3\2\2\2\u00b9\u00bb\t\7\2\2\u00ba\u00b9\3\2\2\2\u00bb\u00bc"+
		"\3\2\2\2\u00bc\u00ba\3\2\2\2\u00bc\u00bd\3\2\2\2\u00bd\u00be\3\2\2\2\u00be"+
		"\u00bf\b\26\2\2\u00bf,\3\2\2\2\u00c0\u00c4\7^\2\2\u00c1\u00c5\t\b\2\2"+
		"\u00c2\u00c5\5/\30\2\u00c3\u00c5\5g\64\2\u00c4\u00c1\3\2\2\2\u00c4\u00c2"+
		"\3\2\2\2\u00c4\u00c3\3\2\2\2\u00c5.\3\2\2\2\u00c6\u00c7\7w\2\2\u00c7\u00c8"+
		"\5\61\31\2\u00c8\u00c9\5\61\31\2\u00c9\u00ca\5\61\31\2\u00ca\u00cb\5\61"+
		"\31\2\u00cb\60\3\2\2\2\u00cc\u00cd\t\t\2\2\u00cd\62\3\2\2\2\u00ce\u00cf"+
		"\t\n\2\2\u00cf\64\3\2\2\2\u00d0\u00d1\t\13\2\2\u00d1\66\3\2\2\2\u00d2"+
		"\u00d3\t\f\2\2\u00d38\3\2\2\2\u00d4\u00d5\t\r\2\2\u00d5:\3\2\2\2\u00d6"+
		"\u00d7\t\16\2\2\u00d7<\3\2\2\2\u00d8\u00d9\t\17\2\2\u00d9>\3\2\2\2\u00da"+
		"\u00db\t\20\2\2\u00db@\3\2\2\2\u00dc\u00dd\t\21\2\2\u00ddB\3\2\2\2\u00de"+
		"\u00df\t\22\2\2\u00dfD\3\2\2\2\u00e0\u00e1\t\23\2\2\u00e1F\3\2\2\2\u00e2"+
		"\u00e3\t\24\2\2\u00e3H\3\2\2\2\u00e4\u00e5\t\25\2\2\u00e5J\3\2\2\2\u00e6"+
		"\u00e7\t\26\2\2\u00e7L\3\2\2\2\u00e8\u00e9\t\27\2\2\u00e9N\3\2\2\2\u00ea"+
		"\u00eb\t\30\2\2\u00ebP\3\2\2\2\u00ec\u00ed\t\31\2\2\u00edR\3\2\2\2\u00ee"+
		"\u00ef\t\32\2\2\u00efT\3\2\2\2\u00f0\u00f1\t\33\2\2\u00f1V\3\2\2\2\u00f2"+
		"\u00f3\t\34\2\2\u00f3X\3\2\2\2\u00f4\u00f5\t\35\2\2\u00f5Z\3\2\2\2\u00f6"+
		"\u00f7\t\36\2\2\u00f7\\\3\2\2\2\u00f8\u00f9\t\37\2\2\u00f9^\3\2\2\2\u00fa"+
		"\u00fb\t \2\2\u00fb`\3\2\2\2\u00fc\u00fd\t!\2\2\u00fdb\3\2\2\2\u00fe\u00ff"+
		"\t\"\2\2\u00ffd\3\2\2\2\u0100\u0101\t#\2\2\u0101f\3\2\2\2\u0102\u0103"+
		"\7#\2\2\u0103h\3\2\2\2\r\2\u0093\u009c\u00a2\u00a8\u00aa\u00b1\u00b3\u00b7"+
		"\u00bc\u00c4\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}