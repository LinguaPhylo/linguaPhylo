// Generated from java/lphy/parser/LPhy.g4 by ANTLR 4.12.0
package lphy.core.parser.antlr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class LPhyParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.12.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, DATA=30, MODEL=31, ASSIGN=32, 
		TILDE=33, DOT=34, NAME=35, LENGTH=36, DIM=37, DECIMAL_LITERAL=38, HEX_LITERAL=39, 
		OCT_LITERAL=40, BINARY_LITERAL=41, FLOAT_LITERAL=42, HEX_FLOAT_LITERAL=43, 
		STRING_LITERAL=44, WS=45, COMMENT=46, LINE_COMMENT=47;
	public static final int
		RULE_input = 0, RULE_structured_input = 1, RULE_free_lines = 2, RULE_datablock = 3, 
		RULE_determ_relation_list = 4, RULE_determ_relation_line = 5, RULE_modelblock = 6, 
		RULE_relation_list = 7, RULE_relation = 8, RULE_var = 9, RULE_range_list = 10, 
		RULE_determ_relation = 11, RULE_stoch_relation = 12, RULE_constant = 13, 
		RULE_expression_list = 14, RULE_unnamed_expression_list = 15, RULE_mapFunction = 16, 
		RULE_methodCall = 17, RULE_objectMethodCall = 18, RULE_distribution = 19, 
		RULE_named_expression = 20, RULE_array_expression = 21, RULE_expression = 22;
	private static String[] makeRuleNames() {
		return new String[] {
			"input", "structured_input", "free_lines", "datablock", "determ_relation_list", 
			"determ_relation_line", "modelblock", "relation_list", "relation", "var", 
			"range_list", "determ_relation", "stoch_relation", "constant", "expression_list", 
			"unnamed_expression_list", "mapFunction", "methodCall", "objectMethodCall", 
			"distribution", "named_expression", "array_expression", "expression"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "';'", "'['", "']'", "','", "'-'", "'true'", "'false'", 
			"'('", "')'", "'+'", "'!'", "'**'", "'*'", "'/'", "'%'", "'<='", "'>='", 
			"'>'", "'<'", "'=='", "'!='", "'&&'", "'||'", "':'", "'&'", "'|'", "'^'", 
			"'data'", "'model'", "'='", "'~'", "'.'", null, "'length'", "'dim'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, "DATA", "MODEL", "ASSIGN", "TILDE", 
			"DOT", "NAME", "LENGTH", "DIM", "DECIMAL_LITERAL", "HEX_LITERAL", "OCT_LITERAL", 
			"BINARY_LITERAL", "FLOAT_LITERAL", "HEX_FLOAT_LITERAL", "STRING_LITERAL", 
			"WS", "COMMENT", "LINE_COMMENT"
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
	public String getGrammarFileName() { return "LPhy.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public LPhyParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InputContext extends ParserRuleContext {
		public Structured_inputContext structured_input() {
			return getRuleContext(Structured_inputContext.class,0);
		}
		public Free_linesContext free_lines() {
			return getRuleContext(Free_linesContext.class,0);
		}
		public InputContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_input; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterInput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitInput(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitInput(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InputContext input() throws RecognitionException {
		InputContext _localctx = new InputContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_input);
		try {
			setState(48);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(46);
				structured_input();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(47);
				free_lines();
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

	@SuppressWarnings("CheckReturnValue")
	public static class Structured_inputContext extends ParserRuleContext {
		public DatablockContext datablock() {
			return getRuleContext(DatablockContext.class,0);
		}
		public ModelblockContext modelblock() {
			return getRuleContext(ModelblockContext.class,0);
		}
		public Structured_inputContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structured_input; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterStructured_input(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitStructured_input(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitStructured_input(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Structured_inputContext structured_input() throws RecognitionException {
		Structured_inputContext _localctx = new Structured_inputContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_structured_input);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(51);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(50);
				datablock();
				}
				break;
			}
			setState(54);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MODEL || _la==NAME) {
				{
				setState(53);
				modelblock();
				}
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

	@SuppressWarnings("CheckReturnValue")
	public static class Free_linesContext extends ParserRuleContext {
		public Relation_listContext relation_list() {
			return getRuleContext(Relation_listContext.class,0);
		}
		public Free_linesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_free_lines; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterFree_lines(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitFree_lines(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitFree_lines(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Free_linesContext free_lines() throws RecognitionException {
		Free_linesContext _localctx = new Free_linesContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_free_lines);
		try {
			setState(58);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EOF:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case NAME:
				enterOuterAlt(_localctx, 2);
				{
				setState(57);
				relation_list(0);
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

	@SuppressWarnings("CheckReturnValue")
	public static class DatablockContext extends ParserRuleContext {
		public TerminalNode DATA() { return getToken(LPhyParser.DATA, 0); }
		public Determ_relation_listContext determ_relation_list() {
			return getRuleContext(Determ_relation_listContext.class,0);
		}
		public DatablockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_datablock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterDatablock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitDatablock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitDatablock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DatablockContext datablock() throws RecognitionException {
		DatablockContext _localctx = new DatablockContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_datablock);
		int _la;
		try {
			setState(67);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DATA:
				enterOuterAlt(_localctx, 1);
				{
				setState(60);
				match(DATA);
				setState(61);
				match(T__0);
				setState(63);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NAME) {
					{
					setState(62);
					determ_relation_list(0);
					}
				}

				setState(65);
				match(T__1);
				}
				break;
			case NAME:
				enterOuterAlt(_localctx, 2);
				{
				setState(66);
				determ_relation_list(0);
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

	@SuppressWarnings("CheckReturnValue")
	public static class Determ_relation_listContext extends ParserRuleContext {
		public Determ_relation_lineContext determ_relation_line() {
			return getRuleContext(Determ_relation_lineContext.class,0);
		}
		public Determ_relation_listContext determ_relation_list() {
			return getRuleContext(Determ_relation_listContext.class,0);
		}
		public Determ_relation_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_determ_relation_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterDeterm_relation_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitDeterm_relation_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitDeterm_relation_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Determ_relation_listContext determ_relation_list() throws RecognitionException {
		return determ_relation_list(0);
	}

	private Determ_relation_listContext determ_relation_list(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Determ_relation_listContext _localctx = new Determ_relation_listContext(_ctx, _parentState);
		Determ_relation_listContext _prevctx = _localctx;
		int _startState = 8;
		enterRecursionRule(_localctx, 8, RULE_determ_relation_list, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(70);
			determ_relation_line();
			}
			_ctx.stop = _input.LT(-1);
			setState(76);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Determ_relation_listContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_determ_relation_list);
					setState(72);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(73);
					determ_relation_line();
					}
					} 
				}
				setState(78);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
	public static class Determ_relation_lineContext extends ParserRuleContext {
		public Determ_relationContext determ_relation() {
			return getRuleContext(Determ_relationContext.class,0);
		}
		public Determ_relation_lineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_determ_relation_line; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterDeterm_relation_line(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitDeterm_relation_line(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitDeterm_relation_line(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Determ_relation_lineContext determ_relation_line() throws RecognitionException {
		Determ_relation_lineContext _localctx = new Determ_relation_lineContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_determ_relation_line);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			determ_relation();
			setState(80);
			match(T__2);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ModelblockContext extends ParserRuleContext {
		public TerminalNode MODEL() { return getToken(LPhyParser.MODEL, 0); }
		public Relation_listContext relation_list() {
			return getRuleContext(Relation_listContext.class,0);
		}
		public ModelblockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modelblock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterModelblock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitModelblock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitModelblock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModelblockContext modelblock() throws RecognitionException {
		ModelblockContext _localctx = new ModelblockContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_modelblock);
		int _la;
		try {
			setState(89);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MODEL:
				enterOuterAlt(_localctx, 1);
				{
				setState(82);
				match(MODEL);
				setState(83);
				match(T__0);
				setState(85);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NAME) {
					{
					setState(84);
					relation_list(0);
					}
				}

				setState(87);
				match(T__1);
				}
				break;
			case NAME:
				enterOuterAlt(_localctx, 2);
				{
				setState(88);
				relation_list(0);
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

	@SuppressWarnings("CheckReturnValue")
	public static class Relation_listContext extends ParserRuleContext {
		public RelationContext relation() {
			return getRuleContext(RelationContext.class,0);
		}
		public Relation_listContext relation_list() {
			return getRuleContext(Relation_listContext.class,0);
		}
		public Relation_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relation_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterRelation_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitRelation_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitRelation_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Relation_listContext relation_list() throws RecognitionException {
		return relation_list(0);
	}

	private Relation_listContext relation_list(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Relation_listContext _localctx = new Relation_listContext(_ctx, _parentState);
		Relation_listContext _prevctx = _localctx;
		int _startState = 14;
		enterRecursionRule(_localctx, 14, RULE_relation_list, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(92);
			relation();
			}
			_ctx.stop = _input.LT(-1);
			setState(98);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Relation_listContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_relation_list);
					setState(94);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(95);
					relation();
					}
					} 
				}
				setState(100);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
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

	@SuppressWarnings("CheckReturnValue")
	public static class RelationContext extends ParserRuleContext {
		public Stoch_relationContext stoch_relation() {
			return getRuleContext(Stoch_relationContext.class,0);
		}
		public Determ_relationContext determ_relation() {
			return getRuleContext(Determ_relationContext.class,0);
		}
		public RelationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitRelation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationContext relation() throws RecognitionException {
		RelationContext _localctx = new RelationContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_relation);
		try {
			setState(107);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(101);
				stoch_relation();
				setState(102);
				match(T__2);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(104);
				determ_relation();
				setState(105);
				match(T__2);
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

	@SuppressWarnings("CheckReturnValue")
	public static class VarContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(LPhyParser.NAME, 0); }
		public Range_listContext range_list() {
			return getRuleContext(Range_listContext.class,0);
		}
		public VarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitVar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitVar(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarContext var() throws RecognitionException {
		VarContext _localctx = new VarContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_var);
		try {
			setState(115);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(109);
				match(NAME);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(110);
				match(NAME);
				setState(111);
				match(T__3);
				setState(112);
				range_list();
				setState(113);
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

	@SuppressWarnings("CheckReturnValue")
	public static class Range_listContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public Range_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_range_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterRange_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitRange_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitRange_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Range_listContext range_list() throws RecognitionException {
		Range_listContext _localctx = new Range_listContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_range_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			expression(0);
			setState(122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__5) {
				{
				{
				setState(118);
				match(T__5);
				setState(119);
				expression(0);
				}
				}
				setState(124);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class Determ_relationContext extends ParserRuleContext {
		public VarContext var() {
			return getRuleContext(VarContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(LPhyParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Determ_relationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_determ_relation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterDeterm_relation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitDeterm_relation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitDeterm_relation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Determ_relationContext determ_relation() throws RecognitionException {
		Determ_relationContext _localctx = new Determ_relationContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_determ_relation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			var();
			setState(126);
			match(ASSIGN);
			setState(127);
			expression(0);
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

	@SuppressWarnings("CheckReturnValue")
	public static class Stoch_relationContext extends ParserRuleContext {
		public VarContext var() {
			return getRuleContext(VarContext.class,0);
		}
		public TerminalNode TILDE() { return getToken(LPhyParser.TILDE, 0); }
		public DistributionContext distribution() {
			return getRuleContext(DistributionContext.class,0);
		}
		public Stoch_relationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stoch_relation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterStoch_relation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitStoch_relation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitStoch_relation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Stoch_relationContext stoch_relation() throws RecognitionException {
		Stoch_relationContext _localctx = new Stoch_relationContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_stoch_relation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			var();
			setState(130);
			match(TILDE);
			setState(131);
			distribution();
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

	@SuppressWarnings("CheckReturnValue")
	public static class ConstantContext extends ParserRuleContext {
		public TerminalNode FLOAT_LITERAL() { return getToken(LPhyParser.FLOAT_LITERAL, 0); }
		public TerminalNode DECIMAL_LITERAL() { return getToken(LPhyParser.DECIMAL_LITERAL, 0); }
		public TerminalNode OCT_LITERAL() { return getToken(LPhyParser.OCT_LITERAL, 0); }
		public TerminalNode HEX_LITERAL() { return getToken(LPhyParser.HEX_LITERAL, 0); }
		public TerminalNode HEX_FLOAT_LITERAL() { return getToken(LPhyParser.HEX_FLOAT_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(LPhyParser.STRING_LITERAL, 0); }
		public ConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantContext constant() throws RecognitionException {
		ConstantContext _localctx = new ConstantContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_constant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__6) {
				{
				setState(133);
				match(T__6);
				}
			}

			setState(136);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 32710470927104L) != 0)) ) {
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

	@SuppressWarnings("CheckReturnValue")
	public static class Expression_listContext extends ParserRuleContext {
		public List<Named_expressionContext> named_expression() {
			return getRuleContexts(Named_expressionContext.class);
		}
		public Named_expressionContext named_expression(int i) {
			return getRuleContext(Named_expressionContext.class,i);
		}
		public Expression_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterExpression_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitExpression_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitExpression_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expression_listContext expression_list() throws RecognitionException {
		Expression_listContext _localctx = new Expression_listContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_expression_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			named_expression();
			setState(143);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__5) {
				{
				{
				setState(139);
				match(T__5);
				setState(140);
				named_expression();
				}
				}
				setState(145);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class Unnamed_expression_listContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public Unnamed_expression_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unnamed_expression_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterUnnamed_expression_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitUnnamed_expression_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitUnnamed_expression_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Unnamed_expression_listContext unnamed_expression_list() throws RecognitionException {
		Unnamed_expression_listContext _localctx = new Unnamed_expression_listContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_unnamed_expression_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			expression(0);
			setState(151);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__5) {
				{
				{
				setState(147);
				match(T__5);
				setState(148);
				expression(0);
				}
				}
				setState(153);
				_errHandler.sync(this);
				_la = _input.LA(1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class MapFunctionContext extends ParserRuleContext {
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public MapFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mapFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterMapFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitMapFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitMapFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MapFunctionContext mapFunction() throws RecognitionException {
		MapFunctionContext _localctx = new MapFunctionContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_mapFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			match(T__0);
			setState(155);
			expression_list();
			setState(156);
			match(T__1);
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

	@SuppressWarnings("CheckReturnValue")
	public static class MethodCallContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(LPhyParser.NAME, 0); }
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public Unnamed_expression_listContext unnamed_expression_list() {
			return getRuleContext(Unnamed_expression_listContext.class,0);
		}
		public MethodCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterMethodCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitMethodCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitMethodCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodCallContext methodCall() throws RecognitionException {
		MethodCallContext _localctx = new MethodCallContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_methodCall);
		int _la;
		try {
			setState(170);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(158);
				match(NAME);
				setState(159);
				match(T__9);
				setState(161);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NAME) {
					{
					setState(160);
					expression_list();
					}
				}

				setState(163);
				match(T__10);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(164);
				match(NAME);
				setState(165);
				match(T__9);
				setState(167);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32744830678930L) != 0)) {
					{
					setState(166);
					unnamed_expression_list();
					}
				}

				setState(169);
				match(T__10);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ObjectMethodCallContext extends ParserRuleContext {
		public VarContext var() {
			return getRuleContext(VarContext.class,0);
		}
		public TerminalNode DOT() { return getToken(LPhyParser.DOT, 0); }
		public TerminalNode NAME() { return getToken(LPhyParser.NAME, 0); }
		public Unnamed_expression_listContext unnamed_expression_list() {
			return getRuleContext(Unnamed_expression_listContext.class,0);
		}
		public ObjectMethodCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectMethodCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterObjectMethodCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitObjectMethodCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitObjectMethodCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ObjectMethodCallContext objectMethodCall() throws RecognitionException {
		ObjectMethodCallContext _localctx = new ObjectMethodCallContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_objectMethodCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(172);
			var();
			setState(173);
			match(DOT);
			setState(174);
			match(NAME);
			setState(175);
			match(T__9);
			setState(177);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32744830678930L) != 0)) {
				{
				setState(176);
				unnamed_expression_list();
				}
			}

			setState(179);
			match(T__10);
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

	@SuppressWarnings("CheckReturnValue")
	public static class DistributionContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(LPhyParser.NAME, 0); }
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public DistributionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_distribution; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterDistribution(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitDistribution(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitDistribution(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DistributionContext distribution() throws RecognitionException {
		DistributionContext _localctx = new DistributionContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_distribution);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(181);
			match(NAME);
			setState(182);
			match(T__9);
			setState(183);
			expression_list();
			setState(184);
			match(T__10);
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

	@SuppressWarnings("CheckReturnValue")
	public static class Named_expressionContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(LPhyParser.NAME, 0); }
		public TerminalNode ASSIGN() { return getToken(LPhyParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Named_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_named_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterNamed_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitNamed_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitNamed_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Named_expressionContext named_expression() throws RecognitionException {
		Named_expressionContext _localctx = new Named_expressionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_named_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(186);
			match(NAME);
			setState(187);
			match(ASSIGN);
			setState(188);
			expression(0);
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

	@SuppressWarnings("CheckReturnValue")
	public static class Array_expressionContext extends ParserRuleContext {
		public Unnamed_expression_listContext unnamed_expression_list() {
			return getRuleContext(Unnamed_expression_listContext.class,0);
		}
		public Array_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterArray_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitArray_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitArray_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Array_expressionContext array_expression() throws RecognitionException {
		Array_expressionContext _localctx = new Array_expressionContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_array_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
			match(T__3);
			setState(191);
			unnamed_expression_list();
			setState(192);
			match(T__4);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public Token prefix;
		public Token bop;
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public TerminalNode NAME() { return getToken(LPhyParser.NAME, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public Array_expressionContext array_expression() {
			return getRuleContext(Array_expressionContext.class,0);
		}
		public MethodCallContext methodCall() {
			return getRuleContext(MethodCallContext.class,0);
		}
		public ObjectMethodCallContext objectMethodCall() {
			return getRuleContext(ObjectMethodCallContext.class,0);
		}
		public MapFunctionContext mapFunction() {
			return getRuleContext(MapFunctionContext.class,0);
		}
		public Range_listContext range_list() {
			return getRuleContext(Range_listContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof LPhyListener ) ((LPhyListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof LPhyVisitor ) return ((LPhyVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 44;
		enterRecursionRule(_localctx, 44, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(207);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				{
				setState(195);
				constant();
				}
				break;
			case 2:
				{
				setState(196);
				match(NAME);
				}
				break;
			case 3:
				{
				setState(197);
				match(T__9);
				setState(198);
				expression(0);
				setState(199);
				match(T__10);
				}
				break;
			case 4:
				{
				setState(201);
				array_expression();
				}
				break;
			case 5:
				{
				setState(202);
				methodCall();
				}
				break;
			case 6:
				{
				setState(203);
				objectMethodCall();
				}
				break;
			case 7:
				{
				setState(204);
				((ExpressionContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 12416L) != 0)) ) {
					((ExpressionContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(205);
				expression(10);
				}
				break;
			case 8:
				{
				setState(206);
				mapFunction();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(249);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(247);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(209);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(210);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 245760L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(211);
						expression(10);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(212);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(213);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__6 || _la==T__11) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(214);
						expression(9);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(215);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(216);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3932160L) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(217);
						expression(8);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(218);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(219);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__21 || _la==T__22) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(220);
						expression(7);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(221);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(222);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__23 || _la==T__24) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(223);
						expression(6);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(224);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(225);
						((ExpressionContext)_localctx).bop = match(T__25);
						setState(226);
						expression(5);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(227);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(228);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__26 || _la==T__27) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(229);
						expression(4);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(230);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(239);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
						case 1:
							{
							setState(231);
							match(T__20);
							setState(232);
							match(T__20);
							}
							break;
						case 2:
							{
							setState(233);
							match(T__19);
							setState(234);
							match(T__19);
							setState(235);
							match(T__19);
							}
							break;
						case 3:
							{
							setState(236);
							match(T__19);
							setState(237);
							match(T__19);
							}
							break;
						case 4:
							{
							setState(238);
							match(T__28);
							}
							break;
						}
						setState(241);
						expression(3);
						}
						break;
					case 9:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(242);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(243);
						match(T__3);
						setState(244);
						range_list();
						setState(245);
						match(T__4);
						}
						break;
					}
					} 
				}
				setState(251);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 4:
			return determ_relation_list_sempred((Determ_relation_listContext)_localctx, predIndex);
		case 7:
			return relation_list_sempred((Relation_listContext)_localctx, predIndex);
		case 22:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean determ_relation_list_sempred(Determ_relation_listContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean relation_list_sempred(Relation_listContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 9);
		case 3:
			return precpred(_ctx, 8);
		case 4:
			return precpred(_ctx, 7);
		case 5:
			return precpred(_ctx, 6);
		case 6:
			return precpred(_ctx, 5);
		case 7:
			return precpred(_ctx, 4);
		case 8:
			return precpred(_ctx, 3);
		case 9:
			return precpred(_ctx, 2);
		case 10:
			return precpred(_ctx, 13);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001/\u00fd\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0001\u0000\u0001\u0000\u0003\u00001\b\u0000"+
		"\u0001\u0001\u0003\u00014\b\u0001\u0001\u0001\u0003\u00017\b\u0001\u0001"+
		"\u0002\u0001\u0002\u0003\u0002;\b\u0002\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003@\b\u0003\u0001\u0003\u0001\u0003\u0003\u0003D\b\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004"+
		"K\b\u0004\n\u0004\f\u0004N\t\u0004\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006V\b\u0006\u0001\u0006"+
		"\u0001\u0006\u0003\u0006Z\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0005\u0007a\b\u0007\n\u0007\f\u0007d\t\u0007"+
		"\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0003\bl\b\b\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\tt\b\t\u0001\n\u0001\n"+
		"\u0001\n\u0005\ny\b\n\n\n\f\n|\t\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\r\u0003\r\u0087\b\r"+
		"\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001\u000e\u0005\u000e\u008e"+
		"\b\u000e\n\u000e\f\u000e\u0091\t\u000e\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0005\u000f\u0096\b\u000f\n\u000f\f\u000f\u0099\t\u000f\u0001\u0010\u0001"+
		"\u0010\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0003"+
		"\u0011\u00a2\b\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0003"+
		"\u0011\u00a8\b\u0011\u0001\u0011\u0003\u0011\u00ab\b\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u00b2\b\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u00d0\b\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0003\u0016\u00f0\b\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0005\u0016\u00f8\b\u0016\n\u0016\f\u0016\u00fb"+
		"\t\u0016\u0001\u0016\u0000\u0003\b\u000e,\u0017\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,\u0000"+
		"\b\u0003\u0000\b\t&(*,\u0002\u0000\u0007\u0007\f\r\u0001\u0000\u000e\u0011"+
		"\u0002\u0000\u0007\u0007\f\f\u0001\u0000\u0012\u0015\u0001\u0000\u0016"+
		"\u0017\u0001\u0000\u0018\u0019\u0001\u0000\u001b\u001c\u010c\u00000\u0001"+
		"\u0000\u0000\u0000\u00023\u0001\u0000\u0000\u0000\u0004:\u0001\u0000\u0000"+
		"\u0000\u0006C\u0001\u0000\u0000\u0000\bE\u0001\u0000\u0000\u0000\nO\u0001"+
		"\u0000\u0000\u0000\fY\u0001\u0000\u0000\u0000\u000e[\u0001\u0000\u0000"+
		"\u0000\u0010k\u0001\u0000\u0000\u0000\u0012s\u0001\u0000\u0000\u0000\u0014"+
		"u\u0001\u0000\u0000\u0000\u0016}\u0001\u0000\u0000\u0000\u0018\u0081\u0001"+
		"\u0000\u0000\u0000\u001a\u0086\u0001\u0000\u0000\u0000\u001c\u008a\u0001"+
		"\u0000\u0000\u0000\u001e\u0092\u0001\u0000\u0000\u0000 \u009a\u0001\u0000"+
		"\u0000\u0000\"\u00aa\u0001\u0000\u0000\u0000$\u00ac\u0001\u0000\u0000"+
		"\u0000&\u00b5\u0001\u0000\u0000\u0000(\u00ba\u0001\u0000\u0000\u0000*"+
		"\u00be\u0001\u0000\u0000\u0000,\u00cf\u0001\u0000\u0000\u0000.1\u0003"+
		"\u0002\u0001\u0000/1\u0003\u0004\u0002\u00000.\u0001\u0000\u0000\u0000"+
		"0/\u0001\u0000\u0000\u00001\u0001\u0001\u0000\u0000\u000024\u0003\u0006"+
		"\u0003\u000032\u0001\u0000\u0000\u000034\u0001\u0000\u0000\u000046\u0001"+
		"\u0000\u0000\u000057\u0003\f\u0006\u000065\u0001\u0000\u0000\u000067\u0001"+
		"\u0000\u0000\u00007\u0003\u0001\u0000\u0000\u00008;\u0001\u0000\u0000"+
		"\u00009;\u0003\u000e\u0007\u0000:8\u0001\u0000\u0000\u0000:9\u0001\u0000"+
		"\u0000\u0000;\u0005\u0001\u0000\u0000\u0000<=\u0005\u001e\u0000\u0000"+
		"=?\u0005\u0001\u0000\u0000>@\u0003\b\u0004\u0000?>\u0001\u0000\u0000\u0000"+
		"?@\u0001\u0000\u0000\u0000@A\u0001\u0000\u0000\u0000AD\u0005\u0002\u0000"+
		"\u0000BD\u0003\b\u0004\u0000C<\u0001\u0000\u0000\u0000CB\u0001\u0000\u0000"+
		"\u0000D\u0007\u0001\u0000\u0000\u0000EF\u0006\u0004\uffff\uffff\u0000"+
		"FG\u0003\n\u0005\u0000GL\u0001\u0000\u0000\u0000HI\n\u0001\u0000\u0000"+
		"IK\u0003\n\u0005\u0000JH\u0001\u0000\u0000\u0000KN\u0001\u0000\u0000\u0000"+
		"LJ\u0001\u0000\u0000\u0000LM\u0001\u0000\u0000\u0000M\t\u0001\u0000\u0000"+
		"\u0000NL\u0001\u0000\u0000\u0000OP\u0003\u0016\u000b\u0000PQ\u0005\u0003"+
		"\u0000\u0000Q\u000b\u0001\u0000\u0000\u0000RS\u0005\u001f\u0000\u0000"+
		"SU\u0005\u0001\u0000\u0000TV\u0003\u000e\u0007\u0000UT\u0001\u0000\u0000"+
		"\u0000UV\u0001\u0000\u0000\u0000VW\u0001\u0000\u0000\u0000WZ\u0005\u0002"+
		"\u0000\u0000XZ\u0003\u000e\u0007\u0000YR\u0001\u0000\u0000\u0000YX\u0001"+
		"\u0000\u0000\u0000Z\r\u0001\u0000\u0000\u0000[\\\u0006\u0007\uffff\uffff"+
		"\u0000\\]\u0003\u0010\b\u0000]b\u0001\u0000\u0000\u0000^_\n\u0001\u0000"+
		"\u0000_a\u0003\u0010\b\u0000`^\u0001\u0000\u0000\u0000ad\u0001\u0000\u0000"+
		"\u0000b`\u0001\u0000\u0000\u0000bc\u0001\u0000\u0000\u0000c\u000f\u0001"+
		"\u0000\u0000\u0000db\u0001\u0000\u0000\u0000ef\u0003\u0018\f\u0000fg\u0005"+
		"\u0003\u0000\u0000gl\u0001\u0000\u0000\u0000hi\u0003\u0016\u000b\u0000"+
		"ij\u0005\u0003\u0000\u0000jl\u0001\u0000\u0000\u0000ke\u0001\u0000\u0000"+
		"\u0000kh\u0001\u0000\u0000\u0000l\u0011\u0001\u0000\u0000\u0000mt\u0005"+
		"#\u0000\u0000no\u0005#\u0000\u0000op\u0005\u0004\u0000\u0000pq\u0003\u0014"+
		"\n\u0000qr\u0005\u0005\u0000\u0000rt\u0001\u0000\u0000\u0000sm\u0001\u0000"+
		"\u0000\u0000sn\u0001\u0000\u0000\u0000t\u0013\u0001\u0000\u0000\u0000"+
		"uz\u0003,\u0016\u0000vw\u0005\u0006\u0000\u0000wy\u0003,\u0016\u0000x"+
		"v\u0001\u0000\u0000\u0000y|\u0001\u0000\u0000\u0000zx\u0001\u0000\u0000"+
		"\u0000z{\u0001\u0000\u0000\u0000{\u0015\u0001\u0000\u0000\u0000|z\u0001"+
		"\u0000\u0000\u0000}~\u0003\u0012\t\u0000~\u007f\u0005 \u0000\u0000\u007f"+
		"\u0080\u0003,\u0016\u0000\u0080\u0017\u0001\u0000\u0000\u0000\u0081\u0082"+
		"\u0003\u0012\t\u0000\u0082\u0083\u0005!\u0000\u0000\u0083\u0084\u0003"+
		"&\u0013\u0000\u0084\u0019\u0001\u0000\u0000\u0000\u0085\u0087\u0005\u0007"+
		"\u0000\u0000\u0086\u0085\u0001\u0000\u0000\u0000\u0086\u0087\u0001\u0000"+
		"\u0000\u0000\u0087\u0088\u0001\u0000\u0000\u0000\u0088\u0089\u0007\u0000"+
		"\u0000\u0000\u0089\u001b\u0001\u0000\u0000\u0000\u008a\u008f\u0003(\u0014"+
		"\u0000\u008b\u008c\u0005\u0006\u0000\u0000\u008c\u008e\u0003(\u0014\u0000"+
		"\u008d\u008b\u0001\u0000\u0000\u0000\u008e\u0091\u0001\u0000\u0000\u0000"+
		"\u008f\u008d\u0001\u0000\u0000\u0000\u008f\u0090\u0001\u0000\u0000\u0000"+
		"\u0090\u001d\u0001\u0000\u0000\u0000\u0091\u008f\u0001\u0000\u0000\u0000"+
		"\u0092\u0097\u0003,\u0016\u0000\u0093\u0094\u0005\u0006\u0000\u0000\u0094"+
		"\u0096\u0003,\u0016\u0000\u0095\u0093\u0001\u0000\u0000\u0000\u0096\u0099"+
		"\u0001\u0000\u0000\u0000\u0097\u0095\u0001\u0000\u0000\u0000\u0097\u0098"+
		"\u0001\u0000\u0000\u0000\u0098\u001f\u0001\u0000\u0000\u0000\u0099\u0097"+
		"\u0001\u0000\u0000\u0000\u009a\u009b\u0005\u0001\u0000\u0000\u009b\u009c"+
		"\u0003\u001c\u000e\u0000\u009c\u009d\u0005\u0002\u0000\u0000\u009d!\u0001"+
		"\u0000\u0000\u0000\u009e\u009f\u0005#\u0000\u0000\u009f\u00a1\u0005\n"+
		"\u0000\u0000\u00a0\u00a2\u0003\u001c\u000e\u0000\u00a1\u00a0\u0001\u0000"+
		"\u0000\u0000\u00a1\u00a2\u0001\u0000\u0000\u0000\u00a2\u00a3\u0001\u0000"+
		"\u0000\u0000\u00a3\u00ab\u0005\u000b\u0000\u0000\u00a4\u00a5\u0005#\u0000"+
		"\u0000\u00a5\u00a7\u0005\n\u0000\u0000\u00a6\u00a8\u0003\u001e\u000f\u0000"+
		"\u00a7\u00a6\u0001\u0000\u0000\u0000\u00a7\u00a8\u0001\u0000\u0000\u0000"+
		"\u00a8\u00a9\u0001\u0000\u0000\u0000\u00a9\u00ab\u0005\u000b\u0000\u0000"+
		"\u00aa\u009e\u0001\u0000\u0000\u0000\u00aa\u00a4\u0001\u0000\u0000\u0000"+
		"\u00ab#\u0001\u0000\u0000\u0000\u00ac\u00ad\u0003\u0012\t\u0000\u00ad"+
		"\u00ae\u0005\"\u0000\u0000\u00ae\u00af\u0005#\u0000\u0000\u00af\u00b1"+
		"\u0005\n\u0000\u0000\u00b0\u00b2\u0003\u001e\u000f\u0000\u00b1\u00b0\u0001"+
		"\u0000\u0000\u0000\u00b1\u00b2\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001"+
		"\u0000\u0000\u0000\u00b3\u00b4\u0005\u000b\u0000\u0000\u00b4%\u0001\u0000"+
		"\u0000\u0000\u00b5\u00b6\u0005#\u0000\u0000\u00b6\u00b7\u0005\n\u0000"+
		"\u0000\u00b7\u00b8\u0003\u001c\u000e\u0000\u00b8\u00b9\u0005\u000b\u0000"+
		"\u0000\u00b9\'\u0001\u0000\u0000\u0000\u00ba\u00bb\u0005#\u0000\u0000"+
		"\u00bb\u00bc\u0005 \u0000\u0000\u00bc\u00bd\u0003,\u0016\u0000\u00bd)"+
		"\u0001\u0000\u0000\u0000\u00be\u00bf\u0005\u0004\u0000\u0000\u00bf\u00c0"+
		"\u0003\u001e\u000f\u0000\u00c0\u00c1\u0005\u0005\u0000\u0000\u00c1+\u0001"+
		"\u0000\u0000\u0000\u00c2\u00c3\u0006\u0016\uffff\uffff\u0000\u00c3\u00d0"+
		"\u0003\u001a\r\u0000\u00c4\u00d0\u0005#\u0000\u0000\u00c5\u00c6\u0005"+
		"\n\u0000\u0000\u00c6\u00c7\u0003,\u0016\u0000\u00c7\u00c8\u0005\u000b"+
		"\u0000\u0000\u00c8\u00d0\u0001\u0000\u0000\u0000\u00c9\u00d0\u0003*\u0015"+
		"\u0000\u00ca\u00d0\u0003\"\u0011\u0000\u00cb\u00d0\u0003$\u0012\u0000"+
		"\u00cc\u00cd\u0007\u0001\u0000\u0000\u00cd\u00d0\u0003,\u0016\n\u00ce"+
		"\u00d0\u0003 \u0010\u0000\u00cf\u00c2\u0001\u0000\u0000\u0000\u00cf\u00c4"+
		"\u0001\u0000\u0000\u0000\u00cf\u00c5\u0001\u0000\u0000\u0000\u00cf\u00c9"+
		"\u0001\u0000\u0000\u0000\u00cf\u00ca\u0001\u0000\u0000\u0000\u00cf\u00cb"+
		"\u0001\u0000\u0000\u0000\u00cf\u00cc\u0001\u0000\u0000\u0000\u00cf\u00ce"+
		"\u0001\u0000\u0000\u0000\u00d0\u00f9\u0001\u0000\u0000\u0000\u00d1\u00d2"+
		"\n\t\u0000\u0000\u00d2\u00d3\u0007\u0002\u0000\u0000\u00d3\u00f8\u0003"+
		",\u0016\n\u00d4\u00d5\n\b\u0000\u0000\u00d5\u00d6\u0007\u0003\u0000\u0000"+
		"\u00d6\u00f8\u0003,\u0016\t\u00d7\u00d8\n\u0007\u0000\u0000\u00d8\u00d9"+
		"\u0007\u0004\u0000\u0000\u00d9\u00f8\u0003,\u0016\b\u00da\u00db\n\u0006"+
		"\u0000\u0000\u00db\u00dc\u0007\u0005\u0000\u0000\u00dc\u00f8\u0003,\u0016"+
		"\u0007\u00dd\u00de\n\u0005\u0000\u0000\u00de\u00df\u0007\u0006\u0000\u0000"+
		"\u00df\u00f8\u0003,\u0016\u0006\u00e0\u00e1\n\u0004\u0000\u0000\u00e1"+
		"\u00e2\u0005\u001a\u0000\u0000\u00e2\u00f8\u0003,\u0016\u0005\u00e3\u00e4"+
		"\n\u0003\u0000\u0000\u00e4\u00e5\u0007\u0007\u0000\u0000\u00e5\u00f8\u0003"+
		",\u0016\u0004\u00e6\u00ef\n\u0002\u0000\u0000\u00e7\u00e8\u0005\u0015"+
		"\u0000\u0000\u00e8\u00f0\u0005\u0015\u0000\u0000\u00e9\u00ea\u0005\u0014"+
		"\u0000\u0000\u00ea\u00eb\u0005\u0014\u0000\u0000\u00eb\u00f0\u0005\u0014"+
		"\u0000\u0000\u00ec\u00ed\u0005\u0014\u0000\u0000\u00ed\u00f0\u0005\u0014"+
		"\u0000\u0000\u00ee\u00f0\u0005\u001d\u0000\u0000\u00ef\u00e7\u0001\u0000"+
		"\u0000\u0000\u00ef\u00e9\u0001\u0000\u0000\u0000\u00ef\u00ec\u0001\u0000"+
		"\u0000\u0000\u00ef\u00ee\u0001\u0000\u0000\u0000\u00f0\u00f1\u0001\u0000"+
		"\u0000\u0000\u00f1\u00f8\u0003,\u0016\u0003\u00f2\u00f3\n\r\u0000\u0000"+
		"\u00f3\u00f4\u0005\u0004\u0000\u0000\u00f4\u00f5\u0003\u0014\n\u0000\u00f5"+
		"\u00f6\u0005\u0005\u0000\u0000\u00f6\u00f8\u0001\u0000\u0000\u0000\u00f7"+
		"\u00d1\u0001\u0000\u0000\u0000\u00f7\u00d4\u0001\u0000\u0000\u0000\u00f7"+
		"\u00d7\u0001\u0000\u0000\u0000\u00f7\u00da\u0001\u0000\u0000\u0000\u00f7"+
		"\u00dd\u0001\u0000\u0000\u0000\u00f7\u00e0\u0001\u0000\u0000\u0000\u00f7"+
		"\u00e3\u0001\u0000\u0000\u0000\u00f7\u00e6\u0001\u0000\u0000\u0000\u00f7"+
		"\u00f2\u0001\u0000\u0000\u0000\u00f8\u00fb\u0001\u0000\u0000\u0000\u00f9"+
		"\u00f7\u0001\u0000\u0000\u0000\u00f9\u00fa\u0001\u0000\u0000\u0000\u00fa"+
		"-\u0001\u0000\u0000\u0000\u00fb\u00f9\u0001\u0000\u0000\u0000\u001803"+
		"6:?CLUYbksz\u0086\u008f\u0097\u00a1\u00a7\u00aa\u00b1\u00cf\u00ef\u00f7"+
		"\u00f9";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}