// Generated from com/redhat/cloud/policies/engine/condition/Expression.g4 by ANTLR 4.10.1
package com.redhat.cloud.policies.engine.condition;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ExpressionParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ExpressionVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(ExpressionParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#object}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject(ExpressionParser.ObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(ExpressionParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#logical_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogical_operator(ExpressionParser.Logical_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#boolean_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolean_operator(ExpressionParser.Boolean_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#numeric_compare_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumeric_compare_operator(ExpressionParser.Numeric_compare_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#string_compare_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString_compare_operator(ExpressionParser.String_compare_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#array_operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray_operator(ExpressionParser.Array_operatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#array}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArray(ExpressionParser.ArrayContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#numerical_value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumerical_value(ExpressionParser.Numerical_valueContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(ExpressionParser.ValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#negative_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegative_expr(ExpressionParser.Negative_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link ExpressionParser#key}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKey(ExpressionParser.KeyContext ctx);
}