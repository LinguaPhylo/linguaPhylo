// Generated from Simulator.g4 by ANTLR 4.4
package james.parser;
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
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__34=1, T__33=2, T__32=3, T__31=4, T__30=5, T__29=6, T__28=7, T__27=8, 
		T__26=9, T__25=10, T__24=11, T__23=12, T__22=13, T__21=14, T__20=15, T__19=16, 
		T__18=17, T__17=18, T__16=19, T__15=20, T__14=21, T__13=22, T__12=23, 
		T__11=24, T__10=25, T__9=26, T__8=27, T__7=28, T__6=29, T__5=30, T__4=31, 
		T__3=32, T__2=33, T__1=34, T__0=35, VAR=36, DATA=37, MODEL=38, NAME=39, 
		ARROW=40, LENGTH=41, DIM=42, DECIMAL_LITERAL=43, HEX_LITERAL=44, OCT_LITERAL=45, 
		BINARY_LITERAL=46, FLOAT_LITERAL=47, HEX_FLOAT_LITERAL=48, TILDE=49, WS=50, 
		COMMENT=51, LINE_COMMENT=52, FUNC=53;
	public static final String[] tokenNames = {
		"<INVALID>", "'/'", "'T'", "'true'", "'!='", "'||'", "';'", "'{'", "'&&'", 
		"'**'", "'='", "'}'", "'^'", "'for'", "'<='", "'&'", "'('", "'I'", "'*'", 
		"','", "'false'", "':'", "'>='", "'['", "'|'", "'=='", "'<'", "'--'", 
		"'++'", "']'", "'>'", "'%'", "'in'", "')'", "'+'", "'-'", "'var'", "'data'", 
		"'model'", "NAME", "'<-'", "'length'", "'dim'", "DECIMAL_LITERAL", "HEX_LITERAL", 
		"OCT_LITERAL", "BINARY_LITERAL", "FLOAT_LITERAL", "HEX_FLOAT_LITERAL", 
		"'~'", "WS", "COMMENT", "LINE_COMMENT", "FUNC"
	};
	public static final int
		RULE_input = 0, RULE_relations = 1, RULE_relation_list = 2, RULE_relation = 3, 
		RULE_for_loop = 4, RULE_counter = 5, RULE_assignment = 6, RULE_determ_relation = 7, 
		RULE_stoch_relation = 8, RULE_truncated = 9, RULE_interval = 10, RULE_var = 11, 
		RULE_range_list = 12, RULE_range_element = 13, RULE_constant = 14, RULE_expression_list = 15, 
		RULE_methodCall = 16, RULE_distribution = 17, RULE_named_expression = 18, 
		RULE_expression = 19;
	public static final String[] ruleNames = {
		"input", "relations", "relation_list", "relation", "for_loop", "counter", 
		"assignment", "determ_relation", "stoch_relation", "truncated", "interval", 
		"var", "range_list", "range_element", "constant", "expression_list", "methodCall", 
		"distribution", "named_expression", "expression"
	};

	@Override
	public String getGrammarFileName() { return "Simulator.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

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
			switch (_input.LA(1)) {
			case EOF:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case T__22:
			case NAME:
			case FUNC:
				enterOuterAlt(_localctx, 2);
				{
				setState(41); relation_list(0);
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
			setState(44); match(T__28);
			setState(45); relation_list(0);
			setState(46); match(T__24);
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
			setState(49); relation();
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
					setState(52); relation();
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
		public Determ_relationContext determ_relation() {
			return getRuleContext(Determ_relationContext.class,0);
		}
		public For_loopContext for_loop() {
			return getRuleContext(For_loopContext.class,0);
		}
		public Stoch_relationContext stoch_relation() {
			return getRuleContext(Stoch_relationContext.class,0);
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
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(58); stoch_relation();
				setState(59); match(T__29);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(61); determ_relation();
				setState(62); match(T__29);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(64); for_loop();
				setState(65); match(T__29);
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
		public RelationsContext relations() {
			return getRuleContext(RelationsContext.class,0);
		}
		public CounterContext counter() {
			return getRuleContext(CounterContext.class,0);
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
			setState(69); counter();
			setState(70); relations();
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
			setState(72); match(T__22);
			setState(73); match(T__19);
			setState(74); match(NAME);
			setState(75); match(T__3);
			setState(76); range_element();
			setState(77); match(T__2);
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
			if ( !(_la==T__25 || _la==ARROW) ) {
			_errHandler.recoverInline(this);
			}
			consume();
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
		public AssignmentContext assignment() {
			return getRuleContext(AssignmentContext.class,0);
		}
		public TerminalNode NAME() { return getToken(SimulatorParser.NAME, 0); }
		public VarContext var() {
			return getRuleContext(VarContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode FUNC() { return getToken(SimulatorParser.FUNC, 0); }
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
			setState(92);
			switch (_input.LA(1)) {
			case NAME:
				enterOuterAlt(_localctx, 1);
				{
				setState(81); var();
				setState(82); assignment();
				setState(83); expression(0);
				}
				break;
			case FUNC:
				enterOuterAlt(_localctx, 2);
				{
				setState(85); match(FUNC);
				setState(86); match(T__19);
				setState(87); match(NAME);
				setState(88); match(T__2);
				setState(89); assignment();
				setState(90); expression(0);
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

	public static class Stoch_relationContext extends ParserRuleContext {
		public DistributionContext distribution() {
			return getRuleContext(DistributionContext.class,0);
		}
		public VarContext var() {
			return getRuleContext(VarContext.class,0);
		}
		public TerminalNode TILDE() { return getToken(SimulatorParser.TILDE, 0); }
		public IntervalContext interval() {
			return getRuleContext(IntervalContext.class,0);
		}
		public TruncatedContext truncated() {
			return getRuleContext(TruncatedContext.class,0);
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
			setState(108);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(94); var();
				setState(95); match(TILDE);
				setState(96); distribution();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(98); var();
				setState(99); match(TILDE);
				setState(100); distribution();
				setState(101); truncated();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(103); var();
				setState(104); match(TILDE);
				setState(105); distribution();
				setState(106); interval();
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

	public static class TruncatedContext extends ParserRuleContext {
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public TruncatedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_truncated; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterTruncated(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitTruncated(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitTruncated(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TruncatedContext truncated() throws RecognitionException {
		TruncatedContext _localctx = new TruncatedContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_truncated);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110); match(T__33);
			setState(111); match(T__19);
			setState(112); expression(0);
			setState(113); match(T__16);
			setState(114); expression(0);
			setState(115); match(T__2);
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

	public static class IntervalContext extends ParserRuleContext {
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public IntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_interval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).enterInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SimulatorListener ) ((SimulatorListener)listener).exitInterval(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SimulatorVisitor ) return ((SimulatorVisitor<? extends T>)visitor).visitInterval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntervalContext interval() throws RecognitionException {
		IntervalContext _localctx = new IntervalContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_interval);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117); match(T__18);
			setState(118); match(T__19);
			setState(119); expression(0);
			setState(120); match(T__16);
			setState(121); expression(0);
			setState(122); match(T__2);
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
		public Range_listContext range_list() {
			return getRuleContext(Range_listContext.class,0);
		}
		public TerminalNode NAME() { return getToken(SimulatorParser.NAME, 0); }
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
		enterRule(_localctx, 22, RULE_var);
		try {
			setState(130);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(124); match(NAME);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(125); match(NAME);
				setState(126); match(T__12);
				setState(127); range_list(0);
				setState(128); match(T__6);
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
		public Range_listContext range_list() {
			return getRuleContext(Range_listContext.class,0);
		}
		public Range_elementContext range_element() {
			return getRuleContext(Range_elementContext.class,0);
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
		int _startState = 24;
		enterRecursionRule(_localctx, 24, RULE_range_list, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(133); range_element();
			}
			_ctx.stop = _input.LT(-1);
			setState(140);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Range_listContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_range_list);
					setState(135);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(136); match(T__16);
					setState(137); range_element();
					}
					} 
				}
				setState(142);
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
		enterRule(_localctx, 26, RULE_range_element);
		try {
			setState(145);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(144); expression(0);
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
		public TerminalNode DECIMAL_LITERAL() { return getToken(SimulatorParser.DECIMAL_LITERAL, 0); }
		public TerminalNode FLOAT_LITERAL() { return getToken(SimulatorParser.FLOAT_LITERAL, 0); }
		public TerminalNode HEX_FLOAT_LITERAL() { return getToken(SimulatorParser.HEX_FLOAT_LITERAL, 0); }
		public TerminalNode HEX_LITERAL() { return getToken(SimulatorParser.HEX_LITERAL, 0); }
		public TerminalNode OCT_LITERAL() { return getToken(SimulatorParser.OCT_LITERAL, 0); }
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
		enterRule(_localctx, 28, RULE_constant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(148);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(147); match(T__0);
				}
			}

			setState(150);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__32) | (1L << T__15) | (1L << DECIMAL_LITERAL) | (1L << HEX_LITERAL) | (1L << OCT_LITERAL) | (1L << FLOAT_LITERAL) | (1L << HEX_FLOAT_LITERAL))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
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
		public Named_expressionContext named_expression(int i) {
			return getRuleContext(Named_expressionContext.class,i);
		}
		public List<Named_expressionContext> named_expression() {
			return getRuleContexts(Named_expressionContext.class);
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
		enterRule(_localctx, 30, RULE_expression_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(152); named_expression();
			setState(157);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__16) {
				{
				{
				setState(153); match(T__16);
				setState(154); named_expression();
				}
				}
				setState(159);
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

	public static class MethodCallContext extends ParserRuleContext {
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public TerminalNode NAME() { return getToken(SimulatorParser.NAME, 0); }
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
			enterOuterAlt(_localctx, 1);
			{
			setState(160); match(NAME);
			setState(161); match(T__19);
			setState(163);
			_la = _input.LA(1);
			if (_la==NAME) {
				{
				setState(162); expression_list();
				}
			}

			setState(165); match(T__2);
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
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public TerminalNode NAME() { return getToken(SimulatorParser.NAME, 0); }
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
			setState(167); match(NAME);
			setState(168); match(T__19);
			setState(169); expression_list();
			setState(170); match(T__2);
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
			setState(172); match(NAME);
			setState(173); match(T__25);
			setState(174); expression(0);
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
		public Range_listContext range_list() {
			return getRuleContext(Range_listContext.class,0);
		}
		public TerminalNode NAME() { return getToken(SimulatorParser.NAME, 0); }
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public MethodCallContext methodCall() {
			return getRuleContext(MethodCallContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
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
			setState(186);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(177);
				((ExpressionContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__8) | (1L << T__7) | (1L << T__1) | (1L << T__0))) != 0)) ) {
					((ExpressionContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				consume();
				setState(178); expression(12);
				}
				break;
			case 2:
				{
				setState(179); constant();
				}
				break;
			case 3:
				{
				setState(180); match(NAME);
				}
				break;
			case 4:
				{
				setState(181); match(T__19);
				setState(182); expression(0);
				setState(183); match(T__2);
				}
				break;
			case 5:
				{
				setState(185); methodCall();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(238);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(236);
					switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(188);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(189);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__34) | (1L << T__26) | (1L << T__17) | (1L << T__4))) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						consume();
						setState(190); expression(12);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(191);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(192);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__1 || _la==T__0) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						consume();
						setState(193); expression(11);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(194);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(202);
						switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
						case 1:
							{
							setState(195); match(T__9);
							setState(196); match(T__9);
							}
							break;
						case 2:
							{
							setState(197); match(T__5);
							setState(198); match(T__5);
							setState(199); match(T__5);
							}
							break;
						case 3:
							{
							setState(200); match(T__5);
							setState(201); match(T__5);
							}
							break;
						}
						setState(204); expression(10);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(205);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(206);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__21) | (1L << T__13) | (1L << T__9) | (1L << T__5))) != 0)) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						consume();
						setState(207); expression(9);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(208);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(209);
						((ExpressionContext)_localctx).bop = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__31 || _la==T__10) ) {
							((ExpressionContext)_localctx).bop = (Token)_errHandler.recoverInline(this);
						}
						consume();
						setState(210); expression(8);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(211);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(212); ((ExpressionContext)_localctx).bop = match(T__20);
						setState(213); expression(7);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(214);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(215); ((ExpressionContext)_localctx).bop = match(T__23);
						setState(216); expression(6);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(217);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(218); ((ExpressionContext)_localctx).bop = match(T__11);
						setState(219); expression(5);
						}
						break;
					case 9:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(220);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(221); ((ExpressionContext)_localctx).bop = match(T__27);
						setState(222); expression(4);
						}
						break;
					case 10:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(223);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(224); ((ExpressionContext)_localctx).bop = match(T__30);
						setState(225); expression(3);
						}
						break;
					case 11:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(226);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(227); ((ExpressionContext)_localctx).bop = match(T__14);
						setState(228); expression(2);
						}
						break;
					case 12:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(229);
						if (!(precpred(_ctx, 15))) throw new FailedPredicateException(this, "precpred(_ctx, 15)");
						setState(230); match(T__12);
						setState(231); range_list(0);
						setState(232); match(T__6);
						}
						break;
					case 13:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(234);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(235);
						((ExpressionContext)_localctx).postfix = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__8 || _la==T__7) ) {
							((ExpressionContext)_localctx).postfix = (Token)_errHandler.recoverInline(this);
						}
						consume();
						}
						break;
					}
					} 
				}
				setState(240);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
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
		case 2: return relation_list_sempred((Relation_listContext)_localctx, predIndex);
		case 12: return range_list_sempred((Range_listContext)_localctx, predIndex);
		case 19: return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean relation_list_sempred(Relation_listContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0: return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2: return precpred(_ctx, 11);
		case 3: return precpred(_ctx, 10);
		case 4: return precpred(_ctx, 9);
		case 5: return precpred(_ctx, 8);
		case 6: return precpred(_ctx, 7);
		case 7: return precpred(_ctx, 6);
		case 8: return precpred(_ctx, 5);
		case 9: return precpred(_ctx, 4);
		case 10: return precpred(_ctx, 3);
		case 11: return precpred(_ctx, 2);
		case 12: return precpred(_ctx, 1);
		case 13: return precpred(_ctx, 15);
		case 14: return precpred(_ctx, 13);
		}
		return true;
	}
	private boolean range_list_sempred(Range_listContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1: return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\67\u00f4\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\3\2\3\2\5\2-\n\2\3\3\3\3\3\3\3\3\3\4\3"+
		"\4\3\4\3\4\3\4\7\48\n\4\f\4\16\4;\13\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5"+
		"\3\5\5\5F\n\5\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\t\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t_\n\t\3\n\3\n\3\n\3\n\3\n\3\n"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\no\n\n\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\5\r\u0085\n"+
		"\r\3\16\3\16\3\16\3\16\3\16\3\16\7\16\u008d\n\16\f\16\16\16\u0090\13\16"+
		"\3\17\3\17\5\17\u0094\n\17\3\20\5\20\u0097\n\20\3\20\3\20\3\21\3\21\3"+
		"\21\7\21\u009e\n\21\f\21\16\21\u00a1\13\21\3\22\3\22\3\22\5\22\u00a6\n"+
		"\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\25\3\25\3"+
		"\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\5\25\u00bd\n\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\5\25\u00cd\n\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\3\25\7\25\u00ef\n\25\f\25\16\25\u00f2\13\25\3\25\2\5\6"+
		"\32(\26\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(\2\n\4\2\f\f**\6\2"+
		"\5\5\26\26-/\61\62\4\2\35\36$%\6\2\3\3\13\13\24\24!!\3\2$%\6\2\20\20\30"+
		"\30\34\34  \4\2\6\6\33\33\3\2\35\36\u00ff\2,\3\2\2\2\4.\3\2\2\2\6\62\3"+
		"\2\2\2\bE\3\2\2\2\nG\3\2\2\2\fJ\3\2\2\2\16Q\3\2\2\2\20^\3\2\2\2\22n\3"+
		"\2\2\2\24p\3\2\2\2\26w\3\2\2\2\30\u0084\3\2\2\2\32\u0086\3\2\2\2\34\u0093"+
		"\3\2\2\2\36\u0096\3\2\2\2 \u009a\3\2\2\2\"\u00a2\3\2\2\2$\u00a9\3\2\2"+
		"\2&\u00ae\3\2\2\2(\u00bc\3\2\2\2*-\3\2\2\2+-\5\6\4\2,*\3\2\2\2,+\3\2\2"+
		"\2-\3\3\2\2\2./\7\t\2\2/\60\5\6\4\2\60\61\7\r\2\2\61\5\3\2\2\2\62\63\b"+
		"\4\1\2\63\64\5\b\5\2\649\3\2\2\2\65\66\f\3\2\2\668\5\b\5\2\67\65\3\2\2"+
		"\28;\3\2\2\29\67\3\2\2\29:\3\2\2\2:\7\3\2\2\2;9\3\2\2\2<=\5\22\n\2=>\7"+
		"\b\2\2>F\3\2\2\2?@\5\20\t\2@A\7\b\2\2AF\3\2\2\2BC\5\n\6\2CD\7\b\2\2DF"+
		"\3\2\2\2E<\3\2\2\2E?\3\2\2\2EB\3\2\2\2F\t\3\2\2\2GH\5\f\7\2HI\5\4\3\2"+
		"I\13\3\2\2\2JK\7\17\2\2KL\7\22\2\2LM\7)\2\2MN\7\"\2\2NO\5\34\17\2OP\7"+
		"#\2\2P\r\3\2\2\2QR\t\2\2\2R\17\3\2\2\2ST\5\30\r\2TU\5\16\b\2UV\5(\25\2"+
		"V_\3\2\2\2WX\7\67\2\2XY\7\22\2\2YZ\7)\2\2Z[\7#\2\2[\\\5\16\b\2\\]\5(\25"+
		"\2]_\3\2\2\2^S\3\2\2\2^W\3\2\2\2_\21\3\2\2\2`a\5\30\r\2ab\7\63\2\2bc\5"+
		"$\23\2co\3\2\2\2de\5\30\r\2ef\7\63\2\2fg\5$\23\2gh\5\24\13\2ho\3\2\2\2"+
		"ij\5\30\r\2jk\7\63\2\2kl\5$\23\2lm\5\26\f\2mo\3\2\2\2n`\3\2\2\2nd\3\2"+
		"\2\2ni\3\2\2\2o\23\3\2\2\2pq\7\4\2\2qr\7\22\2\2rs\5(\25\2st\7\25\2\2t"+
		"u\5(\25\2uv\7#\2\2v\25\3\2\2\2wx\7\23\2\2xy\7\22\2\2yz\5(\25\2z{\7\25"+
		"\2\2{|\5(\25\2|}\7#\2\2}\27\3\2\2\2~\u0085\7)\2\2\177\u0080\7)\2\2\u0080"+
		"\u0081\7\31\2\2\u0081\u0082\5\32\16\2\u0082\u0083\7\37\2\2\u0083\u0085"+
		"\3\2\2\2\u0084~\3\2\2\2\u0084\177\3\2\2\2\u0085\31\3\2\2\2\u0086\u0087"+
		"\b\16\1\2\u0087\u0088\5\34\17\2\u0088\u008e\3\2\2\2\u0089\u008a\f\3\2"+
		"\2\u008a\u008b\7\25\2\2\u008b\u008d\5\34\17\2\u008c\u0089\3\2\2\2\u008d"+
		"\u0090\3\2\2\2\u008e\u008c\3\2\2\2\u008e\u008f\3\2\2\2\u008f\33\3\2\2"+
		"\2\u0090\u008e\3\2\2\2\u0091\u0094\3\2\2\2\u0092\u0094\5(\25\2\u0093\u0091"+
		"\3\2\2\2\u0093\u0092\3\2\2\2\u0094\35\3\2\2\2\u0095\u0097\7%\2\2\u0096"+
		"\u0095\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u0098\3\2\2\2\u0098\u0099\t\3"+
		"\2\2\u0099\37\3\2\2\2\u009a\u009f\5&\24\2\u009b\u009c\7\25\2\2\u009c\u009e"+
		"\5&\24\2\u009d\u009b\3\2\2\2\u009e\u00a1\3\2\2\2\u009f\u009d\3\2\2\2\u009f"+
		"\u00a0\3\2\2\2\u00a0!\3\2\2\2\u00a1\u009f\3\2\2\2\u00a2\u00a3\7)\2\2\u00a3"+
		"\u00a5\7\22\2\2\u00a4\u00a6\5 \21\2\u00a5\u00a4\3\2\2\2\u00a5\u00a6\3"+
		"\2\2\2\u00a6\u00a7\3\2\2\2\u00a7\u00a8\7#\2\2\u00a8#\3\2\2\2\u00a9\u00aa"+
		"\7)\2\2\u00aa\u00ab\7\22\2\2\u00ab\u00ac\5 \21\2\u00ac\u00ad\7#\2\2\u00ad"+
		"%\3\2\2\2\u00ae\u00af\7)\2\2\u00af\u00b0\7\f\2\2\u00b0\u00b1\5(\25\2\u00b1"+
		"\'\3\2\2\2\u00b2\u00b3\b\25\1\2\u00b3\u00b4\t\4\2\2\u00b4\u00bd\5(\25"+
		"\16\u00b5\u00bd\5\36\20\2\u00b6\u00bd\7)\2\2\u00b7\u00b8\7\22\2\2\u00b8"+
		"\u00b9\5(\25\2\u00b9\u00ba\7#\2\2\u00ba\u00bd\3\2\2\2\u00bb\u00bd\5\""+
		"\22\2\u00bc\u00b2\3\2\2\2\u00bc\u00b5\3\2\2\2\u00bc\u00b6\3\2\2\2\u00bc"+
		"\u00b7\3\2\2\2\u00bc\u00bb\3\2\2\2\u00bd\u00f0\3\2\2\2\u00be\u00bf\f\r"+
		"\2\2\u00bf\u00c0\t\5\2\2\u00c0\u00ef\5(\25\16\u00c1\u00c2\f\f\2\2\u00c2"+
		"\u00c3\t\6\2\2\u00c3\u00ef\5(\25\r\u00c4\u00cc\f\13\2\2\u00c5\u00c6\7"+
		"\34\2\2\u00c6\u00cd\7\34\2\2\u00c7\u00c8\7 \2\2\u00c8\u00c9\7 \2\2\u00c9"+
		"\u00cd\7 \2\2\u00ca\u00cb\7 \2\2\u00cb\u00cd\7 \2\2\u00cc\u00c5\3\2\2"+
		"\2\u00cc\u00c7\3\2\2\2\u00cc\u00ca\3\2\2\2\u00cd\u00ce\3\2\2\2\u00ce\u00ef"+
		"\5(\25\f\u00cf\u00d0\f\n\2\2\u00d0\u00d1\t\7\2\2\u00d1\u00ef\5(\25\13"+
		"\u00d2\u00d3\f\t\2\2\u00d3\u00d4\t\b\2\2\u00d4\u00ef\5(\25\n\u00d5\u00d6"+
		"\f\b\2\2\u00d6\u00d7\7\21\2\2\u00d7\u00ef\5(\25\t\u00d8\u00d9\f\7\2\2"+
		"\u00d9\u00da\7\16\2\2\u00da\u00ef\5(\25\b\u00db\u00dc\f\6\2\2\u00dc\u00dd"+
		"\7\32\2\2\u00dd\u00ef\5(\25\7\u00de\u00df\f\5\2\2\u00df\u00e0\7\n\2\2"+
		"\u00e0\u00ef\5(\25\6\u00e1\u00e2\f\4\2\2\u00e2\u00e3\7\7\2\2\u00e3\u00ef"+
		"\5(\25\5\u00e4\u00e5\f\3\2\2\u00e5\u00e6\7\27\2\2\u00e6\u00ef\5(\25\4"+
		"\u00e7\u00e8\f\21\2\2\u00e8\u00e9\7\31\2\2\u00e9\u00ea\5\32\16\2\u00ea"+
		"\u00eb\7\37\2\2\u00eb\u00ef\3\2\2\2\u00ec\u00ed\f\17\2\2\u00ed\u00ef\t"+
		"\t\2\2\u00ee\u00be\3\2\2\2\u00ee\u00c1\3\2\2\2\u00ee\u00c4\3\2\2\2\u00ee"+
		"\u00cf\3\2\2\2\u00ee\u00d2\3\2\2\2\u00ee\u00d5\3\2\2\2\u00ee\u00d8\3\2"+
		"\2\2\u00ee\u00db\3\2\2\2\u00ee\u00de\3\2\2\2\u00ee\u00e1\3\2\2\2\u00ee"+
		"\u00e4\3\2\2\2\u00ee\u00e7\3\2\2\2\u00ee\u00ec\3\2\2\2\u00ef\u00f2\3\2"+
		"\2\2\u00f0\u00ee\3\2\2\2\u00f0\u00f1\3\2\2\2\u00f1)\3\2\2\2\u00f2\u00f0"+
		"\3\2\2\2\21,9E^n\u0084\u008e\u0093\u0096\u009f\u00a5\u00bc\u00cc\u00ee"+
		"\u00f0";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}