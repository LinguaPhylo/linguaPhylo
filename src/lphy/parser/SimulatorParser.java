// Generated from /Users/alexeidrummond/Git/graphicalModelSimulation/src/lphy/parser/Simulator.g4 by ANTLR 4.8
package lphy.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SimulatorParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, NAME=34, ARROW=35, LENGTH=36, DIM=37, DECIMAL_LITERAL=38, 
		HEX_LITERAL=39, OCT_LITERAL=40, BINARY_LITERAL=41, FLOAT_LITERAL=42, HEX_FLOAT_LITERAL=43, 
		STRING_LITERAL=44, TILDE=45, WS=46, COMMENT=47, LINE_COMMENT=48;
	public static final int
		RULE_input = 0, RULE_relations = 1, RULE_relation_list = 2, RULE_relation = 3, 
		RULE_for_loop = 4, RULE_counter = 5, RULE_assignment = 6, RULE_determ_relation = 7, 
		RULE_stoch_relation = 8, RULE_var = 9, RULE_range_list = 10, RULE_range_element = 11, 
		RULE_constant = 12, RULE_expression_list = 13, RULE_unnamed_expression_list = 14, 
		RULE_mapFunction = 15, RULE_methodCall = 16, RULE_distribution = 17, RULE_named_expression = 18, 
		RULE_expression = 19;
	private static String[] makeRuleNames() {
		return new String[] {
			"input", "relations", "relation_list", "relation", "for_loop", "counter", 
			"assignment", "determ_relation", "stoch_relation", "var", "range_list", 
			"range_element", "constant", "expression_list", "unnamed_expression_list", 
			"mapFunction", "methodCall", "distribution", "named_expression", "expression"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "';'", "'for'", "'('", "'in'", "')'", "'='", "'['", 
			"']'", "','", "'-'", "'true'", "'false'", "'++'", "'--'", "'+'", "'**'", 
			"'*'", "'/'", "'%'", "'<'", "'>'", "'<='", "'>='", "'=='", "'!='", "'&'", 
			"'^'", "'|'", "'&&'", "'||'", "':'", null, "'<-'", "'length'", "'dim'", 
			null, null, null, null, null, null, null, "'~'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, "NAME", "ARROW", 
			"LENGTH", "DIM", "DECIMAL_LITERAL", "HEX_LITERAL", "OCT_LITERAL", "BINARY_LITERAL", 
			"FLOAT_LITERAL", "HEX_FLOAT_LITERAL", "STRING_LITERAL", "TILDE", "WS", 
			"COMMENT", "LINE_COMMENT"
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
	public String getGrammarFileName() { return "Simulator.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SimulatorParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class InputContext extends ParserRuleContext {
		public Relation_listContext relation_list() {
			return getRuleContext(Relation_listContext.class,0);
		}
		public InputContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_input; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterInput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitInput(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitInput(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InputContext input() throws RecognitionException {
		InputContext _localctx = new InputContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_input);
		try {
			setState(42);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EOF:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case T__3:
			case NAME:
				enterOuterAlt(_localctx, 2);
				{
				setState(41);
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

	public static class RelationsContext extends ParserRuleContext {
		public Relation_listContext relation_list() {
			return getRuleContext(Relation_listContext.class,0);
		}
		public RelationsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relations; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterRelations(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitRelations(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitRelations(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationsContext relations() throws RecognitionException {
		RelationsContext _localctx = new RelationsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_relations);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(44);
			match(T__0);
			setState(45);
			relation_list(0);
			setState(46);
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
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterRelation_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitRelation_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitRelation_list(this);
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
		int _startState = 4;
		enterRecursionRule(_localctx, 4, RULE_relation_list, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(49);
			relation();
			}
			_ctx.stop = _input.LT(-1);
			setState(55);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Relation_listContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_relation_list);
					setState(51);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(52);
					relation();
					}
					} 
				}
				setState(57);
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

	public static class RelationContext extends ParserRuleContext {
		public Stoch_relationContext stoch_relation() {
			return getRuleContext(Stoch_relationContext.class,0);
		}
		public Determ_relationContext determ_relation() {
			return getRuleContext(Determ_relationContext.class,0);
		}
		public For_loopContext for_loop() {
			return getRuleContext(For_loopContext.class,0);
		}
		public RelationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitRelation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationContext relation() throws RecognitionException {
		RelationContext _localctx = new RelationContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_relation);
		try {
			setState(67);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(58);
				stoch_relation();
				setState(59);
				match(T__2);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(61);
				determ_relation();
				setState(62);
				match(T__2);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(64);
				for_loop();
				setState(65);
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

	public static class For_loopContext extends ParserRuleContext {
		public CounterContext counter() {
			return getRuleContext(CounterContext.class,0);
		}
		public RelationsContext relations() {
			return getRuleContext(RelationsContext.class,0);
		}
		public For_loopContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_for_loop; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterFor_loop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitFor_loop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitFor_loop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final For_loopContext for_loop() throws RecognitionException {
		For_loopContext _localctx = new For_loopContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_for_loop);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			counter();
			setState(70);
			relations();
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

	public static class CounterContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(SimulatorParser.NAME, 0); }
		public Range_elementContext range_element() {
			return getRuleContext(Range_elementContext.class,0);
		}
		public CounterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_counter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterCounter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitCounter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitCounter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CounterContext counter() throws RecognitionException {
		CounterContext _localctx = new CounterContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_counter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(72);
			match(T__3);
			setState(73);
			match(T__4);
			setState(74);
			match(NAME);
			setState(75);
			match(T__5);
			setState(76);
			range_element();
			setState(77);
			match(T__6);
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

	public static class AssignmentContext extends ParserRuleContext {
		public TerminalNode ARROW() { return getToken(SimulatorParser.ARROW, 0); }
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitAssignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_assignment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			_la = _input.LA(1);
			if ( !(_la==T__7 || _la==ARROW) ) {
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

	public static class Determ_relationContext extends ParserRuleContext {
		public VarContext var() {
			return getRuleContext(VarContext.class,0);
		}
		public AssignmentContext assignment() {
			return getRuleContext(AssignmentContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Determ_relationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_determ_relation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterDeterm_relation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitDeterm_relation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitDeterm_relation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Determ_relationContext determ_relation() throws RecognitionException {
		Determ_relationContext _localctx = new Determ_relationContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_determ_relation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81);
			var();
			setState(82);
			assignment();
			setState(83);
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

	public static class Stoch_relationContext extends ParserRuleContext {
		public VarContext var() {
			return getRuleContext(VarContext.class,0);
		}
		public TerminalNode TILDE() { return getToken(SimulatorParser.TILDE, 0); }
		public DistributionContext distribution() {
			return getRuleContext(DistributionContext.class,0);
		}
		public Stoch_relationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stoch_relation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterStoch_relation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitStoch_relation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitStoch_relation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Stoch_relationContext stoch_relation() throws RecognitionException {
		Stoch_relationContext _localctx = new Stoch_relationContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_stoch_relation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			var();
			setState(86);
			match(TILDE);
			setState(87);
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

	public static class VarContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(SimulatorParser.NAME, 0); }
		public Range_listContext range_list() {
			return getRuleContext(Range_listContext.class,0);
		}
		public VarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitVar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitVar(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarContext var() throws RecognitionException {
		VarContext _localctx = new VarContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_var);
		try {
			setState(95);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(89);
				match(NAME);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(90);
				match(NAME);
				setState(91);
				match(T__8);
				setState(92);
				range_list(0);
				setState(93);
				match(T__9);
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

	public static class Range_listContext extends ParserRuleContext {
		public Range_elementContext range_element() {
			return getRuleContext(Range_elementContext.class,0);
		}
		public Range_listContext range_list() {
			return getRuleContext(Range_listContext.class,0);
		}
		public Range_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_range_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterRange_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitRange_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitRange_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Range_listContext range_list() throws RecognitionException {
		return range_list(0);
	}

	private Range_listContext range_list(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		Range_listContext _localctx = new Range_listContext(_ctx, _parentState);
		Range_listContext _prevctx = _localctx;
		int _startState = 20;
		enterRecursionRule(_localctx, 20, RULE_range_list, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(98);
			range_element();
			}
			_ctx.stop = _input.LT(-1);
			setState(105);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Range_listContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_range_list);
					setState(100);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(101);
					match(T__10);
					setState(102);
					range_element();
					}
					} 
				}
				setState(107);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
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

	public static class Range_elementContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Range_elementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_range_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterRange_element(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitRange_element(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitRange_element(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Range_elementContext range_element() throws RecognitionException {
		Range_elementContext _localctx = new Range_elementContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_range_element);
		try {
			setState(110);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(109);
				expression(0);
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

	public static class ConstantContext extends ParserRuleContext {
		public TerminalNode FLOAT_LITERAL() { return getToken(SimulatorParser.FLOAT_LITERAL, 0); }
		public TerminalNode DECIMAL_LITERAL() { return getToken(SimulatorParser.DECIMAL_LITERAL, 0); }
		public TerminalNode OCT_LITERAL() { return getToken(SimulatorParser.OCT_LITERAL, 0); }
		public TerminalNode HEX_LITERAL() { return getToken(SimulatorParser.HEX_LITERAL, 0); }
		public TerminalNode HEX_FLOAT_LITERAL() { return getToken(SimulatorParser.HEX_FLOAT_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(SimulatorParser.STRING_LITERAL, 0); }
		public ConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantContext constant() throws RecognitionException {
		ConstantContext _localctx = new ConstantContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_constant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(113);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__11) {
				{
				setState(112);
				match(T__11);
				}
			}

			setState(115);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__12) | (1L << T__13) | (1L << DECIMAL_LITERAL) | (1L << HEX_LITERAL) | (1L << OCT_LITERAL) | (1L << FLOAT_LITERAL) | (1L << HEX_FLOAT_LITERAL) | (1L << STRING_LITERAL))) != 0)) ) {
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
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterExpression_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitExpression_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitExpression_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expression_listContext expression_list() throws RecognitionException {
		Expression_listContext _localctx = new Expression_listContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_expression_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			named_expression();
			setState(122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__10) {
				{
				{
				setState(118);
				match(T__10);
				setState(119);
				named_expression();
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
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterUnnamed_expression_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitUnnamed_expression_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitUnnamed_expression_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Unnamed_expression_listContext unnamed_expression_list() throws RecognitionException {
		Unnamed_expression_listContext _localctx = new Unnamed_expression_listContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_unnamed_expression_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			expression(0);
			setState(130);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__10) {
				{
				{
				setState(126);
				match(T__10);
				setState(127);
				expression(0);
				}
				}
				setState(132);
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
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterMapFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitMapFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitMapFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MapFunctionContext mapFunction() throws RecognitionException {
		MapFunctionContext _localctx = new MapFunctionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_mapFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(133);
			match(T__0);
			setState(134);
			expression_list();
			setState(135);
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

	public static class MethodCallContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(SimulatorParser.NAME, 0); }
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
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterMethodCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitMethodCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitMethodCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodCallContext methodCall() throws RecognitionException {
		MethodCallContext _localctx = new MethodCallContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_methodCall);
		int _la;
		try {
			setState(149);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(137);
				match(NAME);
				setState(138);
				match(T__4);
				setState(140);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NAME) {
					{
					setState(139);
					expression_list();
					}
				}

				setState(142);
				match(T__6);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(143);
				match(NAME);
				setState(144);
				match(T__4);
				setState(146);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__4) | (1L << T__8) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << T__15) | (1L << T__16) | (1L << NAME) | (1L << DECIMAL_LITERAL) | (1L << HEX_LITERAL) | (1L << OCT_LITERAL) | (1L << FLOAT_LITERAL) | (1L << HEX_FLOAT_LITERAL) | (1L << STRING_LITERAL))) != 0)) {
					{
					setState(145);
					unnamed_expression_list();
					}
				}

				setState(148);
				match(T__6);
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

	public static class DistributionContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(SimulatorParser.NAME, 0); }
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public DistributionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_distribution; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterDistribution(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitDistribution(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitDistribution(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DistributionContext distribution() throws RecognitionException {
		DistributionContext _localctx = new DistributionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_distribution);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(151);
			match(NAME);
			setState(152);
			match(T__4);
			setState(153);
			expression_list();
			setState(154);
			match(T__6);
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

	public static class Named_expressionContext extends ParserRuleContext {
		public TerminalNode NAME() { return getToken(SimulatorParser.NAME, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Named_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_named_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterNamed_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitNamed_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitNamed_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Named_expressionContext named_expression() throws RecognitionException {
		Named_expressionContext _localctx = new Named_expressionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_named_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156);
			match(NAME);
			setState(157);
			match(T__7);
			setState(158);
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

	public static class ExpressionContext extends ParserRuleContext {
		public Token prefix;
		public Token bop;
		public Token postfix;
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public TerminalNode NAME() { return getToken(SimulatorParser.NAME, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public Unnamed_expression_listContext unnamed_expression_list() {
			return getRuleContext(Unnamed_expression_listContext.class,0);
		}
		public MethodCallContext methodCall() {
			return getRuleContext(MethodCallContext.class,0);
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
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitExpression(this);
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
		int _startState = 38;
		enterRecursionRule(_localctx, 38, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(175);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				{
				setState(161);
				constant();
				}
				break;
			case 2:
				{
				setState(162);
				match(NAME);
				}
				break;
			case 3:
				{
				setState(163);
				match(T__4);
				setState(164);
				expression(0);
				setState(165);
				match(T__6);
				}
				break;
			case 4:
				{
				setState(167);
				match(T__8);
				setState(168);
				unnamed_expression_list();
				setState(169);
				match(T__9);
				}
				break;
			case 5:
				{
				setState(171);
				methodCall();
				}
				break;
			case 6:
				{
				setState(172);
				((ExpressionContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__11) | (1L << T__14) | (1L << T__15) | (1L << T__16))) != 0)) ) {
					((ExpressionContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(173);
				expression(13);
				}
				break;
			case 7:
				{
				setState(174);
				mapFunction();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(227);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(225);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(177);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(178);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__17) | (1L << T__18) | (1L << T__19) | (1L << T__20))) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(179);
						expression(13);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(180);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(181);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__11 || _la==T__16) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(182);
						expression(12);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(183);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(191);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
						case 1:
							{
							setState(184);
							match(T__21);
							setState(185);
							match(T__21);
							}
							break;
						case 2:
							{
							setState(186);
							match(T__22);
							setState(187);
							match(T__22);
							setState(188);
							match(T__22);
							}
							break;
						case 3:
							{
							setState(189);
							match(T__22);
							setState(190);
							match(T__22);
							}
							break;
						}
						setState(193);
						expression(11);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(194);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(195);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__21) | (1L << T__22) | (1L << T__23) | (1L << T__24))) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(196);
						expression(10);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(197);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(198);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__25 || _la==T__26) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(199);
						expression(9);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(200);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(201);
						((ExpressionContext)_localctx).bop = match(T__27);
						setState(202);
						expression(8);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(203);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(204);
						((ExpressionContext)_localctx).bop = match(T__28);
						setState(205);
						expression(7);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(206);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(207);
						((ExpressionContext)_localctx).bop = match(T__29);
						setState(208);
						expression(6);
						}
						break;
					case 9:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(209);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(210);
						((ExpressionContext)_localctx).bop = match(T__30);
						setState(211);
						expression(5);
						}
						break;
					case 10:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(212);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(213);
						((ExpressionContext)_localctx).bop = match(T__31);
						setState(214);
						expression(4);
						}
						break;
					case 11:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(215);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(216);
						((ExpressionContext)_localctx).bop = match(T__32);
						setState(217);
						expression(3);
						}
						break;
					case 12:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(218);
						if (!(precpred(_ctx, 16))) throw new FailedPredicateException(this, "precpred(_ctx, 16)");
						setState(219);
						match(T__8);
						setState(220);
						range_list(0);
						setState(221);
						match(T__9);
						}
						break;
					case 13:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(223);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(224);
						((ExpressionContext)_localctx).postfix = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__14 || _la==T__15) ) {
							((ExpressionContext)_localctx).postfix = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					}
					} 
				}
				setState(229);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
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
		case 2:
			return relation_list_sempred((Relation_listContext)_localctx, predIndex);
		case 10:
			return range_list_sempred((Range_listContext)_localctx, predIndex);
		case 19:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean relation_list_sempred(Relation_listContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean range_list_sempred(Range_listContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 12);
		case 3:
			return precpred(_ctx, 11);
		case 4:
			return precpred(_ctx, 10);
		case 5:
			return precpred(_ctx, 9);
		case 6:
			return precpred(_ctx, 8);
		case 7:
			return precpred(_ctx, 7);
		case 8:
			return precpred(_ctx, 6);
		case 9:
			return precpred(_ctx, 5);
		case 10:
			return precpred(_ctx, 4);
		case 11:
			return precpred(_ctx, 3);
		case 12:
			return precpred(_ctx, 2);
		case 13:
			return precpred(_ctx, 16);
		case 14:
			return precpred(_ctx, 14);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\62\u00e9\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\3\2\3\2\5\2-\n\2\3\3\3\3\3\3\3\3\3\4\3"+
		"\4\3\4\3\4\3\4\7\48\n\4\f\4\16\4;\13\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5"+
		"\3\5\5\5F\n\5\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3\t"+
		"\3\t\3\t\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\5\13b\n\13\3\f"+
		"\3\f\3\f\3\f\3\f\3\f\7\fj\n\f\f\f\16\fm\13\f\3\r\3\r\5\rq\n\r\3\16\5\16"+
		"t\n\16\3\16\3\16\3\17\3\17\3\17\7\17{\n\17\f\17\16\17~\13\17\3\20\3\20"+
		"\3\20\7\20\u0083\n\20\f\20\16\20\u0086\13\20\3\21\3\21\3\21\3\21\3\22"+
		"\3\22\3\22\5\22\u008f\n\22\3\22\3\22\3\22\3\22\5\22\u0095\n\22\3\22\5"+
		"\22\u0098\n\22\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\5\25"+
		"\u00b2\n\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\5\25\u00c2\n\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\7\25\u00e4\n\25\f\25\16"+
		"\25\u00e7\13\25\3\25\2\5\6\26(\26\2\4\6\b\n\f\16\20\22\24\26\30\32\34"+
		"\36 \"$&(\2\n\4\2\n\n%%\5\2\17\20(*,.\4\2\16\16\21\23\3\2\24\27\4\2\16"+
		"\16\23\23\3\2\30\33\3\2\34\35\3\2\21\22\2\u00f6\2,\3\2\2\2\4.\3\2\2\2"+
		"\6\62\3\2\2\2\bE\3\2\2\2\nG\3\2\2\2\fJ\3\2\2\2\16Q\3\2\2\2\20S\3\2\2\2"+
		"\22W\3\2\2\2\24a\3\2\2\2\26c\3\2\2\2\30p\3\2\2\2\32s\3\2\2\2\34w\3\2\2"+
		"\2\36\177\3\2\2\2 \u0087\3\2\2\2\"\u0097\3\2\2\2$\u0099\3\2\2\2&\u009e"+
		"\3\2\2\2(\u00b1\3\2\2\2*-\3\2\2\2+-\5\6\4\2,*\3\2\2\2,+\3\2\2\2-\3\3\2"+
		"\2\2./\7\3\2\2/\60\5\6\4\2\60\61\7\4\2\2\61\5\3\2\2\2\62\63\b\4\1\2\63"+
		"\64\5\b\5\2\649\3\2\2\2\65\66\f\3\2\2\668\5\b\5\2\67\65\3\2\2\28;\3\2"+
		"\2\29\67\3\2\2\29:\3\2\2\2:\7\3\2\2\2;9\3\2\2\2<=\5\22\n\2=>\7\5\2\2>"+
		"F\3\2\2\2?@\5\20\t\2@A\7\5\2\2AF\3\2\2\2BC\5\n\6\2CD\7\5\2\2DF\3\2\2\2"+
		"E<\3\2\2\2E?\3\2\2\2EB\3\2\2\2F\t\3\2\2\2GH\5\f\7\2HI\5\4\3\2I\13\3\2"+
		"\2\2JK\7\6\2\2KL\7\7\2\2LM\7$\2\2MN\7\b\2\2NO\5\30\r\2OP\7\t\2\2P\r\3"+
		"\2\2\2QR\t\2\2\2R\17\3\2\2\2ST\5\24\13\2TU\5\16\b\2UV\5(\25\2V\21\3\2"+
		"\2\2WX\5\24\13\2XY\7/\2\2YZ\5$\23\2Z\23\3\2\2\2[b\7$\2\2\\]\7$\2\2]^\7"+
		"\13\2\2^_\5\26\f\2_`\7\f\2\2`b\3\2\2\2a[\3\2\2\2a\\\3\2\2\2b\25\3\2\2"+
		"\2cd\b\f\1\2de\5\30\r\2ek\3\2\2\2fg\f\3\2\2gh\7\r\2\2hj\5\30\r\2if\3\2"+
		"\2\2jm\3\2\2\2ki\3\2\2\2kl\3\2\2\2l\27\3\2\2\2mk\3\2\2\2nq\3\2\2\2oq\5"+
		"(\25\2pn\3\2\2\2po\3\2\2\2q\31\3\2\2\2rt\7\16\2\2sr\3\2\2\2st\3\2\2\2"+
		"tu\3\2\2\2uv\t\3\2\2v\33\3\2\2\2w|\5&\24\2xy\7\r\2\2y{\5&\24\2zx\3\2\2"+
		"\2{~\3\2\2\2|z\3\2\2\2|}\3\2\2\2}\35\3\2\2\2~|\3\2\2\2\177\u0084\5(\25"+
		"\2\u0080\u0081\7\r\2\2\u0081\u0083\5(\25\2\u0082\u0080\3\2\2\2\u0083\u0086"+
		"\3\2\2\2\u0084\u0082\3\2\2\2\u0084\u0085\3\2\2\2\u0085\37\3\2\2\2\u0086"+
		"\u0084\3\2\2\2\u0087\u0088\7\3\2\2\u0088\u0089\5\34\17\2\u0089\u008a\7"+
		"\4\2\2\u008a!\3\2\2\2\u008b\u008c\7$\2\2\u008c\u008e\7\7\2\2\u008d\u008f"+
		"\5\34\17\2\u008e\u008d\3\2\2\2\u008e\u008f\3\2\2\2\u008f\u0090\3\2\2\2"+
		"\u0090\u0098\7\t\2\2\u0091\u0092\7$\2\2\u0092\u0094\7\7\2\2\u0093\u0095"+
		"\5\36\20\2\u0094\u0093\3\2\2\2\u0094\u0095\3\2\2\2\u0095\u0096\3\2\2\2"+
		"\u0096\u0098\7\t\2\2\u0097\u008b\3\2\2\2\u0097\u0091\3\2\2\2\u0098#\3"+
		"\2\2\2\u0099\u009a\7$\2\2\u009a\u009b\7\7\2\2\u009b\u009c\5\34\17\2\u009c"+
		"\u009d\7\t\2\2\u009d%\3\2\2\2\u009e\u009f\7$\2\2\u009f\u00a0\7\n\2\2\u00a0"+
		"\u00a1\5(\25\2\u00a1\'\3\2\2\2\u00a2\u00a3\b\25\1\2\u00a3\u00b2\5\32\16"+
		"\2\u00a4\u00b2\7$\2\2\u00a5\u00a6\7\7\2\2\u00a6\u00a7\5(\25\2\u00a7\u00a8"+
		"\7\t\2\2\u00a8\u00b2\3\2\2\2\u00a9\u00aa\7\13\2\2\u00aa\u00ab\5\36\20"+
		"\2\u00ab\u00ac\7\f\2\2\u00ac\u00b2\3\2\2\2\u00ad\u00b2\5\"\22\2\u00ae"+
		"\u00af\t\4\2\2\u00af\u00b2\5(\25\17\u00b0\u00b2\5 \21\2\u00b1\u00a2\3"+
		"\2\2\2\u00b1\u00a4\3\2\2\2\u00b1\u00a5\3\2\2\2\u00b1\u00a9\3\2\2\2\u00b1"+
		"\u00ad\3\2\2\2\u00b1\u00ae\3\2\2\2\u00b1\u00b0\3\2\2\2\u00b2\u00e5\3\2"+
		"\2\2\u00b3\u00b4\f\16\2\2\u00b4\u00b5\t\5\2\2\u00b5\u00e4\5(\25\17\u00b6"+
		"\u00b7\f\r\2\2\u00b7\u00b8\t\6\2\2\u00b8\u00e4\5(\25\16\u00b9\u00c1\f"+
		"\f\2\2\u00ba\u00bb\7\30\2\2\u00bb\u00c2\7\30\2\2\u00bc\u00bd\7\31\2\2"+
		"\u00bd\u00be\7\31\2\2\u00be\u00c2\7\31\2\2\u00bf\u00c0\7\31\2\2\u00c0"+
		"\u00c2\7\31\2\2\u00c1\u00ba\3\2\2\2\u00c1\u00bc\3\2\2\2\u00c1\u00bf\3"+
		"\2\2\2\u00c2\u00c3\3\2\2\2\u00c3\u00e4\5(\25\r\u00c4\u00c5\f\13\2\2\u00c5"+
		"\u00c6\t\7\2\2\u00c6\u00e4\5(\25\f\u00c7\u00c8\f\n\2\2\u00c8\u00c9\t\b"+
		"\2\2\u00c9\u00e4\5(\25\13\u00ca\u00cb\f\t\2\2\u00cb\u00cc\7\36\2\2\u00cc"+
		"\u00e4\5(\25\n\u00cd\u00ce\f\b\2\2\u00ce\u00cf\7\37\2\2\u00cf\u00e4\5"+
		"(\25\t\u00d0\u00d1\f\7\2\2\u00d1\u00d2\7 \2\2\u00d2\u00e4\5(\25\b\u00d3"+
		"\u00d4\f\6\2\2\u00d4\u00d5\7!\2\2\u00d5\u00e4\5(\25\7\u00d6\u00d7\f\5"+
		"\2\2\u00d7\u00d8\7\"\2\2\u00d8\u00e4\5(\25\6\u00d9\u00da\f\4\2\2\u00da"+
		"\u00db\7#\2\2\u00db\u00e4\5(\25\5\u00dc\u00dd\f\22\2\2\u00dd\u00de\7\13"+
		"\2\2\u00de\u00df\5\26\f\2\u00df\u00e0\7\f\2\2\u00e0\u00e4\3\2\2\2\u00e1"+
		"\u00e2\f\20\2\2\u00e2\u00e4\t\t\2\2\u00e3\u00b3\3\2\2\2\u00e3\u00b6\3"+
		"\2\2\2\u00e3\u00b9\3\2\2\2\u00e3\u00c4\3\2\2\2\u00e3\u00c7\3\2\2\2\u00e3"+
		"\u00ca\3\2\2\2\u00e3\u00cd\3\2\2\2\u00e3\u00d0\3\2\2\2\u00e3\u00d3\3\2"+
		"\2\2\u00e3\u00d6\3\2\2\2\u00e3\u00d9\3\2\2\2\u00e3\u00dc\3\2\2\2\u00e3"+
		"\u00e1\3\2\2\2\u00e4\u00e7\3\2\2\2\u00e5\u00e3\3\2\2\2\u00e5\u00e6\3\2"+
		"\2\2\u00e6)\3\2\2\2\u00e7\u00e5\3\2\2\2\22,9Eakps|\u0084\u008e\u0094\u0097"+
		"\u00b1\u00c1\u00e3\u00e5";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}