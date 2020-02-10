// Generated from Simulator.g4 by ANTLR 4.4
package james.parser;
import org.antlr.v4.runtime.misc.NotNull;
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
	 * Visit a parse tree produced by {@link SimulatorParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(@NotNull SimulatorParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(@NotNull SimulatorParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(@NotNull SimulatorParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#var}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar(@NotNull SimulatorParser.VarContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#truncated}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTruncated(@NotNull SimulatorParser.TruncatedContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#counter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCounter(@NotNull SimulatorParser.CounterContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#range_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange_list(@NotNull SimulatorParser.Range_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#distribution}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistribution(@NotNull SimulatorParser.DistributionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation(@NotNull SimulatorParser.RelationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#determ_relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeterm_relation(@NotNull SimulatorParser.Determ_relationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#stoch_relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStoch_relation(@NotNull SimulatorParser.Stoch_relationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#input}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInput(@NotNull SimulatorParser.InputContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#named_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamed_expression(@NotNull SimulatorParser.Named_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#relation_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation_list(@NotNull SimulatorParser.Relation_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#range_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange_element(@NotNull SimulatorParser.Range_elementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list(@NotNull SimulatorParser.Expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#interval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInterval(@NotNull SimulatorParser.IntervalContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#for_loop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_loop(@NotNull SimulatorParser.For_loopContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#relations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelations(@NotNull SimulatorParser.RelationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link SimulatorParser#methodCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCall(@NotNull SimulatorParser.MethodCallContext ctx);
}