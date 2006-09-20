header {/* 

 Copyright (c) 2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.ptalon;
}

///////////////////////////////////////////////////////////////////
////                         Ptalon Parser                     ////
class PtalonRecognizer extends Parser;
options {
	exportVocab = Ptalon;
	k = 2;
	buildAST = true;
	defaultErrorHandler = false;
	ASTLabelType = "PtalonAST";
}

/**
 * Parse for statement:
 * <p>import <i>qualified_identifier</i>;
 * <p>Generate tree #(IMPORT <i>qualified_identifier</i>).
 */
import_declaration:
	(IMPORT^ qualified_identifier SEMI!)
;

/**
 * Parse for statement:
 * <p><i>portType</i> <i>ID</i>
 * <p>where portType is either "port", "inport", or "outport".
 * Generate corresponding tree #(PORT ID), #(INPORT ID), or #(OUTPORT ID).
 */
port_declaration:
	(PORT^ | INPORT^ | OUTPORT^) ID
;

/**
 * Parse for statement:
 * <p><i>parameterType</i> <i>ID</i>
 * <p>where parameterType is either "parameter", "intparameter", or 
 * "outparameter".
 * Generate corresponding tree #(PARAMETER ID), #(INTPARAMETER ID), or 
 * #(BOOLPARAMETER ID).
 */
parameter_declaration:
	(PARAMETER^ | INTPARAMETER^ | BOOLPARAMETER^) ID
;

/**
 * Parse for statement:
 * <p>relation <i>ID</i>
 * <p>Generate tree #(RELATION ID)
 */
relation_declaration:
	RELATION^ ID
;

/**
 * Parse for statement
 * <p><i>ID</i>
 * <p>or
 * <p><i>ID</i>.qualified_identifier
 * <p>Generate tree #(QUALID)
 */
qualified_identifier!
{
	String identifier = "";
}
:
	a:ID {
		identifier = identifier + a.getText();
	}
	(DOT b:ID
	{
		identifier = identifier + "." +  b.getText();
	}
	)*
	{
		#qualified_identifier = #[QUALID, identifier];
	}
;

/**
 * Parse statements of one of form:
 * <p><i>ID</i> := <i>ID</i>
 * <p><i>ID</i> := <i>actor_declaration</i>
 * <p><i>ID</i> := <i>arithmetic_expression</i>
 * <p><i>ID</i> := <i>boolean_expression</i>
 * <p>with preference given in that order.  Generate corresponding
 * tree:
 * <p>#(ASSIGN ID ID)
 * <p>#(ASSIGN ID <i>actor_declaration</i>)
 * <p>#(ASSIGN ID <i>arithmetic_expression</i>)
 * <p>#(ASSIGN ID <i>boolean_expression</i>)
 */
assignment:
	ID ASSIGN^ ((ID (RPAREN | COMMA)) => ID |
		((ID LPAREN) => actor_declaration | 
			((arithmetic_expression) =>
				arithmetic_expression |
				boolean_expression
			)
		)
	)
;

/**
 * Parse statements of one of form:
 * <p><i>ID</i>(<i>assignment</i>, <i>assignment</i>, ...)
 * <p>Generate tree:
 * <p>#(ACTOR_DELCARATION <i>assignment</i> <i>assignment</i> ...)
 * <p>where the text for token ACTOR_DECLARATION is the leftmost
 * <i>ID</i> in the statement, or the name of the declared actor.
 */
actor_declaration!
:
	a:ID
	{
		#a = #[ACTOR_DECLARATION, a.getText()];
		#actor_declaration = #(a);
	} 
	LPAREN! (
		b:assignment 
		{
			#actor_declaration.addChild(#b);
		}
		(COMMA! c:assignment
		{
			#actor_declaration.addChild(#c);
		}
		)*
	)? RPAREN!
;

arithmetic_factor!
{
	int sign = 1;
}
:
	(MINUS
	{
		sign = -sign;
	}
	)*
	{
		if (sign == 1) {
			#arithmetic_factor = #([ARITHMETIC_FACTOR, "arithmetic_factor"], 
				[POSITIVE_SIGN, "positive"]);
		} else {
			#arithmetic_factor = #([ARITHMETIC_FACTOR, "arithmetic_factor"], 
				[NEGATIVE_SIGN, "negative"]);
		}
	}
	(a:ID 
	{
		#arithmetic_factor.addChild(#a);
	}
	| b:NUMBER_LITERAL 
	{
		#arithmetic_factor.addChild(#b);
	}
	| LPAREN! c:arithmetic_expression RPAREN!
	{
		#arithmetic_factor.addChild(#c);
	}
	)
;

arithmetic_term:
	arithmetic_factor ((STAR^ | DIVIDE^ | MOD^) arithmetic_factor)*
;

arithmetic_expression:
	arithmetic_term ((PLUS^ | MINUS^) arithmetic_term)*
;

relational_expression:
	arithmetic_expression (
		EQUAL^ | NOT_EQUAL^ | LESS_THAN^ | GREATER_THAN^ |
		LESS_EQUAL^ | GREATER_EQUAL^
	) arithmetic_expression
;

