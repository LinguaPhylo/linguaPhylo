// Generated from Simulator.g4 by ANTLR 4.4
package james.parser;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SimulatorParser}.
 */
public interface SimulatorListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(@NotNull SimulatorParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(@NotNull SimulatorParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(@NotNull SimulatorParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(@NotNull SimulatorParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#assignment}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(@NotNull SimulatorParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#assignment}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(@NotNull SimulatorParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#var}.
	 * @param ctx the parse tree
	 */
	void enterVar(@NotNull SimulatorParser.VarContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#var}.
	 * @param ctx the parse tree
	 */
	void exitVar(@NotNull SimulatorParser.VarContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#truncated}.
	 * @param ctx the parse tree
	 */
	void enterTruncated(@NotNull SimulatorParser.TruncatedContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#truncated}.
	 * @param ctx the parse tree
	 */
	void exitTruncated(@NotNull SimulatorParser.TruncatedContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#counter}.
	 * @param ctx the parse tree
	 */
	void enterCounter(@NotNull SimulatorParser.CounterContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#counter}.
	 * @param ctx the parse tree
	 */
	void exitCounter(@NotNull SimulatorParser.CounterContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#range_list}.
	 * @param ctx the parse tree
	 */
	void enterRange_list(@NotNull SimulatorParser.Range_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#range_list}.
	 * @param ctx the parse tree
	 */
	void exitRange_list(@NotNull SimulatorParser.Range_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#distribution}.
	 * @param ctx the parse tree
	 */
	void enterDistribution(@NotNull SimulatorParser.DistributionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#distribution}.
	 * @param ctx the parse tree
	 */
	void exitDistribution(@NotNull SimulatorParser.DistributionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#relation}.
	 * @param ctx the parse tree
	 */
	void enterRelation(@NotNull SimulatorParser.RelationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#relation}.
	 * @param ctx the parse tree
	 */
	void exitRelation(@NotNull SimulatorParser.RelationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#determ_relation}.
	 * @param ctx the parse tree
	 */
	void enterDeterm_relation(@NotNull SimulatorParser.Determ_relationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#determ_relation}.
	 * @param ctx the parse tree
	 */
	void exitDeterm_relation(@NotNull SimulatorParser.Determ_relationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#stoch_relation}.
	 * @param ctx the parse tree
	 */
	void enterStoch_relation(@NotNull SimulatorParser.Stoch_relationContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#stoch_relation}.
	 * @param ctx the parse tree
	 */
	void exitStoch_relation(@NotNull SimulatorParser.Stoch_relationContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#input}.
	 * @param ctx the parse tree
	 */
	void enterInput(@NotNull SimulatorParser.InputContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#input}.
	 * @param ctx the parse tree
	 */
	void exitInput(@NotNull SimulatorParser.InputContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#named_expression}.
	 * @param ctx the parse tree
	 */
	void enterNamed_expression(@NotNull SimulatorParser.Named_expressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#named_expression}.
	 * @param ctx the parse tree
	 */
	void exitNamed_expression(@NotNull SimulatorParser.Named_expressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#relation_list}.
	 * @param ctx the parse tree
	 */
	void enterRelation_list(@NotNull SimulatorParser.Relation_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#relation_list}.
	 * @param ctx the parse tree
	 */
	void exitRelation_list(@NotNull SimulatorParser.Relation_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#range_element}.
	 * @param ctx the parse tree
	 */
	void enterRange_element(@NotNull SimulatorParser.Range_elementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#range_element}.
	 * @param ctx the parse tree
	 */
	void exitRange_element(@NotNull SimulatorParser.Range_elementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void enterExpression_list(@NotNull SimulatorParser.Expression_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#expression_list}.
	 * @param ctx the parse tree
	 */
	void exitExpression_list(@NotNull SimulatorParser.Expression_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#interval}.
	 * @param ctx the parse tree
	 */
	void enterInterval(@NotNull SimulatorParser.IntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#interval}.
	 * @param ctx the parse tree
	 */
	void exitInterval(@NotNull SimulatorParser.IntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#for_loop}.
	 * @param ctx the parse tree
	 */
	void enterFor_loop(@NotNull SimulatorParser.For_loopContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#for_loop}.
	 * @param ctx the parse tree
	 */
	void exitFor_loop(@NotNull SimulatorParser.For_loopContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#relations}.
	 * @param ctx the parse tree
	 */
	void enterRelations(@NotNull SimulatorParser.RelationsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#relations}.
	 * @param ctx the parse tree
	 */
	void exitRelations(@NotNull SimulatorParser.RelationsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SimulatorParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void enterMethodCall(@NotNull SimulatorParser.MethodCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link SimulatorParser#methodCall}.
	 * @param ctx the parse tree
	 */
	void exitMethodCall(@NotNull SimulatorParser.MethodCallContext ctx);
}