// Generated from java/lphy/parser/LPhy.g4 by ANTLR 4.12.0
package lphy.core.parser.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link LPhyParser}.
 */
public interface LPhyListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link LPhyParser#input}.
	 * @param ctx the parse tree
	 */
	void enterInput(LPhyParser.InputContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#input}.
	 * @param ctx the parse tree
	 */
	void exitInput(LPhyParser.InputContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#structured_input}.
	 * @param ctx the parse tree
	 */
	void enterStructured_input(LPhyParser.Structured_inputContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#structured_input}.
	 * @param ctx the parse tree
	 */
	void exitStructured_input(LPhyParser.Structured_inputContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#free_lines}.
	 * @param ctx the parse tree
	 */
	void enterFree_lines(LPhyParser.Free_linesContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#free_lines}.
	 * @param ctx the parse tree
	 */
	void exitFree_lines(LPhyParser.Free_linesContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#datablock}.
	 * @param ctx the parse tree
	 */
	void enterDatablock(LPhyParser.DatablockContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#datablock}.
	 * @param ctx the parse tree
	 */
	void exitDatablock(LPhyParser.DatablockContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#determ_relation_list}.
	 * @param ctx the parse tree
	 */
	void enterDeterm_relation_list(LPhyParser.Determ_relation_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#determ_relation_list}.
	 * @param ctx the parse tree
	 */
	void exitDeterm_relation_list(LPhyParser.Determ_relation_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#determ_relation_line}.
	 * @param ctx the parse tree
	 */
	void enterDeterm_relation_line(LPhyParser.Determ_relation_lineContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#determ_relation_line}.
	 * @param ctx the parse tree
	 */
	void exitDeterm_relation_line(LPhyParser.Determ_relation_lineContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#modelblock}.
	 * @param ctx the parse tree
	 */
	void enterModelblock(LPhyParser.ModelblockContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#modelblock}.
	 * @param ctx the parse tree
	 */
	void exitModelblock(LPhyParser.ModelblockContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#relation_list}.
	 * @param ctx the parse tree
	 */
	void enterRelation_list(LPhyParser.Relation_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#relation_list}.
	 * @param ctx the parse tree
	 */
	void exitRelation_list(LPhyParser.Relation_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#relation}.
	 * @param ctx the parse tree
	 */
	void enterRelation(LPhyParser.RelationContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#relation}.
	 * @param ctx the parse tree
	 */
	void exitRelation(LPhyParser.RelationContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#var}.
	 * @param ctx the parse tree
	 */
	void enterVar(LPhyParser.VarContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#var}.
	 * @param ctx the parse tree
	 */
	void exitVar(LPhyParser.VarContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#range_list}.
	 * @param ctx the parse tree
	 */
	void enterRange_list(LPhyParser.Range_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#range_list}.
	 * @param ctx the parse tree
	 */
	void exitRange_list(LPhyParser.Range_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#determ_relation}.
	 * @param ctx the parse tree
	 */
	void enterDeterm_relation(LPhyParser.Determ_relationContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#determ_relation}.
	 * @param ctx the parse tree
	 */
	void exitDeterm_relation(LPhyParser.Determ_relationContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#stoch_relation}.
	 * @param ctx the parse tree
	 */
	void enterStoch_relation(LPhyParser.Stoch_relationContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#stoch_relation}.
	 * @param ctx the parse tree
	 */
	void exitStoch_relation(LPhyParser.Stoch_relationContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(LPhyParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(LPhyParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(LPhyParser.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(LPhyParser.Expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#unnamed_expression_list}.
	 * @param ctx the parse tree
	 */
	void enterUnnamed_expression_list(LPhyParser.Unnamed_expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#unnamed_expression_list}.
	 * @param ctx the parse tree
	 */
	void exitUnnamed_expression_list(LPhyParser.Unnamed_expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#mapFunction}.
	 * @param ctx the parse tree
	 */
	void enterMapFunction(LPhyParser.MapFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#mapFunction}.
	 * @param ctx the parse tree
	 */
	void exitMapFunction(LPhyParser.MapFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void enterMethodCall(LPhyParser.MethodCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void exitMethodCall(LPhyParser.MethodCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#objectMethodCall}.
	 * @param ctx the parse tree
	 */
	void enterObjectMethodCall(LPhyParser.ObjectMethodCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#objectMethodCall}.
	 * @param ctx the parse tree
	 */
	void exitObjectMethodCall(LPhyParser.ObjectMethodCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#distribution}.
	 * @param ctx the parse tree
	 */
	void enterDistribution(LPhyParser.DistributionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#distribution}.
	 * @param ctx the parse tree
	 */
	void exitDistribution(LPhyParser.DistributionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#named_expression}.
	 * @param ctx the parse tree
	 */
	void enterNamed_expression(LPhyParser.Named_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#named_expression}.
	 * @param ctx the parse tree
	 */
	void exitNamed_expression(LPhyParser.Named_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#array_expression}.
	 * @param ctx the parse tree
	 */
	void enterArray_expression(LPhyParser.Array_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#array_expression}.
	 * @param ctx the parse tree
	 */
	void exitArray_expression(LPhyParser.Array_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link LPhyParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(LPhyParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link LPhyParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(LPhyParser.ExpressionContext ctx);
}