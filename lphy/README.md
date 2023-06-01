# LPhy Core

The "lphy" [subproject](https://docs.gradle.org/current/userguide/multi_project_builds.html)
is the cornerstone of the entire LinguaPhylo project, encompassing critical components and
features that drive the project's functionality and capabilities.
It integrates essential elements such as the ANTLR Parser, vectorization techniques,
built-in functions and native functions, Java Exceptions for error handling,
and the Service Provider Interface (SPI):

## Java packages

1. ANTLR Parser: The "lphy" subproject includes an ANTLR Parser that defines the grammar of the LPhy language and
   converts scripts into probabilistic models implemented as Java objects.

2. Built-in functions and native functions: The built-in functions are directly implemented during parsing,
   such as numeric and logical operations, some common functions (e.g., min, log, exp, etc.), and trigonometric
   functions. The native functions refer to deterministic functions that third-party developers cannot implement using
   the exported core API. They are considered as the part of the "parser" package.

3. Models: The LPhy models implemented as Java objects consists of variables, arrays of variables, and generators.
   Generators include generative distributions, deterministic functions, and method calls.
   In addition, the basic data types (e.g., IntegerVale, ArrayValue, MapValue) are considered as the part of the "model"
   package.

4. Vectorization: LPhy supports implicit vectorization, similar to R, by matching inputs to functions and generative
   distributions. It is called as "vector match" in the Java implementation.
   Alternatively, users can generate vectors of independent and identically distributed (IID) random variables using
   the "replicates" argument for generative distributions.
   But this is currently not available to functions.

5. Java Exceptions: These Java Exceptions are used to handle errors and provide meaningful error messages to users and
   developers.

6. Service Provider Interface (SPI): The LPhy extension mechanism utilizes the Java Platform Module System (JPMS) and
   SPI to allow the third party developers to develop LPhy extensions by extending
   generative distributions and deterministic functions.

## TODO:

- Shall the narratives be required as the part of core?

- Simulate should work on interfaces, so that it can be the part of core.
