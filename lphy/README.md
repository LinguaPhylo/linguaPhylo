# LPhy Core

The "lphy" [subproject](https://docs.gradle.org/current/userguide/multi_project_builds.html)
is the cornerstone of the entire LinguaPhylo project, encompassing critical components and
features that drive the project's functionality and capabilities.
It integrates essential elements such as the ANTLR Parser, vectorization techniques,
built-in functions and native functions, Java Exceptions for error handling,
and the Service Provider Interface (SPI):

## Java packages

1. `lphy.core.parser`: The "lphy" subproject includes an ANTLR Parser that defines the grammar of the LPhy language and
   converts scripts into probabilistic models implemented as Java objects.

2. Built-in functions and native functions: The built-in functions are directly implemented during parsing,
   such as numeric and logical operations, some common functions (e.g., min, log, exp, etc.), and trigonometric
   functions. The native functions refer to deterministic functions that third-party developers cannot implement using
   the exported core API. They are considered as the part of the "parser" package.

3. `lphy.core.model`: The LPhy models implemented as Java objects consists of variables, arrays of variables, and generators.
   Generators include generative distributions, deterministic functions, and method calls.
   In addition, the basic data types (e.g., IntegerVale, ArrayValue, MapValue) are considered as the part of the "model"
   package.

4. `lphy.core.vectorization`: LPhy supports implicit vectorization, similar to R, by matching inputs to functions and generative
   distributions. It is called as "vector match" in the Java implementation.
   Alternatively, users can generate vectors of independent and identically distributed (IID) random variables using
   the "replicates" argument for generative distributions.
   But this is currently not available to functions.

5. `lphy.core.exception`: These Java Exceptions are used to handle errors and provide meaningful error messages to users and
   developers.

6. `lphy.core.spi`: The LPhy extension mechanism utilizes the Java Platform Module System (JPMS) and
   SPI to allow the third party developers to develop LPhy extensions by extending
   generative distributions and deterministic functions.

## TODO:

- Currently, the classes related to the narratives are consolidated within the `lphy.core.model` package. 
  However, it is ideal to separate them into a distinct package in the future 
  when we can resolve the cyclic dependencies between them.

- Simulate should work on interfaces, so that it can be the part of core.
