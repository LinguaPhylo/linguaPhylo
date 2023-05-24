// this is the grammar to defines LPHY language.
// It contains 4 scenarios: either, or both a data and model block,
// and free lines without data and model blocks.
grammar LPhy;

// This is the top-level rule that the parser will start with.
input: structured_input | free_lines;
// LinguaPhylo consists of optional data and model blocks.
// If these blocks are not provided, the syntax can include free lines containing relations.
structured_input: datablock? modelblock?;
// free lines without data and model blocks.
free_lines: | relation_list ;

// fundamental reserved keywords in LPhy language
DATA:               'data';
MODEL:              'model';
ASSIGN:             '=';
TILDE:              '~';
DOT:                '.';
//TODO replicates=INT


// *** data block *** //
// The data block, signified by the keyword DATA,
// can contain deterministic relations enclosed within '{' and '}' brackets.
// Alternatively, a data block can simply be a list of deterministic relations without the DATA keyword.
datablock: DATA '{' determ_relation_list? '}' | determ_relation_list;

// a rule called determ_relation_list that matches either a determ_relation_line or a determ_relation_list followed by a determ_relation_line.
determ_relation_list:	determ_relation_line | determ_relation_list determ_relation_line;

// a rule called determ_relation_line that matches a determ_relation followed by a semicolon.
determ_relation_line: determ_relation ';';

// *** model block *** //
// The model block, marked by the keyword MODEL,
// can hold relations, including stochastic and deterministic relations,
// enclosed within '{' and '}' brackets. Similar to the data block,
// a model block can also just be a list of relations without the MODEL keyword.
modelblock: MODEL '{' relation_list? '}' | relation_list;

// a rule called relation_list that matches either a relation or a relation_list followed by a relation.
relation_list:	relation | relation_list relation;

// A relation can either be a stochastic or a deterministic relation.
// A deterministic relation consists of a variable assignment (using the '=' operator) to an expression.
// A stochastic relation is a variable that follows a given distribution (signified by the '~' operator).
relation: stoch_relation ';' | determ_relation ';';

// *** assign relations *** //
// a rule called var that matches either a NAME or a NAME followed by a pair of square brackets containing a range_list.
var: NAME | NAME '[' range_list ']';

// a rule called range_list which specifies a comma-separated list of expressions.
// (',' expression)* is a zero or more repetition of the pattern ", expression".
// This means to define the range of index for arrays, inlcuding array of arrays scenario.
range_list : expression (',' expression)*  ;

// a rule called determ_relation that matches a var followed by an assignment followed by an expression.
determ_relation: var ASSIGN expression ;

// a rule called stoch_relation that matches a var followed by a tilde (~) followed by a distribution.
stoch_relation:	var TILDE distribution ;

// Constants can be any literal (floating point, decimal, hexadecimal, octal, binary, string, boolean)
// and may be preceded by a minus sign.
constant : '-'? (FLOAT_LITERAL|DECIMAL_LITERAL|OCT_LITERAL|HEX_LITERAL|HEX_FLOAT_LITERAL|STRING_LITERAL|'true'|'false');

// An expression_list consists of one or more named_expressions separated by commas.
expression_list : named_expression (',' named_expression)*  ;

// An unnamed_expression_list consists of one or more expressions separated by commas.
unnamed_expression_list : expression (',' expression)*  ;

// MapFunctions take a list of named expressions as an argument and are enclosed in curly braces '{}'.
mapFunction: '{' expression_list '}';

// Functions consist of a function name followed by an optional list of expressions enclosed in parentheses.
// The arguments can be unnamed (if less than or equal to 3 arguments) or named.
function
    : NAME '(' expression_list? ')'
    | NAME '(' unnamed_expression_list? ')'
    ;

// Method calls are functions applied to an object (denoted by variable).
// They are separated by a dot (.) operator and their arguments are always unnamed.
methodCall : var DOT NAME '(' unnamed_expression_list? ')';

// a distribution function which takes an expression_list as an argument.
distribution : NAME '(' expression_list ')' ;

