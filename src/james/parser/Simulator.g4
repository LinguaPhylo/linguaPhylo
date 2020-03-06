grammar Simulator;


// grammar adapted from parser.yy in JAGS-4.3.0 source code


input:   /* empty */
| relation_list
;

relations:'{' relation_list '}' 
;

relation_list:	relation 
| relation_list relation 
;

relation: stoch_relation ';'
| determ_relation ';'
| for_loop ';'
;

for_loop: counter relations 
;

counter: 'for' '(' NAME 'in' range_element ')'
;

assignment: ARROW 
| '='
;

determ_relation: var assignment expression 
| FUNC '(' NAME ')' assignment expression 
;

stoch_relation:	var TILDE distribution 
//| var '~' distribution truncated 
//| var '~' distribution interval 
;

//truncated: 'T' '(' expression ','  expression ')' ;
//interval: 'I' '(' expression ','  expression ')' ;


var: NAME 
| NAME '[' range_list ']';

range_list: range_element
| range_list ',' range_element
;

range_element: 
| expression 
;

constant : '-'? (FLOAT_LITERAL|DECIMAL_LITERAL|OCT_LITERAL|HEX_LITERAL|HEX_FLOAT_LITERAL|STRING_LITERAL|'true'|'false');

expression_list
    : named_expression (',' named_expression)*
    ;

unnamed_expression_list
    : expression (',' expression)*
    ;

methodCall
    : NAME '(' expression_list? ')'
    ;

distribution
	: NAME '(' expression_list ')'
	;

named_expression: NAME '=' expression
	;
	
expression
    : constant
    | NAME
    | '(' expression ')'
    | '[' unnamed_expression_list ']'
    | expression '[' range_list ']'
    | methodCall
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
//    | expression bop='?' expression ':' expression
//    | <assoc=right> expression
//      bop=('=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '>>=' | '>>>=' | '<<=' | '%=')
//	  expression
;

   
// JAGS tokens

//C:                   'c';graphicalModelSimulation/

//VAR:                 'var';
//DATA:                'data';
//MODEL:               'model';

NAME:                Letter LetterOrDigit*;
//FUNC:                Letter LetterOrDigit*;
//SPECIAL:             Letter LetterOrDigit*;
//BADCHAR:             Letter LetterOrDigit*;

//IN:                  'in';
ARROW:               '<-';
//FOR:                 'for';
//GT GE LT LE EQ NE 
//AND OR
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

//BOOL_LITERAL:       'true'
//            |       'false'
//            ;
//
//CHAR_LITERAL:       '\'' (~['\\\r\n] | EscapeSequence) '\'';
//
//STRING_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';
STRING_LITERAL:     '"' .*? '"';
//
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
//DOT:                '.';// TODO: implement

// Operators

//ASSIGN:             '=';
//GT:                 '>';
//LT:                 '<';
//BANG:               '!';
TILDE:              '~';
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

// Whitespace and comments

WS:                 [ \t\r\n\u000C]+ -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);

// Identifiers

//NAME:         Letter LetterOrDigit*;

// Fragment rules

fragment ExponentPart
    : [eE] [+-]? Digits
    ;

fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

fragment HexDigits
    : HexDigit ((HexDigit | '_')* HexDigit)?
    ;

fragment HexDigit
    : [0-9a-fA-F]
    ;

fragment Digits
    : [0-9] ([0-9_]* [0-9])?
    ;

fragment LetterOrDigit
    : Letter
    | [0-9]
    ;

fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    | [\u0370-\u03FF] // Greek and Coptic characters
    ;
   
