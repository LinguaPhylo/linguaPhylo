// Generated from Simulator.g4 by ANTLR 4.8
package james.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SimulatorParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SimulatorVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#input}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInput(SimulatorParser.InputContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#relations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelations(SimulatorParser.RelationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#relation_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation_list(SimulatorParser.Relation_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation(SimulatorParser.RelationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#for_loop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_loop(SimulatorParser.For_loopContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#counter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCounter(SimulatorParser.CounterContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(SimulatorParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#determ_relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeterm_relation(SimulatorParser.Determ_relationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#stoch_relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStoch_relation(SimulatorParser.Stoch_relationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#truncated}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTruncated(SimulatorParser.TruncatedContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#interval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval(SimulatorParser.IntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#var}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar(SimulatorParser.VarContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#range_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange_list(SimulatorParser.Range_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#range_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange_element(SimulatorParser.Range_elementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(SimulatorParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list(SimulatorParser.Expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#unnamed_expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnnamed_expression_list(SimulatorParser.Unnamed_expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#methodCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCall(SimulatorParser.MethodCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#distribution}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistribution(SimulatorParser.DistributionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#named_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamed_expression(SimulatorParser.Named_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(SimulatorParser.ExpressionContext ctx);
}