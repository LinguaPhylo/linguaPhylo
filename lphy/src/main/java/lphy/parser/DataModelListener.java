// Generated from lphy/parser/DataModel.g4 by ANTLR 4.12.0
package lphy.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DataModelParser}.
 */
public interface DataModelListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DataModelParser#input}.
	 * @param ctx the parse tree
	 */
	void enterInput(DataModelParser.InputContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#input}.
	 * @param ctx the parse tree
	 */
	void exitInput(DataModelParser.InputContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#datablock}.
	 * @param ctx the parse tree
	 */
	void enterDatablock(DataModelParser.DatablockContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#datablock}.
	 * @param ctx the parse tree
	 */
	void exitDatablock(DataModelParser.DatablockContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#modelblock}.
	 * @param ctx the parse tree
	 */
	void enterModelblock(DataModelParser.ModelblockContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#modelblock}.
	 * @param ctx the parse tree
	 */
	void exitModelblock(DataModelParser.ModelblockContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#relations}.
	 * @param ctx the parse tree
	 */
	void enterRelations(DataModelParser.RelationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#relations}.
	 * @param ctx the parse tree
	 */
	void exitRelations(DataModelParser.RelationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#determ_relations}.
	 * @param ctx the parse tree
	 */
	void enterDeterm_relations(DataModelParser.Determ_relationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#determ_relations}.
	 * @param ctx the parse tree
	 */
	void exitDeterm_relations(DataModelParser.Determ_relationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#relation_list}.
	 * @param ctx the parse tree
	 */
	void enterRelation_list(DataModelParser.Relation_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#relation_list}.
	 * @param ctx the parse tree
	 */
	void exitRelation_list(DataModelParser.Relation_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#determ_relation_list}.
	 * @param ctx the parse tree
	 */
	void enterDeterm_relation_list(DataModelParser.Determ_relation_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#determ_relation_list}.
	 * @param ctx the parse tree
	 */
	void exitDeterm_relation_list(DataModelParser.Determ_relation_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#determ_relation_line}.
	 * @param ctx the parse tree
	 */
	void enterDeterm_relation_line(DataModelParser.Determ_relation_lineContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#determ_relation_line}.
	 * @param ctx the parse tree
	 */
	void exitDeterm_relation_line(DataModelParser.Determ_relation_lineContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#relation}.
	 * @param ctx the parse tree
	 */
	void enterRelation(DataModelParser.RelationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#relation}.
	 * @param ctx the parse tree
	 */
	void exitRelation(DataModelParser.RelationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(DataModelParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(DataModelParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#determ_relation}.
	 * @param ctx the parse tree
	 */
	void enterDeterm_relation(DataModelParser.Determ_relationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#determ_relation}.
	 * @param ctx the parse tree
	 */
	void exitDeterm_relation(DataModelParser.Determ_relationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#stoch_relation}.
	 * @param ctx the parse tree
	 */
	void enterStoch_relation(DataModelParser.Stoch_relationContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#stoch_relation}.
	 * @param ctx the parse tree
	 */
	void exitStoch_relation(DataModelParser.Stoch_relationContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#var}.
	 * @param ctx the parse tree
	 */
	void enterVar(DataModelParser.VarContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#var}.
	 * @param ctx the parse tree
	 */
	void exitVar(DataModelParser.VarContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#range_list}.
	 * @param ctx the parse tree
	 */
	void enterRange_list(DataModelParser.Range_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#range_list}.
	 * @param ctx the parse tree
	 */
	void exitRange_list(DataModelParser.Range_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#range_element}.
	 * @param ctx the parse tree
	 */
	void enterRange_element(DataModelParser.Range_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#range_element}.
	 * @param ctx the parse tree
	 */
	void exitRange_element(DataModelParser.Range_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(DataModelParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(DataModelParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(DataModelParser.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(DataModelParser.Expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#unnamed_expression_list}.
	 * @param ctx the parse tree
	 */
	void enterUnnamed_expression_list(DataModelParser.Unnamed_expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#unnamed_expression_list}.
	 * @param ctx the parse tree
	 */
	void exitUnnamed_expression_list(DataModelParser.Unnamed_expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#mapFunction}.
	 * @param ctx the parse tree
	 */
	void enterMapFunction(DataModelParser.MapFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#mapFunction}.
	 * @param ctx the parse tree
	 */
	void exitMapFunction(DataModelParser.MapFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void enterMethodCall(DataModelParser.MethodCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void exitMethodCall(DataModelParser.MethodCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#objectMethodCall}.
	 * @param ctx the parse tree
	 */
	void enterObjectMethodCall(DataModelParser.ObjectMethodCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#objectMethodCall}.
	 * @param ctx the parse tree
	 */
	void exitObjectMethodCall(DataModelParser.ObjectMethodCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#distribution}.
	 * @param ctx the parse tree
	 */
	void enterDistribution(DataModelParser.DistributionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#distribution}.
	 * @param ctx the parse tree
	 */
	void exitDistribution(DataModelParser.DistributionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#named_expression}.
	 * @param ctx the parse tree
	 */
	void enterNamed_expression(DataModelParser.Named_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#named_expression}.
	 * @param ctx the parse tree
	 */
	void exitNamed_expression(DataModelParser.Named_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#array_expression}.
	 * @param ctx the parse tree
	 */
	void enterArray_expression(DataModelParser.Array_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#array_expression}.
	 * @param ctx the parse tree
	 */
	void exitArray_expression(DataModelParser.Array_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link DataModelParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(DataModelParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link DataModelParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(DataModelParser.ExpressionContext ctx);
}