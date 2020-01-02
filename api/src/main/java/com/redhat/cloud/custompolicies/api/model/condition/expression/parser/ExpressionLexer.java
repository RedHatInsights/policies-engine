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
		CONTAINS=11, GT=12, GTE=13, LT=14, LTE=15, IN=16, NUMBER=17, FLOAT=18, 
		INTEGER=19, SIMPLETEXT=20, STRING=21, WS=22;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "OR", "AND", "NOT", "EQUAL", 
			"NOTEQUAL", "CONTAINS", "GT", "GTE", "LT", "LTE", "IN", "NUMBER", "FLOAT", 
			"INTEGER", "SIMPLETEXT", "STRING", "WS", "ESC", "UNICODE", "HEX", "A", 
			"B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", 
			"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "NEG_OP"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'['", "','", "']'", null, null, null, "'='", "'!='", 
			null, "'>'", "'>='", "'<'", "'<='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, "OR", "AND", "NOT", "EQUAL", "NOTEQUAL", 
			"CONTAINS", "GT", "GTE", "LT", "LTE", "IN", "NUMBER", "FLOAT", "INTEGER", 
			"SIMPLETEXT", "STRING", "WS"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\30\u010f\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\4\65\t\65\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\7\3"+
		"\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f"+
		"\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\20\3\20\3\20\3"+
		"\21\3\21\3\21\3\22\3\22\3\22\5\22\u009f\n\22\3\23\3\23\3\23\3\23\3\24"+
		"\6\24\u00a6\n\24\r\24\16\24\u00a7\3\25\3\25\7\25\u00ac\n\25\f\25\16\25"+
		"\u00af\13\25\3\26\3\26\3\26\7\26\u00b4\n\26\f\26\16\26\u00b7\13\26\3\26"+
		"\3\26\3\26\3\26\7\26\u00bd\n\26\f\26\16\26\u00c0\13\26\3\26\5\26\u00c3"+
		"\n\26\3\27\6\27\u00c6\n\27\r\27\16\27\u00c7\3\27\3\27\3\30\3\30\3\30\3"+
		"\30\5\30\u00d0\n\30\3\31\3\31\3\31\3\31\3\31\3\31\3\32\3\32\3\33\3\33"+
		"\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$"+
		"\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3.\3.\3/\3"+
		"/\3\60\3\60\3\61\3\61\3\62\3\62\3\63\3\63\3\64\3\64\3\65\3\65\2\2\66\3"+
		"\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37"+
		"\21!\22#\23%\24\'\25)\26+\27-\30/\2\61\2\63\2\65\2\67\29\2;\2=\2?\2A\2"+
		"C\2E\2G\2I\2K\2M\2O\2Q\2S\2U\2W\2Y\2[\2]\2_\2a\2c\2e\2g\2i\2\3\2$\3\2"+
		"\62;\7\2\60\60\62;C\\aac|\7\2/\60\62;C\\aac|\4\2))^^\4\2$$^^\5\2\13\f"+
		"\17\17\"\"\n\2))\61\61^^ddhhppttvv\5\2\62;CHch\4\2CCcc\4\2DDdd\4\2EEe"+
		"e\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4\2LLll\4\2MMmm\4\2"+
		"NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2TTtt\4\2UUuu\4\2VVvv\4"+
		"\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\2\u00fb\2\3\3\2\2\2\2"+
		"\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2"+
		"\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2"+
		"\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2"+
		"\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\3k\3\2\2\2\5m\3\2\2\2\7"+
		"o\3\2\2\2\tq\3\2\2\2\13s\3\2\2\2\ru\3\2\2\2\17x\3\2\2\2\21|\3\2\2\2\23"+
		"\u0080\3\2\2\2\25\u0082\3\2\2\2\27\u0085\3\2\2\2\31\u008e\3\2\2\2\33\u0090"+
		"\3\2\2\2\35\u0093\3\2\2\2\37\u0095\3\2\2\2!\u0098\3\2\2\2#\u009b\3\2\2"+
		"\2%\u00a0\3\2\2\2\'\u00a5\3\2\2\2)\u00a9\3\2\2\2+\u00c2\3\2\2\2-\u00c5"+
		"\3\2\2\2/\u00cb\3\2\2\2\61\u00d1\3\2\2\2\63\u00d7\3\2\2\2\65\u00d9\3\2"+
		"\2\2\67\u00db\3\2\2\29\u00dd\3\2\2\2;\u00df\3\2\2\2=\u00e1\3\2\2\2?\u00e3"+
		"\3\2\2\2A\u00e5\3\2\2\2C\u00e7\3\2\2\2E\u00e9\3\2\2\2G\u00eb\3\2\2\2I"+
		"\u00ed\3\2\2\2K\u00ef\3\2\2\2M\u00f1\3\2\2\2O\u00f3\3\2\2\2Q\u00f5\3\2"+
		"\2\2S\u00f7\3\2\2\2U\u00f9\3\2\2\2W\u00fb\3\2\2\2Y\u00fd\3\2\2\2[\u00ff"+
		"\3\2\2\2]\u0101\3\2\2\2_\u0103\3\2\2\2a\u0105\3\2\2\2c\u0107\3\2\2\2e"+
		"\u0109\3\2\2\2g\u010b\3\2\2\2i\u010d\3\2\2\2kl\7*\2\2l\4\3\2\2\2mn\7+"+
		"\2\2n\6\3\2\2\2op\7]\2\2p\b\3\2\2\2qr\7.\2\2r\n\3\2\2\2st\7_\2\2t\f\3"+
		"\2\2\2uv\5Q)\2vw\5W,\2w\16\3\2\2\2xy\5\65\33\2yz\5O(\2z{\5;\36\2{\20\3"+
		"\2\2\2|}\5O(\2}~\5Q)\2~\177\5[.\2\177\22\3\2\2\2\u0080\u0081\7?\2\2\u0081"+
		"\24\3\2\2\2\u0082\u0083\7#\2\2\u0083\u0084\7?\2\2\u0084\26\3\2\2\2\u0085"+
		"\u0086\59\35\2\u0086\u0087\5Q)\2\u0087\u0088\5O(\2\u0088\u0089\5[.\2\u0089"+
		"\u008a\5\65\33\2\u008a\u008b\5E#\2\u008b\u008c\5O(\2\u008c\u008d\5Y-\2"+
		"\u008d\30\3\2\2\2\u008e\u008f\7@\2\2\u008f\32\3\2\2\2\u0090\u0091\7@\2"+
		"\2\u0091\u0092\7?\2\2\u0092\34\3\2\2\2\u0093\u0094\7>\2\2\u0094\36\3\2"+
		"\2\2\u0095\u0096\7>\2\2\u0096\u0097\7?\2\2\u0097 \3\2\2\2\u0098\u0099"+
		"\5E#\2\u0099\u009a\5O(\2\u009a\"\3\2\2\2\u009b\u009e\5\'\24\2\u009c\u009d"+
		"\7\60\2\2\u009d\u009f\5\'\24\2\u009e\u009c\3\2\2\2\u009e\u009f\3\2\2\2"+
		"\u009f$\3\2\2\2\u00a0\u00a1\5\'\24\2\u00a1\u00a2\7\60\2\2\u00a2\u00a3"+
		"\5\'\24\2\u00a3&\3\2\2\2\u00a4\u00a6\t\2\2\2\u00a5\u00a4\3\2\2\2\u00a6"+
		"\u00a7\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a7\u00a8\3\2\2\2\u00a8(\3\2\2\2"+
		"\u00a9\u00ad\t\3\2\2\u00aa\u00ac\t\4\2\2\u00ab\u00aa\3\2\2\2\u00ac\u00af"+
		"\3\2\2\2\u00ad\u00ab\3\2\2\2\u00ad\u00ae\3\2\2\2\u00ae*\3\2\2\2\u00af"+
		"\u00ad\3\2\2\2\u00b0\u00b5\7)\2\2\u00b1\u00b4\5/\30\2\u00b2\u00b4\n\5"+
		"\2\2\u00b3\u00b1\3\2\2\2\u00b3\u00b2\3\2\2\2\u00b4\u00b7\3\2\2\2\u00b5"+
		"\u00b3\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6\u00b8\3\2\2\2\u00b7\u00b5\3\2"+
		"\2\2\u00b8\u00c3\7)\2\2\u00b9\u00be\7$\2\2\u00ba\u00bd\5/\30\2\u00bb\u00bd"+
		"\n\6\2\2\u00bc\u00ba\3\2\2\2\u00bc\u00bb\3\2\2\2\u00bd\u00c0\3\2\2\2\u00be"+
		"\u00bc\3\2\2\2\u00be\u00bf\3\2\2\2\u00bf\u00c1\3\2\2\2\u00c0\u00be\3\2"+
		"\2\2\u00c1\u00c3\7$\2\2\u00c2\u00b0\3\2\2\2\u00c2\u00b9\3\2\2\2\u00c3"+
		",\3\2\2\2\u00c4\u00c6\t\7\2\2\u00c5\u00c4\3\2\2\2\u00c6\u00c7\3\2\2\2"+
		"\u00c7\u00c5\3\2\2\2\u00c7\u00c8\3\2\2\2\u00c8\u00c9\3\2\2\2\u00c9\u00ca"+
		"\b\27\2\2\u00ca.\3\2\2\2\u00cb\u00cf\7^\2\2\u00cc\u00d0\t\b\2\2\u00cd"+
		"\u00d0\5\61\31\2\u00ce\u00d0\5i\65\2\u00cf\u00cc\3\2\2\2\u00cf\u00cd\3"+
		"\2\2\2\u00cf\u00ce\3\2\2\2\u00d0\60\3\2\2\2\u00d1\u00d2\7w\2\2\u00d2\u00d3"+
		"\5\63\32\2\u00d3\u00d4\5\63\32\2\u00d4\u00d5\5\63\32\2\u00d5\u00d6\5\63"+
		"\32\2\u00d6\62\3\2\2\2\u00d7\u00d8\t\t\2\2\u00d8\64\3\2\2\2\u00d9\u00da"+
		"\t\n\2\2\u00da\66\3\2\2\2\u00db\u00dc\t\13\2\2\u00dc8\3\2\2\2\u00dd\u00de"+
		"\t\f\2\2\u00de:\3\2\2\2\u00df\u00e0\t\r\2\2\u00e0<\3\2\2\2\u00e1\u00e2"+
		"\t\16\2\2\u00e2>\3\2\2\2\u00e3\u00e4\t\17\2\2\u00e4@\3\2\2\2\u00e5\u00e6"+
		"\t\20\2\2\u00e6B\3\2\2\2\u00e7\u00e8\t\21\2\2\u00e8D\3\2\2\2\u00e9\u00ea"+
		"\t\22\2\2\u00eaF\3\2\2\2\u00eb\u00ec\t\23\2\2\u00ecH\3\2\2\2\u00ed\u00ee"+
		"\t\24\2\2\u00eeJ\3\2\2\2\u00ef\u00f0\t\25\2\2\u00f0L\3\2\2\2\u00f1\u00f2"+
		"\t\26\2\2\u00f2N\3\2\2\2\u00f3\u00f4\t\27\2\2\u00f4P\3\2\2\2\u00f5\u00f6"+
		"\t\30\2\2\u00f6R\3\2\2\2\u00f7\u00f8\t\31\2\2\u00f8T\3\2\2\2\u00f9\u00fa"+
		"\t\32\2\2\u00faV\3\2\2\2\u00fb\u00fc\t\33\2\2\u00fcX\3\2\2\2\u00fd\u00fe"+
		"\t\34\2\2\u00feZ\3\2\2\2\u00ff\u0100\t\35\2\2\u0100\\\3\2\2\2\u0101\u0102"+
		"\t\36\2\2\u0102^\3\2\2\2\u0103\u0104\t\37\2\2\u0104`\3\2\2\2\u0105\u0106"+
		"\t \2\2\u0106b\3\2\2\2\u0107\u0108\t!\2\2\u0108d\3\2\2\2\u0109\u010a\t"+
		"\"\2\2\u010af\3\2\2\2\u010b\u010c\t#\2\2\u010ch\3\2\2\2\u010d\u010e\7"+
		"#\2\2\u010ej\3\2\2\2\r\2\u009e\u00a7\u00ad\u00b3\u00b5\u00bc\u00be\u00c2"+
		"\u00c7\u00cf\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}