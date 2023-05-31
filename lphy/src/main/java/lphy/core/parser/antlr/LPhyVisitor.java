// Generated from java/lphy/parser/LPhy.g4 by ANTLR 4.12.0
package lphy.core.parser.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link LPhyParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface LPhyVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link LPhyParser#input}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInput(LPhyParser.InputContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#structured_input}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructured_input(LPhyParser.Structured_inputContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#free_lines}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFree_lines(LPhyParser.Free_linesContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#datablock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatablock(LPhyParser.DatablockContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#determ_relation_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeterm_relation_list(LPhyParser.Determ_relation_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#determ_relation_line}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeterm_relation_line(LPhyParser.Determ_relation_lineContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#modelblock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModelblock(LPhyParser.ModelblockContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#relation_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation_list(LPhyParser.Relation_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation(LPhyParser.RelationContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#var}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar(LPhyParser.VarContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#range_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange_list(LPhyParser.Range_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#determ_relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeterm_relation(LPhyParser.Determ_relationContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#stoch_relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStoch_relation(LPhyParser.Stoch_relationContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(LPhyParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list(LPhyParser.Expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#unnamed_expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnnamed_expression_list(LPhyParser.Unnamed_expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#mapFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapFunction(LPhyParser.MapFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#methodCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCall(LPhyParser.MethodCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#objectMethodCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectMethodCall(LPhyParser.ObjectMethodCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#distribution}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistribution(LPhyParser.DistributionContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#named_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamed_expression(LPhyParser.Named_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#array_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_expression(LPhyParser.Array_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link LPhyParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(LPhyParser.ExpressionContext ctx);
}