// Generated from /Users/alexeidrummond/Git/graphicalModelSimulation/src/lphy/parser/DataModel.g4 by ANTLR 4.8
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
public class DataModelParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, DATA=34, MODEL=35, NAME=36, ARROW=37, LENGTH=38, DIM=39, 
		DECIMAL_LITERAL=40, HEX_LITERAL=41, OCT_LITERAL=42, BINARY_LITERAL=43, 
		FLOAT_LITERAL=44, HEX_FLOAT_LITERAL=45, STRING_LITERAL=46, TILDE=47, WS=48, 
		COMMENT=49, LINE_COMMENT=50;
	public static final int
		RULE_input = 0, RULE_datablock = 1, RULE_modelblock = 2, RULE_relations = 3, 
		RULE_relation_list = 4, RULE_relation = 5, RULE_for_loop = 6, RULE_counter = 7, 
		RULE_assignment = 8, RULE_determ_relation = 9, RULE_stoch_relation = 10, 
		RULE_var = 11, RULE_range_list = 12, RULE_range_element = 13, RULE_constant = 14, 
		RULE_expression_list = 15, RULE_unnamed_expression_list = 16, RULE_mapFunction = 17, 
		RULE_methodCall = 18, RULE_distribution = 19, RULE_named_expression = 20, 
		RULE_expression = 21;
	private static String[] makeRuleNames() {
		return new String[] {
			"input", "datablock", "modelblock", "relations", "relation_list", "relation", 
			"for_loop", "counter", "assignment", "determ_relation", "stoch_relation", 
			"var", "range_list", "range_element", "constant", "expression_list", 
			"unnamed_expression_list", "mapFunction", "methodCall", "distribution", 
			"named_expression", "expression"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "';'", "'for'", "'('", "'in'", "')'", "'='", "'['", 
			"']'", "','", "'-'", "'true'", "'false'", "'++'", "'--'", "'+'", "'**'", 
			"'*'", "'/'", "'%'", "'<'", "'>'", "'<='", "'>='", "'=='", "'!='", "'&'", 
			"'^'", "'|'", "'&&'", "'||'", "':'", "'data'", "'model'", null, "'<-'", 
			"'length'", "'dim'", null, null, null, null, null, null, null, "'~'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, "DATA", "MODEL", 
			"NAME", "ARROW", "LENGTH", "DIM", "DECIMAL_LITERAL", "HEX_LITERAL", "OCT_LITERAL", 
			"BINARY_LITERAL", "FLOAT_LITERAL", "HEX_FLOAT_LITERAL", "STRING_LITERAL", 
			"TILDE", "WS", "COMMENT", "LINE_COMMENT"
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
	public String getGrammarFileName() { return "DataModel.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public DataModelParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class InputContext extends ParserRuleContext {
		public DatablockContext datablock() {
			return getRuleContext(DatablockContext.class,0);
		}
		public ModelblockContext modelblock() {
			return getRuleContext(ModelblockContext.class,0);
		}
		public InputContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_input; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterInput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitInput(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitInput(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InputContext input() throws RecognitionException {
		InputContext _localctx = new InputContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_input);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(45);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DATA) {
				{
				setState(44);
				datablock();
				}
			}

			setState(48);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==MODEL) {
				{
				setState(47);
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

	public static class DatablockContext extends ParserRuleContext {
		public TerminalNode DATA() { return getToken(DataModelParser.DATA, 0); }
		public RelationsContext relations() {
			return getRuleContext(RelationsContext.class,0);
		}
		public DatablockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_datablock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterDatablock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitDatablock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitDatablock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DatablockContext datablock() throws RecognitionException {
		DatablockContext _localctx = new DatablockContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_datablock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			match(DATA);
			setState(51);
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

	public static class ModelblockContext extends ParserRuleContext {
		public TerminalNode MODEL() { return getToken(DataModelParser.MODEL, 0); }
		public RelationsContext relations() {
			return getRuleContext(RelationsContext.class,0);
		}
		public ModelblockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modelblock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterModelblock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitModelblock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitModelblock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ModelblockContext modelblock() throws RecognitionException {
		ModelblockContext _localctx = new ModelblockContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_modelblock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			match(MODEL);
			setState(54);
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
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterRelations(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitRelations(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitRelations(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationsContext relations() throws RecognitionException {
		RelationsContext _localctx = new RelationsContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_relations);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(56);
			match(T__0);
			setState(58);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3 || _la==NAME) {
				{
				setState(57);
				relation_list(0);
				}
			}

			setState(60);
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
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterRelation_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitRelation_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitRelation_list(this);
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
		int _startState = 8;
		enterRecursionRule(_localctx, 8, RULE_relation_list, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(63);
			relation();
			}
			_ctx.stop = _input.LT(-1);
			setState(69);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new Relation_listContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_relation_list);
					setState(65);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(66);
					relation();
					}
					} 
				}
				setState(71);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
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
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitRelation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitRelation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationContext relation() throws RecognitionException {
		RelationContext _localctx = new RelationContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_relation);
		try {
			setState(81);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(72);
				stoch_relation();
				setState(73);
				match(T__2);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(75);
				determ_relation();
				setState(76);
				match(T__2);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(78);
				for_loop();
				setState(79);
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
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterFor_loop(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitFor_loop(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitFor_loop(this);
			else return visitor.visitChildren(this);
		}
	}

	public final For_loopContext for_loop() throws RecognitionException {
		For_loopContext _localctx = new For_loopContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_for_loop);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			counter();
			setState(84);
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
		public TerminalNode NAME() { return getToken(DataModelParser.NAME, 0); }
		public Range_elementContext range_element() {
			return getRuleContext(Range_elementContext.class,0);
		}
		public CounterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_counter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterCounter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitCounter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitCounter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CounterContext counter() throws RecognitionException {
		CounterContext _localctx = new CounterContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_counter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(86);
			match(T__3);
			setState(87);
			match(T__4);
			setState(88);
			match(NAME);
			setState(89);
			match(T__5);
			setState(90);
			range_element();
			setState(91);
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
		public TerminalNode ARROW() { return getToken(DataModelParser.ARROW, 0); }
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitAssignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_assignment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
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
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterDeterm_relation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitDeterm_relation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitDeterm_relation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Determ_relationContext determ_relation() throws RecognitionException {
		Determ_relationContext _localctx = new Determ_relationContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_determ_relation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(95);
			var();
			setState(96);
			assignment();
			setState(97);
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
		public TerminalNode TILDE() { return getToken(DataModelParser.TILDE, 0); }
		public DistributionContext distribution() {
			return getRuleContext(DistributionContext.class,0);
		}
		public Stoch_relationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stoch_relation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterStoch_relation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitStoch_relation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitStoch_relation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Stoch_relationContext stoch_relation() throws RecognitionException {
		Stoch_relationContext _localctx = new Stoch_relationContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_stoch_relation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			var();
			setState(100);
			match(TILDE);
			setState(101);
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
		public TerminalNode NAME() { return getToken(DataModelParser.NAME, 0); }
		public Range_listContext range_list() {
			return getRuleContext(Range_listContext.class,0);
		}
		public VarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_var; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitVar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitVar(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarContext var() throws RecognitionException {
		VarContext _localctx = new VarContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_var);
		try {
			setState(109);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(103);
				match(NAME);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(104);
				match(NAME);
				setState(105);
				match(T__8);
				setState(106);
				range_list(0);
				setState(107);
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
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterRange_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitRange_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitRange_list(this);
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
			setState(112);
			range_element();
			}
			_ctx.stop = _input.LT(-1);
			setState(119);
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
					setState(114);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(115);
					match(T__10);
					setState(116);
					range_element();
					}
					} 
				}
				setState(121);
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
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterRange_element(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitRange_element(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitRange_element(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Range_elementContext range_element() throws RecognitionException {
		Range_elementContext _localctx = new Range_elementContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_range_element);
		try {
			setState(124);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(123);
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
		public TerminalNode FLOAT_LITERAL() { return getToken(DataModelParser.FLOAT_LITERAL, 0); }
		public TerminalNode DECIMAL_LITERAL() { return getToken(DataModelParser.DECIMAL_LITERAL, 0); }
		public TerminalNode OCT_LITERAL() { return getToken(DataModelParser.OCT_LITERAL, 0); }
		public TerminalNode HEX_LITERAL() { return getToken(DataModelParser.HEX_LITERAL, 0); }
		public TerminalNode HEX_FLOAT_LITERAL() { return getToken(DataModelParser.HEX_FLOAT_LITERAL, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(DataModelParser.STRING_LITERAL, 0); }
		public ConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitConstant(this);
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
			setState(127);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__11) {
				{
				setState(126);
				match(T__11);
				}
			}

			setState(129);
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
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterExpression_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitExpression_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitExpression_list(this);
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
			setState(131);
			named_expression();
			setState(136);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__10) {
				{
				{
				setState(132);
				match(T__10);
				setState(133);
				named_expression();
				}
				}
				setState(138);
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
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterUnnamed_expression_list(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitUnnamed_expression_list(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitUnnamed_expression_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Unnamed_expression_listContext unnamed_expression_list() throws RecognitionException {
		Unnamed_expression_listContext _localctx = new Unnamed_expression_listContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_unnamed_expression_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			expression(0);
			setState(144);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__10) {
				{
				{
				setState(140);
				match(T__10);
				setState(141);
				expression(0);
				}
				}
				setState(146);
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
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterMapFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitMapFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitMapFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MapFunctionContext mapFunction() throws RecognitionException {
		MapFunctionContext _localctx = new MapFunctionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_mapFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
			match(T__0);
			setState(148);
			expression_list();
			setState(149);
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
		public TerminalNode NAME() { return getToken(DataModelParser.NAME, 0); }
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
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterMethodCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitMethodCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitMethodCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodCallContext methodCall() throws RecognitionException {
		MethodCallContext _localctx = new MethodCallContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_methodCall);
		int _la;
		try {
			setState(163);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(151);
				match(NAME);
				setState(152);
				match(T__4);
				setState(154);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NAME) {
					{
					setState(153);
					expression_list();
					}
				}

				setState(156);
				match(T__6);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(157);
				match(NAME);
				setState(158);
				match(T__4);
				setState(160);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__4) | (1L << T__8) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << T__14) | (1L << T__15) | (1L << T__16) | (1L << NAME) | (1L << DECIMAL_LITERAL) | (1L << HEX_LITERAL) | (1L << OCT_LITERAL) | (1L << FLOAT_LITERAL) | (1L << HEX_FLOAT_LITERAL) | (1L << STRING_LITERAL))) != 0)) {
					{
					setState(159);
					unnamed_expression_list();
					}
				}

				setState(162);
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
		public TerminalNode NAME() { return getToken(DataModelParser.NAME, 0); }
		public Expression_listContext expression_list() {
			return getRuleContext(Expression_listContext.class,0);
		}
		public DistributionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_distribution; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterDistribution(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitDistribution(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitDistribution(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DistributionContext distribution() throws RecognitionException {
		DistributionContext _localctx = new DistributionContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_distribution);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
			match(NAME);
			setState(166);
			match(T__4);
			setState(167);
			expression_list();
			setState(168);
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
		public TerminalNode NAME() { return getToken(DataModelParser.NAME, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Named_expressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_named_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterNamed_expression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitNamed_expression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitNamed_expression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Named_expressionContext named_expression() throws RecognitionException {
		Named_expressionContext _localctx = new Named_expressionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_named_expression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(170);
			match(NAME);
			setState(171);
			match(T__7);
			setState(172);
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
		public TerminalNode NAME() { return getToken(DataModelParser.NAME, 0); }
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
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof DataModelListener ) ((DataModelListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof DataModelVisitor ) return ((DataModelVisitor<? extends T>)visitor).visitExpression(this);
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
		int _startState = 42;
		enterRecursionRule(_localctx, 42, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(175);
				constant();
				}
				break;
			case 2:
				{
				setState(176);
				match(NAME);
				}
				break;
			case 3:
				{
				setState(177);
				match(T__4);
				setState(178);
				expression(0);
				setState(179);
				match(T__6);
				}
				break;
			case 4:
				{
				setState(181);
				match(T__8);
				setState(182);
				unnamed_expression_list();
				setState(183);
				match(T__9);
				}
				break;
			case 5:
				{
				setState(185);
				methodCall();
				}
				break;
			case 6:
				{
				setState(186);
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
				setState(187);
				expression(13);
				}
				break;
			case 7:
				{
				setState(188);
				mapFunction();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(241);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(239);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(191);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(192);
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
						setState(193);
						expression(13);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(194);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(195);
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
						setState(196);
						expression(12);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(197);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(205);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
						case 1:
							{
							setState(198);
							match(T__21);
							setState(199);
							match(T__21);
							}
							break;
						case 2:
							{
							setState(200);
							match(T__22);
							setState(201);
							match(T__22);
							setState(202);
							match(T__22);
							}
							break;
						case 3:
							{
							setState(203);
							match(T__22);
							setState(204);
							match(T__22);
							}
							break;
						}
						setState(207);
						expression(11);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(208);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(209);
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
						setState(210);
						expression(10);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(211);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(212);
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
						setState(213);
						expression(9);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(214);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(215);
						((ExpressionContext)_localctx).bop = match(T__27);
						setState(216);
						expression(8);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(217);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(218);
						((ExpressionContext)_localctx).bop = match(T__28);
						setState(219);
						expression(7);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(220);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(221);
						((ExpressionContext)_localctx).bop = match(T__29);
						setState(222);
						expression(6);
						}
						break;
					case 9:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(223);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(224);
						((ExpressionContext)_localctx).bop = match(T__30);
						setState(225);
						expression(5);
						}
						break;
					case 10:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(226);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(227);
						((ExpressionContext)_localctx).bop = match(T__31);
						setState(228);
						expression(4);
						}
						break;
					case 11:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(229);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(230);
						((ExpressionContext)_localctx).bop = match(T__32);
						setState(231);
						expression(3);
						}
						break;
					case 12:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(232);
						if (!(precpred(_ctx, 16))) throw new FailedPredicateException(this, "precpred(_ctx, 16)");
						setState(233);
						match(T__8);
						setState(234);
						range_list(0);
						setState(235);
						match(T__9);
						}
						break;
					case 13:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(237);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(238);
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
				setState(243);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
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
			return relation_list_sempred((Relation_listContext)_localctx, predIndex);
		case 12:
			return range_list_sempred((Range_listContext)_localctx, predIndex);
		case 21:
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\64\u00f7\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\3\2\5\2\60\n\2\3\2"+
		"\5\2\63\n\2\3\3\3\3\3\3\3\4\3\4\3\4\3\5\3\5\5\5=\n\5\3\5\3\5\3\6\3\6\3"+
		"\6\3\6\3\6\7\6F\n\6\f\6\16\6I\13\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\5\7T\n\7\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\13\3\13\3"+
		"\13\3\13\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\r\5\rp\n\r\3\16\3\16\3"+
		"\16\3\16\3\16\3\16\7\16x\n\16\f\16\16\16{\13\16\3\17\3\17\5\17\177\n\17"+
		"\3\20\5\20\u0082\n\20\3\20\3\20\3\21\3\21\3\21\7\21\u0089\n\21\f\21\16"+
		"\21\u008c\13\21\3\22\3\22\3\22\7\22\u0091\n\22\f\22\16\22\u0094\13\22"+
		"\3\23\3\23\3\23\3\23\3\24\3\24\3\24\5\24\u009d\n\24\3\24\3\24\3\24\3\24"+
		"\5\24\u00a3\n\24\3\24\5\24\u00a6\n\24\3\25\3\25\3\25\3\25\3\25\3\26\3"+
		"\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3"+
		"\27\3\27\3\27\3\27\5\27\u00c0\n\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\27\3\27\3\27\5\27\u00d0\n\27\3\27\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\7\27\u00f2\n\27\f\27\16\27\u00f5\13\27\3\27\2\5\n\32,\30\2\4\6\b\n\f"+
		"\16\20\22\24\26\30\32\34\36 \"$&(*,\2\n\4\2\n\n\'\'\5\2\17\20*,.\60\4"+
		"\2\16\16\21\23\3\2\24\27\4\2\16\16\23\23\3\2\30\33\3\2\34\35\3\2\21\22"+
		"\2\u0104\2/\3\2\2\2\4\64\3\2\2\2\6\67\3\2\2\2\b:\3\2\2\2\n@\3\2\2\2\f"+
		"S\3\2\2\2\16U\3\2\2\2\20X\3\2\2\2\22_\3\2\2\2\24a\3\2\2\2\26e\3\2\2\2"+
		"\30o\3\2\2\2\32q\3\2\2\2\34~\3\2\2\2\36\u0081\3\2\2\2 \u0085\3\2\2\2\""+
		"\u008d\3\2\2\2$\u0095\3\2\2\2&\u00a5\3\2\2\2(\u00a7\3\2\2\2*\u00ac\3\2"+
		"\2\2,\u00bf\3\2\2\2.\60\5\4\3\2/.\3\2\2\2/\60\3\2\2\2\60\62\3\2\2\2\61"+
		"\63\5\6\4\2\62\61\3\2\2\2\62\63\3\2\2\2\63\3\3\2\2\2\64\65\7$\2\2\65\66"+
		"\5\b\5\2\66\5\3\2\2\2\678\7%\2\289\5\b\5\29\7\3\2\2\2:<\7\3\2\2;=\5\n"+
		"\6\2<;\3\2\2\2<=\3\2\2\2=>\3\2\2\2>?\7\4\2\2?\t\3\2\2\2@A\b\6\1\2AB\5"+
		"\f\7\2BG\3\2\2\2CD\f\3\2\2DF\5\f\7\2EC\3\2\2\2FI\3\2\2\2GE\3\2\2\2GH\3"+
		"\2\2\2H\13\3\2\2\2IG\3\2\2\2JK\5\26\f\2KL\7\5\2\2LT\3\2\2\2MN\5\24\13"+
		"\2NO\7\5\2\2OT\3\2\2\2PQ\5\16\b\2QR\7\5\2\2RT\3\2\2\2SJ\3\2\2\2SM\3\2"+
		"\2\2SP\3\2\2\2T\r\3\2\2\2UV\5\20\t\2VW\5\b\5\2W\17\3\2\2\2XY\7\6\2\2Y"+
		"Z\7\7\2\2Z[\7&\2\2[\\\7\b\2\2\\]\5\34\17\2]^\7\t\2\2^\21\3\2\2\2_`\t\2"+
		"\2\2`\23\3\2\2\2ab\5\30\r\2bc\5\22\n\2cd\5,\27\2d\25\3\2\2\2ef\5\30\r"+
		"\2fg\7\61\2\2gh\5(\25\2h\27\3\2\2\2ip\7&\2\2jk\7&\2\2kl\7\13\2\2lm\5\32"+
		"\16\2mn\7\f\2\2np\3\2\2\2oi\3\2\2\2oj\3\2\2\2p\31\3\2\2\2qr\b\16\1\2r"+
		"s\5\34\17\2sy\3\2\2\2tu\f\3\2\2uv\7\r\2\2vx\5\34\17\2wt\3\2\2\2x{\3\2"+
		"\2\2yw\3\2\2\2yz\3\2\2\2z\33\3\2\2\2{y\3\2\2\2|\177\3\2\2\2}\177\5,\27"+
		"\2~|\3\2\2\2~}\3\2\2\2\177\35\3\2\2\2\u0080\u0082\7\16\2\2\u0081\u0080"+
		"\3\2\2\2\u0081\u0082\3\2\2\2\u0082\u0083\3\2\2\2\u0083\u0084\t\3\2\2\u0084"+
		"\37\3\2\2\2\u0085\u008a\5*\26\2\u0086\u0087\7\r\2\2\u0087\u0089\5*\26"+
		"\2\u0088\u0086\3\2\2\2\u0089\u008c\3\2\2\2\u008a\u0088\3\2\2\2\u008a\u008b"+
		"\3\2\2\2\u008b!\3\2\2\2\u008c\u008a\3\2\2\2\u008d\u0092\5,\27\2\u008e"+
		"\u008f\7\r\2\2\u008f\u0091\5,\27\2\u0090\u008e\3\2\2\2\u0091\u0094\3\2"+
		"\2\2\u0092\u0090\3\2\2\2\u0092\u0093\3\2\2\2\u0093#\3\2\2\2\u0094\u0092"+
		"\3\2\2\2\u0095\u0096\7\3\2\2\u0096\u0097\5 \21\2\u0097\u0098\7\4\2\2\u0098"+
		"%\3\2\2\2\u0099\u009a\7&\2\2\u009a\u009c\7\7\2\2\u009b\u009d\5 \21\2\u009c"+
		"\u009b\3\2\2\2\u009c\u009d\3\2\2\2\u009d\u009e\3\2\2\2\u009e\u00a6\7\t"+
		"\2\2\u009f\u00a0\7&\2\2\u00a0\u00a2\7\7\2\2\u00a1\u00a3\5\"\22\2\u00a2"+
		"\u00a1\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3\u00a4\3\2\2\2\u00a4\u00a6\7\t"+
		"\2\2\u00a5\u0099\3\2\2\2\u00a5\u009f\3\2\2\2\u00a6\'\3\2\2\2\u00a7\u00a8"+
		"\7&\2\2\u00a8\u00a9\7\7\2\2\u00a9\u00aa\5 \21\2\u00aa\u00ab\7\t\2\2\u00ab"+
		")\3\2\2\2\u00ac\u00ad\7&\2\2\u00ad\u00ae\7\n\2\2\u00ae\u00af\5,\27\2\u00af"+
		"+\3\2\2\2\u00b0\u00b1\b\27\1\2\u00b1\u00c0\5\36\20\2\u00b2\u00c0\7&\2"+
		"\2\u00b3\u00b4\7\7\2\2\u00b4\u00b5\5,\27\2\u00b5\u00b6\7\t\2\2\u00b6\u00c0"+
		"\3\2\2\2\u00b7\u00b8\7\13\2\2\u00b8\u00b9\5\"\22\2\u00b9\u00ba\7\f\2\2"+
		"\u00ba\u00c0\3\2\2\2\u00bb\u00c0\5&\24\2\u00bc\u00bd\t\4\2\2\u00bd\u00c0"+
		"\5,\27\17\u00be\u00c0\5$\23\2\u00bf\u00b0\3\2\2\2\u00bf\u00b2\3\2\2\2"+
		"\u00bf\u00b3\3\2\2\2\u00bf\u00b7\3\2\2\2\u00bf\u00bb\3\2\2\2\u00bf\u00bc"+
		"\3\2\2\2\u00bf\u00be\3\2\2\2\u00c0\u00f3\3\2\2\2\u00c1\u00c2\f\16\2\2"+
		"\u00c2\u00c3\t\5\2\2\u00c3\u00f2\5,\27\17\u00c4\u00c5\f\r\2\2\u00c5\u00c6"+
		"\t\6\2\2\u00c6\u00f2\5,\27\16\u00c7\u00cf\f\f\2\2\u00c8\u00c9\7\30\2\2"+
		"\u00c9\u00d0\7\30\2\2\u00ca\u00cb\7\31\2\2\u00cb\u00cc\7\31\2\2\u00cc"+
		"\u00d0\7\31\2\2\u00cd\u00ce\7\31\2\2\u00ce\u00d0\7\31\2\2\u00cf\u00c8"+
		"\3\2\2\2\u00cf\u00ca\3\2\2\2\u00cf\u00cd\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1"+
		"\u00f2\5,\27\r\u00d2\u00d3\f\13\2\2\u00d3\u00d4\t\7\2\2\u00d4\u00f2\5"+
		",\27\f\u00d5\u00d6\f\n\2\2\u00d6\u00d7\t\b\2\2\u00d7\u00f2\5,\27\13\u00d8"+
		"\u00d9\f\t\2\2\u00d9\u00da\7\36\2\2\u00da\u00f2\5,\27\n\u00db\u00dc\f"+
		"\b\2\2\u00dc\u00dd\7\37\2\2\u00dd\u00f2\5,\27\t\u00de\u00df\f\7\2\2\u00df"+
		"\u00e0\7 \2\2\u00e0\u00f2\5,\27\b\u00e1\u00e2\f\6\2\2\u00e2\u00e3\7!\2"+
		"\2\u00e3\u00f2\5,\27\7\u00e4\u00e5\f\5\2\2\u00e5\u00e6\7\"\2\2\u00e6\u00f2"+
		"\5,\27\6\u00e7\u00e8\f\4\2\2\u00e8\u00e9\7#\2\2\u00e9\u00f2\5,\27\5\u00ea"+
		"\u00eb\f\22\2\2\u00eb\u00ec\7\13\2\2\u00ec\u00ed\5\32\16\2\u00ed\u00ee"+
		"\7\f\2\2\u00ee\u00f2\3\2\2\2\u00ef\u00f0\f\20\2\2\u00f0\u00f2\t\t\2\2"+
		"\u00f1\u00c1\3\2\2\2\u00f1\u00c4\3\2\2\2\u00f1\u00c7\3\2\2\2\u00f1\u00d2"+
		"\3\2\2\2\u00f1\u00d5\3\2\2\2\u00f1\u00d8\3\2\2\2\u00f1\u00db\3\2\2\2\u00f1"+
		"\u00de\3\2\2\2\u00f1\u00e1\3\2\2\2\u00f1\u00e4\3\2\2\2\u00f1\u00e7\3\2"+
		"\2\2\u00f1\u00ea\3\2\2\2\u00f1\u00ef\3\2\2\2\u00f2\u00f5\3\2\2\2\u00f3"+
		"\u00f1\3\2\2\2\u00f3\u00f4\3\2\2\2\u00f4-\3\2\2\2\u00f5\u00f3\3\2\2\2"+
		"\24/\62<GSoy~\u0081\u008a\u0092\u009c\u00a2\u00a5\u00bf\u00cf\u00f1\u00f3";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}