// this grammar defines a line in an LPHY file without data and model blocks.
grammar Simulator;

// defines the top-level input of the grammar as a relation_list with an optional empty alternative (|).
input:  | relation_list ;

// defines relations as a list of one or more relation rules enclosed in curly braces.
relations : '{' relation_list '}' ;

// defines relation_list as a list of one or more relation rules separated by semicolons.
relation_list:	relation | relation_list relation ;

// defines relation as either a stoch_relation or a determ_relation, followed by a semicolon.
relation: stoch_relation ';' | determ_relation ';' ;

//for_loop: counter relations
//;
//counter: 'for' '(' NAME 'in' range_element ')'
//;

//  the equal sign =, which assigns the value of the expression to the variable.
assignment: '=' ;

// define a deterministic relation, which consists of a variable assignment expression.
// The var in this context represents a variable name,
// and expression represents any valid expression that can be assigned to that variable.
determ_relation: var assignment expression
//| FUNC '(' NAME ')' assignment expression
;

// defines a stochastic relation, where the variable on the left-hand side of the tilde
// is assigned a value based on the distribution on the right-hand side of the tilde.
stoch_relation:	var TILDE distribution 
//| var '~' distribution truncated 
//| var '~' distribution interval 
;

//truncated: 'T' '(' expression ','  expression ')' ;
//interval: 'I' '(' expression ','  expression ')' ;

// var represents a variable name, which can optionally include a range list in square brackets ([ ]).
var: NAME '[' range_list ']' | NAME ;

// range_list represents a list of expressions separated by commas that define a range, for example 1, 2, 3.
range_list  : expression (',' expression)*  ;

// range_element represents an individual expression within a range list.
range_element: | expression ;

// constant represents a constant value,
// which can be a number (FLOAT_LITERAL, DECIMAL_LITERAL, OCT_LITERAL, HEX_LITERAL, HEX_FLOAT_LITERAL)
// or a string (STRING_LITERAL), or the boolean values true and false.
constant : '-'? (FLOAT_LITERAL|DECIMAL_LITERAL|OCT_LITERAL|HEX_LITERAL|HEX_FLOAT_LITERAL|STRING_LITERAL|'true'|'false');

// expression_list represents a list of named expressions separated by commas.
expression_list
    : named_expression (',' named_expression)*
    ;

// unnamed_expression_list represents a list of unnamed expressions separated by commas.
unnamed_expression_list
    : expression (',' expression)*
    ;

// mapFunction represents a function that takes a list of named expressions as an argument and returns a map.
mapFunction: '{' expression_list '}'
;

// methodCall represents a method call with a name and optional argument list.
methodCall
    : NAME '(' expression_list? ')'
    | NAME '(' unnamed_expression_list? ')'
    ;

// objectMethodCall represents a method call on an object variable,
// with the object variable represented by var and the method name represented by NAME.
objectMethodCall: var DOT NAME '(' unnamed_expression_list? ')'
;

// distribution represents a distribution function call,
// with the name of the distribution represented by NAME and the arguments represented by expression_list.
distribution : NAME '(' expression_list ')' ;

// named_expression represents a named expression,
// with a name on the left side of an equals sign (=) and an expression on the right side.
named_expression: NAME '=' expression ;

// rule for array expression
array_expression
    : '[' unnamed_expression_list? ']'
    ;

expression
    : constant
    | NAME
    | '(' expression ')'
    | array_expression
    | expression '[' range_list ']'
    | methodCall
    | objectMethodCall
    | expression postfix=('++' | '--')
    | prefix=('+'|'-'|'++'|'--') expression
//    | prefix=('~'|'!') expression
//    | prefix=('!') expression
    | expression bop=('**'|'*'|'/'|'%') expression
    | expression bop=('+'|'-') expression
    | expression ('<' '<' | '>' '>' '>' | '>' '>') expression
    | expression bop=('<=' | '>=' | '>' | '<') expression
    | expression bop=('==' | '!=') expression
    | expression bop='&' expression
    | expression bop='^' expression
    | expression bop='|' expression
    | expression bop='&&' expression
    | expression bop='||' expression
    | expression bop=':' expression
    | mapFunction
//    | expression bop='?' expression ':' expression
//    | <assoc=right> expression
//      bop=('=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '>>=' | '>>>=' | '<<=' | '%=')
//	  expression
;

NAME:                Letter LetterOrDigit*;

LENGTH:              'length';
DIM:                 'dim';



/*
 * Adapted from https://github.com/antlr/grammars-v4/blob/master/java/JavaLexer.g4
 [The "BSD licence"]
 Copyright (c) 2013 Terence Parr, Sam Harwell
 Copyright (c) 2017 Ivan Kochurkin (upgrade to Java 8)
 All rights reserved.

*/

// Literals

DECIMAL_LITERAL:    ('0' | [1-9] (Digits? | '_'+ Digits)) [lL]?;
HEX_LITERAL:        '0' [xX] [0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])? [lL]?;
OCT_LITERAL:        '0' '_'* [0-7] ([0-7_]* [0-7])? [lL]?;
BINARY_LITERAL:     '0' [bB] [01] ([01_]* [01])? [lL]?;

