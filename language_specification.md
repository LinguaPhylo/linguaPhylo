# LinguaPhylo Language Specification

## Introduction

LinguaPhylo (LPhy for short - pronounced el-fee) is a probabilistic model specification language to concisely and precisely define phylogenetic models. The aim is to provide a language (work towards a lingua franca) for probabilistic models of phylogenetic evolution that is independent of the method to perform inference. This language is readable by both humans and computers.

It does not have not traditional flow control, but does have explicit and implicit vectorisation to allow for easy contruction of rich and complex phylogenetic models.

## Lexical Structure

__Keywords__: There are two main keywords in your language which are data and model.

__Identifiers__: In LinguaPhylo, an identifier is a sequence of letters and digits, with the first character being a letter or an underscore. It is represented by the NAME token in the ANTLR grammar.

__Unicode Identifiers__: LinguaPhylo allows for unicode identifiers including Greek characters such as θ (theta) and ρ (rho). This feature helps in expressing models in a manner that aligns with standard mathematical notation. These characters are included in the Letter fragment in the ANTLR grammar, which defines a letter to include Greek and Coptic characters among others.

__Literals__: Literals can be of various types: floating point, decimal, hexadecimal, octal, binary, and string literals. They are represented by the FLOAT_LITERAL, DECIMAL_LITERAL, HEX_LITERAL, OCT_LITERAL, BINARY_LITERAL, and STRING_LITERAL tokens respectively.

__Operators__: LPhy includes several arithmetic, relational, logical, and bitwise operators, such as '+', '-', '*', '/', '**', '%', '>=', '<=', '>', '<', '==', '!=', '&&', '||', and ':'. Specification operators include the assignment operator ('='), the tilde operator ('~'), and the dot operator ('.').

__Braces and Brackets__: LPhy uses various types of braces and brackets, including round brackets '()' to enclose the arguments or functions and distributions, square brackets '[]' for array indexing and array literals, and curly braces '{}' for mapFunctions and blocks in data and model constructs.

__Whitespace and Comments__: Whitespace (spaces, tabs, and newlines) is used to separate tokens, and comments can be defined using single line '//' or multi-line '/* ... */' syntax.

__Escape Sequences__: In string literals, escape sequences can be used to include special characters, defined by the EscapeSequence fragment.

## Syntax

__Structured Input__: LinguaPhylo consists of optional data and model blocks. If these blocks are not provided, the syntax can include free lines containing variable specifications.

__Data block__: The data block, signified by the keyword DATA, can contain deterministic variable relations enclosed within '{' and '}' brackets. Alternatively, a data block can simply be a list of deterministic relations without the DATA keyword.

__Model block__: The model block, marked by the keyword MODEL, can hold relations, including stochastic and deterministic relations, enclosed within '{' and '}' brackets. Similar to the data block, a model block can also just be a list of relations without the MODEL keyword.

__Specifications__: A specification can either be stochastic or deterministic. A deterministic specification consists of a variable assignment (using the '=' operator) to an expression. A stochastic specification is a variable that follows a given distribution (signified by the '~' operator), and specifies a random variable.

__Expressions__: Expressions in LinguaPhylo are versatile and can include constants, variable names, parenthesized expressions, array expressions, method calls, object method calls, unary and binary operations, ternary operations, and map literals.

__Constants__: Constants can be any literal (floating point, decimal, hexadecimal, octal, binary, string, boolean) and may be preceded by a minus sign.

__Distributions__: Distributions consist of a name followed by a (possibly empty) list of named arguments (expressions) enclosed in parentheses. Distributions can't appear in expressions and can only appear as the right operand of a stochastic specification line.

__Functions__: Functions consist of a function name followed by an optional list of expressions enclosed in parentheses. The arguments can be unnamed (if less than or equal to 3 arguments) or named.

__Method calls__: Method calls are functions applied to an object (denoted by variable). They are separated by a dot (.) operator and their arguments are always unnamed.

__Maps__: Map literals take a list of named expressions as an argument and are enclosed in curly braces '{}'.

__Array Construction__: An array construction expression consists of one or more comma-delimited expressions enclosed in square brackets '[]'. Note that an empty array is not allowed.

__Identifiers__: LinguaPhylo allows standard Latin alphabet-based identifiers as well as Greek characters like θ (theta) and ρ (rho) for identifiers.

__Comments__: Comments can be single-line comments (preceded by '//') or multiline comments (enclosed within '/*' and '*/'). Comments are ignored by the interpreter.

__Whitespace__: Spaces, tabs, and newlines are used to separate tokens in the language.

## Data Types

__Primitive types__: Several primitive types are supported, as indicated by the rule for constant:

