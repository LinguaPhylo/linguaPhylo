# LinguaPhylo Language Specification

## Introduction

Description and motivation behind LinguaPhylo, including the key feature of implicit vectorization.

## Lexical Structure

*Keywords*: There are two main keywords in your language which are data and model.

*Identifiers*: In LinguaPhylo, an identifier is a sequence of letters and digits, with the first character being a letter or an underscore. It is represented by the NAME token in the ANTLR grammar.

*Unicode Identifiers*: LinguaPhylo allows for unicode identifiers including Greek characters such as θ (theta) and ρ (rho). This feature helps in expressing models in a manner that aligns with standard mathematical notation. These characters are included in the Letter fragment in the ANTLR grammar, which defines a letter to include Greek and Coptic characters among others.

*Literals*: Literals can be of various types: floating point, decimal, hexadecimal, octal, binary, and string literals. They are represented by the FLOAT_LITERAL, DECIMAL_LITERAL, HEX_LITERAL, OCT_LITERAL, BINARY_LITERAL, and STRING_LITERAL tokens respectively.

*Operators*: LPhy includes several arithmetic, relational, logical, and bitwise operators, such as '+', '-', '*', '/', '**', '%', '>=', '<=', '>', '<', '==', '!=', '&&', '||', and ':'. Specification operators include the assignment operator ('='), the tilde operator ('~'), and the dot operator ('.').

*Braces and Brackets*: LPhy uses various types of braces and brackets, including round brackets '()' to enclose the arguments or functions and distributions, square brackets '[]' for array indexing and array literals, and curly braces '{}' for mapFunctions and blocks in data and model constructs.

*Whitespace and Comments*: Whitespace (spaces, tabs, and newlines) is used to separate tokens, and comments can be defined using the '//' and '/.../' syntax.

*Special Functions*: LPhy has two special functions, 'length' and 'dim', that operate on data structures.

*Escape Sequences*: In string literals, escape sequences can be used to include special characters, defined by the EscapeSequence fragment.

## Syntax

*Structured Input*: LinguaPhylo consists of optional data and model blocks. If these blocks are not provided, the syntax can include free lines containing relations.

*Data Block*: The data block, signified by the keyword DATA, can contain deterministic relations enclosed within '{' and '}' brackets. Alternatively, a data block can simply be a list of deterministic relations without the DATA keyword.

*Model Block*: The model block, marked by the keyword MODEL, can hold relations, including stochastic and deterministic relations, enclosed within '{' and '}' brackets. Similar to the data block, a model block can also just be a list of relations without the MODEL keyword.

*Relations*: A relation can either be a stochastic or a deterministic relation. A deterministic relation consists of a variable assignment (using the '=' operator) to an expression. A stochastic relation is a variable that follows a given distribution (signified by the '~' operator).

*Expressions*: Expressions in LinguaPhylo are versatile and can include constants, variable names, parenthesized expressions, array expressions, method calls, object method calls, unary and binary operations, ternary operations, and mapFunctions.

*Constants*: Constants can be any literal (floating point, decimal, hexadecimal, octal, binary, string, boolean) and may be preceded by a minus sign.

*Functions*: Functions consist of a function name followed by an optional list of expressions enclosed in parentheses. The arguments can be unnamed (if less than 3 arguments) or named.

*Method calls*: Method calls are functions applied to an object (denoted by variable). They are separated by a dot (.) operator and their arguments are always unnamed.

*MapFunctions*: MapFunctions take a list of named expressions as an argument and are enclosed in curly braces '{}'.

*Array Expressions*: An array expression consists of one or more comma-delimited expressions enclosed in square brackets '[]'. Note that an empty array is not allowed.

*Identifiers*: LinguaPhylo allows standard Latin alphabet-based identifiers as well as Greek characters like θ (theta) and ρ (rho) for identifiers.

*Comments*: Comments can be single-line comments (preceded by '//') or multiline comments (enclosed within '/' and '/'). Comments are ignored by the interpreter.

*Whitespace*: Spaces, tabs, and newlines are used to separate tokens in the language.

## Data Types

*Primitive types*: Several primitive types are supported, as indicated by the rule for constant:

* Integer: There are various literal types for integers: Decimal (DECIMAL_LITERAL), Hexadecimal (HEX_LITERAL), and Octal (OCT_LITERAL).
* Float: There are two types of float literals: FLOAT_LITERAL and HEX_FLOAT_LITERAL. The latter is a hexadecimal floating-point literal.
* String: STRING_LITERAL represents a string type.
* Boolean: 'true' and 'false' represent boolean values.
* Arrays: LPhy supports arrays of all primitive types. Arrays may be indexed.

*Objects and functions*: 

LPhy supports the extension to other more complex data types and the standard library includes TimeTree, Alignment and other objects. Method calls on these objects are also allowed by the language.

*Named Variables and Expressions*: 

Named variables and expressions are also supported as defined by the NAME token and the named_expression rule.

## Implicit Vectorization

The LPhy language provides features that support the efficient generation and manipulation of independent and identically distributed (IID) random variables. This is achieved through a feature known as "Variable Vectorization", which encompasses two mechanisms for generating vectors or matrices of IID random variables.

Replicate Keyword: The replicates keyword is used to generate a vector of IID random variables.

```lphy
kappa ~ LogNormal(meanlog=0.5, sdlog=1.0, replicates=3);
````

In the above example, replicates=3 generates a vector kappa of three log-normally distributed random variables.

Vectorized Generative Distribution: Variable vectorization can also be applied to generative distributions that produce vectors. In this case, the output is a matrix.

```lphy
pi ~ Dirichlet(conc=[2.0, 2.0, 2.0, 2.0], replicates=3);
```

In the second example, replicates=3 in combination with the Dirichlet distribution creates a 3x4 matrix pi where each row is a vector of length 4 representing nucleotide base frequencies.

Vector Arguments in Generators: A second mechanism for vectorization involves passing a vector of elements as an argument to a generator.

```lphy
kappa ~ LogNormal(meanlog=0.5, sdlog=1.0, replicates=3);
pi ~ Dirichlet(conc=[2.0, 2.0, 2.0, 2.0], replicates=3);
Q = hky(kappa=kappa, freq=pi);
```

In this example, the vectors kappa and pi (both with a major dimension of 3) are passed as arguments to the hky function, yielding a vector of three instantaneous rate matrices stored in Q.

This form of vectorization, called "Implicit Vectorization," allows the generation and manipulation of complex structures like matrices, thereby broadening the scope of computations that can be performed using LPhy. It is particularly beneficial in the field of statistics where such scenarios are commonplace.

## Operators and Expressions

Description of the behavior of the built-in operators, especially when used with different data types. Discuss operator precedence and associativity.

## Built-In Functions and Generative Distributions:

Description of built-in functions or procedures, expected inputs, outputs, and side effects, with a particular focus on how they interact with the vectorization feature of the language.

## Standard Library: 

If there's a standard library that comes with the language, describe its contents and how to use them.

## Examples

Provide examples of code in the language to illustrate the unique features and general syntax and semantics of the language. For LPhy, it would be particularly beneficial to provide examples of models built using implicit vectorization.

## Error Messages and Debugging

Information on common error messages and debugging guidance.