// a named_expression which consists of a variable name (NAME) followed by an equal sign and an expression.
named_expression : NAME ASSIGN expression ;

// An array expression consists of one or more comma-delimited expressions enclosed in square brackets '[]'.
// Note that an empty array is not allowed.
array_expression : '[' unnamed_expression_list ']' ;

// Expressions in LinguaPhylo are versatile and can include constants,
// variable names, parenthesized expressions, array expressions, method calls,
// object method calls, unary and binary operations, ternary operations, and mapFunctions.
expression
    : constant
    | NAME
    | '(' expression ')'
    | array_expression
    | expression '[' range_list ']'
    | function
    | methodCall
    | prefix=('+'|'-'|'!') expression
    // ** is power, % is mod
    | expression bop=('**'|'*'|'/'|'%') expression
    | expression bop=('+'|'-') expression
    | expression bop=('<=' | '>=' | '>' | '<') expression
    | expression bop=('==' | '!=') expression
    | expression bop=('&&' | '||') expression
    // create an array
    | expression bop=':' expression
    // bitwise
    | expression bop=('&' | '|') expression
    // TODO Java code is commented out
    | expression ('<' '<' | '>' '>' '>' | '>' '>' | '^') expression
//    | expression bop='?' expression ':' expression // conflict with expression bop=':' expression
    | mapFunction
;

// Identifiers: LinguaPhylo allows standard Latin alphabet-based identifiers
// as well as Greek characters like θ (theta) and ρ (rho) for identifiers.
// TODO Greek characters are tranlated by Java class internally, not here.
NAME:               Letter LetterOrDigit*;

//TODO defined below but not used here ?
// Whitespace: Spaces, tabs, and newlines are used to separate tokens in the language.
WS:                 [ \t\r\n\u000C]+ -> channel(HIDDEN);
//TODO defined below but not used here ?
// Comments: Comments can be single-line comments (preceded by '//')
// or multiline comments (enclosed within '/' and '/').
// Comments are ignored by the interpreter.
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);



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

//CHAR_LITERAL:       '\'' (~['\\\r\n] | EscapeSequence) '\'';
//STRING_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';
STRING_LITERAL:     '"' .*? '"';


// Fragment rules

// defines a regex pattern for matching the exponent part of a floating-point literal.
// It matches the e or E character, optionally followed by a + or - sign,
// and one or more digits (as defined by the Digits fragment rule).
fragment ExponentPart
    : [eE] [+-]? Digits
    ;

// defines a regex pattern for matching escape sequences in string literals.
// It can match escape sequences for newline, tab, backspace, form feed, double quote,
// single quote, and backslash characters.
// It can also match octal escape sequences (up to three digits)
// and Unicode escape sequences (using the \u prefix followed by four hexadecimal digits).
fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

// defines a regex pattern for matching hexadecimal digits.
// It can match one or more hexadecimal digits,
// optionally separated by underscores (useful for making long hexadecimal literals more readable).
fragment HexDigits
    : HexDigit ((HexDigit | '_')* HexDigit)?
    ;

// defines a regex pattern for matching a single hexadecimal digit.
fragment HexDigit
    : [0-9a-fA-F]
    ;

// defines a regex pattern for matching decimal digits.
// It can match one or more digits, optionally separated by underscores
// (useful for making long decimal literals more readable).
fragment Digits
    : [0-9] ([0-9_]* [0-9])?
    ;

// defines a regex pattern for matching a letter or a digit.
// It can match any letter (as defined by the Letter fragment rule) or any decimal digit.
fragment LetterOrDigit
    : Letter
    | [0-9]
    ;

// defines a regex pattern for matching a Unicode letter.
// It can match any ASCII letter, the dollar sign,
// or any Unicode character above 0x7F that is not a surrogate or a control character.
// It can also match Greek and Coptic characters or
// UTF-16 surrogate pairs used for encoding characters above U+FFFF.
fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    | [\u0370-\u03FF] // Greek and Coptic characters
    ;
