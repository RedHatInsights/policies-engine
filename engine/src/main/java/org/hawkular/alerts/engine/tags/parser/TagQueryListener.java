// Generated from org/hawkular/alerts/engine/tags/parser/TagQuery.g4 by ANTLR 4.7.2
package org.hawkular.alerts.engine.tags.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TagQueryParser}.
 */
public interface TagQueryListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TagQueryParser#tagquery}.
	 * @param ctx the parse tree
	 */
	void enterTagquery(TagQueryParser.TagqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link TagQueryParser#tagquery}.
	 * @param ctx the parse tree
	 */
	void exitTagquery(TagQueryParser.TagqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link TagQueryParser#object}.
	 * @param ctx the parse tree
	 */
	void enterObject(TagQueryParser.ObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link TagQueryParser#object}.
	 * @param ctx the parse tree
	 */
	void exitObject(TagQueryParser.ObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link TagQueryParser#tagexp}.
	 * @param ctx the parse tree
	 */
	void enterTagexp(TagQueryParser.TagexpContext ctx);
	/**
	 * Exit a parse tree produced by {@link TagQueryParser#tagexp}.
	 * @param ctx the parse tree
	 */
	void exitTagexp(TagQueryParser.TagexpContext ctx);
	/**
	 * Enter a parse tree produced by {@link TagQueryParser#logical_operator}.
	 * @param ctx the parse tree
	 */
	void enterLogical_operator(TagQueryParser.Logical_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link TagQueryParser#logical_operator}.
	 * @param ctx the parse tree
	 */
	void exitLogical_operator(TagQueryParser.Logical_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link TagQueryParser#boolean_operator}.
	 * @param ctx the parse tree
	 */
	void enterBoolean_operator(TagQueryParser.Boolean_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link TagQueryParser#boolean_operator}.
	 * @param ctx the parse tree
	 */
	void exitBoolean_operator(TagQueryParser.Boolean_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link TagQueryParser#array_operator}.
	 * @param ctx the parse tree
	 */
	void enterArray_operator(TagQueryParser.Array_operatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link TagQueryParser#array_operator}.
	 * @param ctx the parse tree
	 */
	void exitArray_operator(TagQueryParser.Array_operatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link TagQueryParser#array}.
	 * @param ctx the parse tree
	 */
	void enterArray(TagQueryParser.ArrayContext ctx);
	/**
	 * Exit a parse tree produced by {@link TagQueryParser#array}.
	 * @param ctx the parse tree
	 */
	void exitArray(TagQueryParser.ArrayContext ctx);
	/**
	 * Enter a parse tree produced by {@link TagQueryParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(TagQueryParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link TagQueryParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(TagQueryParser.ValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link TagQueryParser#key}.
	 * @param ctx the parse tree
	 */
	void enterKey(TagQueryParser.KeyContext ctx);
	/**
	 * Exit a parse tree produced by {@link TagQueryParser#key}.
	 * @param ctx the parse tree
	 */
	void exitKey(TagQueryParser.KeyContext ctx);
}