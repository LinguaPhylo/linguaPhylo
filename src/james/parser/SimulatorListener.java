// Generated from Simulator.g4 by ANTLR 4.8
package james.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SimulatorParser}.
 */
public interface SimulatorListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#input}.
	 * @param ctx the parse tree
	 */
	void enterInput(SimulatorParser.InputContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#input}.
	 * @param ctx the parse tree
	 */
	void exitInput(SimulatorParser.InputContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#relations}.
	 * @param ctx the parse tree
	 */
	void enterRelations(SimulatorParser.RelationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#relations}.
	 * @param ctx the parse tree
	 */
	void exitRelations(SimulatorParser.RelationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#relation_list}.
	 * @param ctx the parse tree
	 */
	void enterRelation_list(SimulatorParser.Relation_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#relation_list}.
	 * @param ctx the parse tree
	 */
	void exitRelation_list(SimulatorParser.Relation_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#relation}.
	 * @param ctx the parse tree
	 */
	void enterRelation(SimulatorParser.RelationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#relation}.
	 * @param ctx the parse tree
	 */
	void exitRelation(SimulatorParser.RelationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#for_loop}.
	 * @param ctx the parse tree
	 */
	void enterFor_loop(SimulatorParser.For_loopContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#for_loop}.
	 * @param ctx the parse tree
	 */
	void exitFor_loop(SimulatorParser.For_loopContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#counter}.
	 * @param ctx the parse tree
	 */
	void enterCounter(SimulatorParser.CounterContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#counter}.
	 * @param ctx the parse tree
	 */
	void exitCounter(SimulatorParser.CounterContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(SimulatorParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(SimulatorParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#determ_relation}.
	 * @param ctx the parse tree
	 */
	void enterDeterm_relation(SimulatorParser.Determ_relationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#determ_relation}.
	 * @param ctx the parse tree
	 */
	void exitDeterm_relation(SimulatorParser.Determ_relationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#stoch_relation}.
	 * @param ctx the parse tree
	 */
	void enterStoch_relation(SimulatorParser.Stoch_relationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#stoch_relation}.
	 * @param ctx the parse tree
	 */
	void exitStoch_relation(SimulatorParser.Stoch_relationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#var}.
	 * @param ctx the parse tree
	 */
	void enterVar(SimulatorParser.VarContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#var}.
	 * @param ctx the parse tree
	 */
	void exitVar(SimulatorParser.VarContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#range_list}.
	 * @param ctx the parse tree
	 */
	void enterRange_list(SimulatorParser.Range_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#range_list}.
	 * @param ctx the parse tree
	 */
	void exitRange_list(SimulatorParser.Range_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#range_element}.
	 * @param ctx the parse tree
	 */
	void enterRange_element(SimulatorParser.Range_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#range_element}.
	 * @param ctx the parse tree
	 */
	void exitRange_element(SimulatorParser.Range_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(SimulatorParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(SimulatorParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(SimulatorParser.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(SimulatorParser.Expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#unnamed_expression_list}.
	 * @param ctx the parse tree
	 */
	void enterUnnamed_expression_list(SimulatorParser.Unnamed_expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#unnamed_expression_list}.
	 * @param ctx the parse tree
	 */
	void exitUnnamed_expression_list(SimulatorParser.Unnamed_expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void enterMethodCall(SimulatorParser.MethodCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void exitMethodCall(SimulatorParser.MethodCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#distribution}.
	 * @param ctx the parse tree
	 */
	void enterDistribution(SimulatorParser.DistributionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#distribution}.
	 * @param ctx the parse tree
	 */
	void exitDistribution(SimulatorParser.DistributionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#named_expression}.
	 * @param ctx the parse tree
	 */
	void enterNamed_expression(SimulatorParser.Named_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#named_expression}.
	 * @param ctx the parse tree
	 */
	void exitNamed_expression(SimulatorParser.Named_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(SimulatorParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(SimulatorParser.ExpressionContext ctx);
}