FLOAT_LITERAL:      (Digits '.' Digits? | '.' Digits) ExponentPart? [fFdD]?
             |       Digits (ExponentPart [fFdD]? | [fFdD])
             ;

HEX_FLOAT_LITERAL:  '0' [xX] (HexDigits '.'? | HexDigits? '.' HexDigits) [pP] [+-]? Digits [fFdD]?;

STRING_LITERAL:     '"' .*? '"';

DOT:                '.';// TODO: implement
TILDE:              '~';


// Whitespace and comments

WS:                 [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);

// Fragment rules

// ExponentPart matches an exponent part of a number in scientific notation, such as e+10 or E-5.
// It starts with the characters e or E, followed by an optional plus or minus sign (+ or -),
// and then one or more digits.
fragment ExponentPart
    : [eE] [+-]? Digits
    ;

// Matches an escape sequence in a string literal, such as \n or \u00A9.
// It consists of a backslash (\) followed by one of the following:
// btnfr"'\\: A single character that represents the escape sequence for backspace (b), tab (t), newline (n),
// form feed (f), carriage return (r), double quote ("), single quote ('), or backslash (\).
// ([0-3]? [0-7])? [0-7]: An octal escape sequence, consisting of an optional 1- or 2-digit octal number ([0-3][0-7]) followed by a single octal digit ([0-7]).
//' u'+ HexDigit HexDigit HexDigit HexDigit: A Unicode escape sequence,
// consisting of the characters u followed by four hexadecimal digits (HexDigit).
fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

// Matches one or more hexadecimal digits (0-9, a-f, or A-F), optionally separated by underscores (_).
fragment HexDigits
    : HexDigit ((HexDigit | '_')* HexDigit)?
    ;

// Matches a single hexadecimal digit (0-9, a-f, or A-F).
fragment HexDigit
    : [0-9a-fA-F]
    ;

//  Matches one or more digits (0-9), optionally separated by underscores (_).
fragment Digits
    : [0-9] ([0-9_]* [0-9])?
    ;

// Matches a single letter or digit.
// It can be either a Java letter (a letter or underscore) or a digit (0-9).
fragment LetterOrDigit
    : Letter
    | [0-9]
    ;

// Matches a single Unicode character that is a Java letter.
// It can be a Latin letter (a-zA-Z), underscore (_),
// or any Unicode character above 0x7F that is not a surrogate,
// or a surrogate pair that encodes a character in the range U+10000 to U+10FFFF,
// or a character in the Greek and Coptic range (\u0370-\u03FF).
fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    | [\u0370-\u03FF] // Greek and Coptic characters
    ;

   
// JAGS tokens

//C:                   'c';

//VAR:                 'var';
//DATA:                'data';
//MODEL:               'model';

//FUNC:                Letter LetterOrDigit*;
//SPECIAL:             Letter LetterOrDigit*;
//BADCHAR:             Letter LetterOrDigit*;

//IN:                  'in';
//ARROW:               '<-';
//FOR:                 'for';
//GT GE LT LE EQ NE 
//AND OR

// Identifiers

//NAME:         Letter LetterOrDigit*;


//BOOL_LITERAL:       'true'
//            |       'false'
//            ;
//
//CHAR_LITERAL:       '\'' (~['\\\r\n] | EscapeSequence) '\'';
//
//STRING_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';

//NULL_LITERAL:       'null';

// Separators

//LPAREN:             '(';
//RPAREN:             ')';
//LBRACE:             '{';
//RBRACE:             '}';
//LBRACK:             '[';
//RBRACK:             ']';
//SEMI:               ';';
//COMMA:              ',';

// Operators

//ASSIGN:             '=';
//GT:                 '>';
//LT:                 '<';
//BANG:               '!';

//QUESTION:           '?';
//COLON:              ':';
//EQUAL:              '==';
//LE:                 '<=';
//GE:                 '>=';
//NOTEQUAL:           '!=';
//AND:                '&&';
//OR:                 '||';
//INC:                '++';
//DEC:                '--';Eric Paulos
//ADD:                '+';
//SUB:                '-';
//MUL:                '*';
//DIV:                '/';
//BITAND:             '&';
//BITOR:              '|';
//CARET:              '^';
//MOD:                '%';
//
//ADD_ASSIGN:         '+=';
//SUB_ASSIGN:         '-=';
//MUL_ASSIGN:         '*=';
//DIV_ASSIGN:         '/=';
//AND_ASSIGN:         '&=';
//OR_ASSIGN:          '|=';// TODO: implement
//XOR_ASSIGN:         '^=';
//MOD_ASSIGN:         '%=';
//LSHIFT_ASSIGN:      '<<=';
//RSHIFT_ASSIGN:      '>>=';
//URSHIFT_ASSIGN:     '>>>=';

// Java 8 tokens

//ARROW:              '->';
//COLONCOLON:         '::';

// Additional symbols not defined in the lexical specification

//AT:                 '@';
//ELLIPSIS:           '...';

