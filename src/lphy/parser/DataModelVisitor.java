// Generated from /Users/adru001/Git/graphicalModelSimulation/src/lphy/parser/DataModel.g4 by ANTLR 4.8
package lphy.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link DataModelParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface DataModelVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link DataModelParser#input}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInput(DataModelParser.InputContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#datablock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatablock(DataModelParser.DatablockContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#modelblock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModelblock(DataModelParser.ModelblockContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#relations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelations(DataModelParser.RelationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#determ_relations}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeterm_relations(DataModelParser.Determ_relationsContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#relation_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation_list(DataModelParser.Relation_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#determ_relation_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeterm_relation_list(DataModelParser.Determ_relation_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#determ_relation_line}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeterm_relation_line(DataModelParser.Determ_relation_lineContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelation(DataModelParser.RelationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#for_loop}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFor_loop(DataModelParser.For_loopContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#counter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCounter(DataModelParser.CounterContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(DataModelParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#determ_relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeterm_relation(DataModelParser.Determ_relationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#stoch_relation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStoch_relation(DataModelParser.Stoch_relationContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#var}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVar(DataModelParser.VarContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#range_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange_list(DataModelParser.Range_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#range_element}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRange_element(DataModelParser.Range_elementContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(DataModelParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_list(DataModelParser.Expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#unnamed_expression_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnnamed_expression_list(DataModelParser.Unnamed_expression_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#mapFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMapFunction(DataModelParser.MapFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#methodCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodCall(DataModelParser.MethodCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#objectMethodCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectMethodCall(DataModelParser.ObjectMethodCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#distribution}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDistribution(DataModelParser.DistributionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#named_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamed_expression(DataModelParser.Named_expressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link DataModelParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(DataModelParser.ExpressionContext ctx);
}