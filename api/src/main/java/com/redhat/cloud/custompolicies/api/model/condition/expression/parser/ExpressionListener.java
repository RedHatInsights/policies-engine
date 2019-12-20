// Generated from com/redhat/cloud/custompolicies/api/model/condition/expression/parser/Expression.g4 by ANTLR 4.7.2
package com.redhat.cloud.custompolicies.api.model.condition.expression.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ExpressionParser}.
 */
public interface ExpressionListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(ExpressionParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(ExpressionParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#object}.
	 * @param ctx the parse tree
	 */
	void enterObject(ExpressionParser.ObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#object}.
	 * @param ctx the parse tree
	 */
	void exitObject(ExpressionParser.ObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(ExpressionParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(ExpressionParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#logical_operator}.
	 * @param ctx the parse tree
	 */
	void enterLogical_operator(ExpressionParser.Logical_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#logical_operator}.
	 * @param ctx the parse tree
	 */
	void exitLogical_operator(ExpressionParser.Logical_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#boolean_operator}.
	 * @param ctx the parse tree
	 */
	void enterBoolean_operator(ExpressionParser.Boolean_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#boolean_operator}.
	 * @param ctx the parse tree
	 */
	void exitBoolean_operator(ExpressionParser.Boolean_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#compare_operator}.
	 * @param ctx the parse tree
	 */
	void enterCompare_operator(ExpressionParser.Compare_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#compare_operator}.
	 * @param ctx the parse tree
	 */
	void exitCompare_operator(ExpressionParser.Compare_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#array_operator}.
	 * @param ctx the parse tree
	 */
	void enterArray_operator(ExpressionParser.Array_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#array_operator}.
	 * @param ctx the parse tree
	 */
	void exitArray_operator(ExpressionParser.Array_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#array}.
	 * @param ctx the parse tree
	 */
	void enterArray(ExpressionParser.ArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#array}.
	 * @param ctx the parse tree
	 */
	void exitArray(ExpressionParser.ArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#numerical_value}.
	 * @param ctx the parse tree
	 */
	void enterNumerical_value(ExpressionParser.Numerical_valueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#numerical_value}.
	 * @param ctx the parse tree
	 */
	void exitNumerical_value(ExpressionParser.Numerical_valueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(ExpressionParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(ExpressionParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ExpressionParser#key}.
	 * @param ctx the parse tree
	 */
	void enterKey(ExpressionParser.KeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link ExpressionParser#key}.
	 * @param ctx the parse tree
	 */
	void exitKey(ExpressionParser.KeyContext ctx);
}