* Integer: There are various literal types for integers: Decimal (DECIMAL_LITERAL), Hexadecimal (HEX_LITERAL), and Octal (OCT_LITERAL).
* Float: There are two types of float literals: FLOAT_LITERAL and HEX_FLOAT_LITERAL. The latter is a hexadecimal floating-point literal.
* String: STRING_LITERAL represents a string type.
* Boolean: 'true' and 'false' represent boolean values.
* Arrays: LPhy supports arrays of all primitive types. Arrays may be indexed.

__Objects and functions__: 

LPhy supports the extension to other more complex data types and the standard library includes TimeTree, Alignment and other objects. Method calls on these objects are also allowed by the language.

__Named Variables and Expressions__: 

Named variables and expressions are also supported as defined by the NAME token and the named_expression rule.

## Implicit Vectorization

The LPhy language provides features that support the efficient generation and manipulation of independent and identically distributed (IID) random variables. This is achieved through a feature known as "Variable Vectorization", which encompasses two mechanisms for generating vectors or matrices of IID random variables.

### Replicates keyword

The replicates keyword is used to generate a vector of IID random variables.

```lphy
kappa ~ LogNormal(meanlog=0.5, sdlog=1.0, replicates=3);
````

In the above example, replicates=3 generates a vector kappa of three log-normally distributed random variables.

### Vectorized Generative Distribution

Variable vectorization can also be applied to generative distributions that produce vectors. In this case, the output is a matrix.

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

Expressions in LPhy form the basic building blocks of the language. They consist of constants, variables, operations, and function calls, and can be combined in various ways to create more complex expressions.

__Constants__ in LPhy can be floating-point, decimal, hexadecimal, octal, binary literals, and string literals. They can also be boolean values represented as true and false.

__Variables__ are identifiers that hold a specific value. The value of a variable can be any of the types defined in LPhy, including primitive types, arrays, and objects.

LPhy supports a variety of operators that can be used in expressions:

1. Arithmetic Operators:
  - These include addition (+), subtraction (-), multiplication (*), division (/), exponentiation (**), and modulo (%). These operators can be used with integer and floating-point number literals and variables.
2. Relational Operators:
  - These include greater than (>), less than (<), greater than or equal to (>=), less than or equal to (<=), equal to (==), and not equal to (!=). Relational operators can be used to compare two expressions and return a boolean result.
3. Logical Operators:
  - Logical operators include logical AND (&&), logical OR (||). They can be used to combine boolean expressions.
4. Specification Operators:
  - The two specification operators are the assignment operator (=), and the tilde operator (~). The assignment operator is used to assign a deterministic value to a variable, while the tilde operator is used to define a variable as following a given distribution. 
5. Dot Operator:
  - The dot operator (.) is used to call methods on an object or to access its properties. The object is specified to the left of the dot operator, and the method name or property is specified to the right.

### Operator Precedence and Associativity:

Operator precedence in LPhy follows the standard order of operations, also known as BODMAS or PEMDAS, which stands for Brackets, Orders (exponents), Division and Multiplication, Addition and Subtraction. Operators of the same precedence level are evaluated from left to right, known as left-associativity. The only exception is exponentiation (**), which is right-associative.

Here is the order of operations from highest to lowest precedence:

1. Parentheses ()
2. Exponentiation **
3. Multiplication *, Division /, Modulo %
4. Addition +, Subtraction -
5. Relational operators: >, <, >=, <=, ==, !=
6. Logical AND &&
7. Logical OR ||
8. Assignment =, Tilde ~, Dot .

### Complex Expressions:

Expressions can be nested, meaning that the result of one expression can be used as a part of another expression. This is particularly useful for defining complex mathematical operations or logic.

Example:

```lphy
x = 5;
y = 10;
z = (x * y) + 5;
```

In this example, z is defined as the result of the expression (x * y) + 5, where x and y are variables.

Expressions can also include function calls and method calls. Functions consist of a function name followed by a list of expressions enclosed in parentheses, while method calls are functions applied to an object, separated by a dot (.) operator.

Example:

```lphy
a = sqrt(x);
b = tree.rootAge();
```

## Built-In Functions and Generative Distributions:

Description of built-in functions or procedures, expected inputs, outputs, and side effects, with a particular focus on how they interact with the vectorization feature of the language.

## Standard Library: 

The standard library is described here: https://linguaphylo.github.io/docs/

## Examples

Provide examples of code in the language to illustrate the unique features and general syntax and semantics of the language. For LPhy, it would be particularly beneficial to provide examples of models built using implicit vectorization.

## Error Messages and Debugging

Information on common error messages and debugging guidance.