boolean_factor!
{
	boolean sign = true;
}
:
	(LOGICAL_NOT
	{
		sign = !sign;
	}
	)* 
	{
		if (sign) {
			#boolean_factor = #([BOOLEAN_FACTOR, "boolean_factor"], 
			[LOGICAL_BUFFER, "!!"]);
		} else {
			#boolean_factor = #([BOOLEAN_FACTOR, "boolean_factor"],
			[LOGICAL_NOT, "!"]);
		}
	}
	( ( (LPAREN boolean_expression) => LPAREN! a:boolean_expression RPAREN! 
	{
		#boolean_factor.addChild(#a);	
	}
	| b:relational_expression 
    {
		#boolean_factor.addChild(#b);	
    }
	) | c:TRUE 
	{
		#boolean_factor.addChild(#c);	
	}
	| d:FALSE 
	{
		#boolean_factor.addChild(#d);	
	}
	| e:ID
	{
		#boolean_factor.addChild(#e);	
	}
	)
;

boolean_term:
	boolean_factor (LOGICAL_AND^ boolean_factor)*
;

boolean_expression:
	boolean_term (LOGICAL_OR^ boolean_term)*
;

atomic_statement : 
	(port_declaration | parameter_declaration |
	relation_declaration | actor_declaration) SEMI!
;

conditional_statement!
{
	AST trueTree = null;
	AST falseTree = null;
}
:
	i:IF LPAREN! b:boolean_expression RPAREN! 
	{
		#conditional_statement = #(i, b);
		trueTree = #[TRUEBRANCH, "true branch"];
		falseTree = #[FALSEBRANCH, "false branch"];
	}
	LCURLY! (a1:atomic_statement 
	{
		trueTree.addChild(#a1);
	}
	| c1:conditional_statement
	{
		trueTree.addChild(#c1);
	}
	)* RCURLY! ELSE! LCURLY! (a2:atomic_statement 
	{
		falseTree.addChild(#a2);
	}
	| c2:conditional_statement
	{
		falseTree.addChild(#c2);
	}
	)* RCURLY!
	{
		#conditional_statement.addChild(trueTree);
		#conditional_statement.addChild(falseTree);
	}
;	

actor_definition!
:
	{
		#actor_definition = #[ACTOR_DEFINITION];
	}
	(i:import_declaration
	{
		#actor_definition.addChild(#i);
	}
	)* a:ID
	{
		#actor_definition.setText(a.getText());
	}
	IS! LCURLY! (b:atomic_statement 
	{
		#actor_definition.addChild(#b);
	}
	| c:conditional_statement
	{
		#actor_definition.addChild(#c);
	}
	)* RCURLY!

;

///////////////////////////////////////////////////////////////////
////                          Ptalon Lexer                     ////

class PtalonLexer extends Lexer;
options {
	exportVocab = Ptalon;
	testLiterals = false;
	k = 3;
}

tokens {
	IMPORT = "import";
	PORT = "port";
	INPORT = "inport";
	OUTPORT = "outport";
	PARAMETER = "parameter";
	INTPARAMETER = "intparameter";
	BOOLPARAMETER = "boolparameter";
	RELATION = "relation";
	TRUE = "true";
	TRUEBRANCH;
	FALSE = "false";
	FALSEBRANCH;
	IF = "if";
	ELSE = "else";
	IS = "is";
	QUALID;
	ATTRIBUTE;
	ACTOR_DECLARATION;
	ACTOR_DEFINITION;
	NEGATIVE_SIGN;
	POSITIVE_SIGN;
	ARITHMETIC_FACTOR;
	BOOLEAN_FACTOR;
	LOGICAL_BUFFER;
	ARITHMETIC_EXPRESSION;
	BOOLEAN_EXPRESSION;
}


// Punctuation symbols
ASSIGN: ":=";

COMMA: ',';

DOT: '.';

LBRACKET: '[';

LCURLY: '{';

LPAREN: '(';

RBRACKET: ']';

RCURLY: '}';

RPAREN: ')';

SEMI: ';';

// Operators

LOGICAL_OR: "||";

LOGICAL_AND: "&&";

EQUAL: "==";

NOT_EQUAL: "!=";

LESS_THAN: '<';

GREATER_THAN: '>';

LESS_EQUAL: "<=";

GREATER_EQUAL: ">=";

PLUS: '+';

MINUS: '-';

STAR: '*';

DIVIDE: '/';

MOD: '%';

BINARY_NOT: '~';

LOGICAL_NOT: '!';

// Escape sequence
ESC:
	'\\' ('n' | 'r' | 't' | 'b' | 'f' | '"' | '\'')
;

// An identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
ID options { testLiterals=true; } :
	('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
;

// Number literals
NUMBER_LITERAL:
	('0'..'9')+ ('.' ('0'..'9')+)?
;

ATTRIBUTE_MARKER :
	'$'
;

// String literals
STRING_LITERAL :
	'"' (ESC | ~('"'|'\\'|'\n'|'\r'))* '"'
;
	
// Whitespace -- ignored
WHITE_SPACE :
	(
		' '
		| '\t'
		| '\f'
		| '\r' '\n' { newline(); }
		| '\r' { newline(); }
		| '\n' { newline(); }
	)
	{ $setType(Token.SKIP); }
;
