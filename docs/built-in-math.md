Built-in math functions
============

abs([Number](types/Number.md) **x**)
-------------------
Return the absolute value of x. Return type [Double](types/Double.md).

cLogLog([Number](types/Number.md) **x**)
-------------------
The complementary log-log function. Return type [Double](types/Double.md).

cbrt([Number](types/Number.md) **x**)
-------------------
The cube root function: x^3. Return type [Double](types/Double.md).

ceil([Number](types/Number.md) **x**)
-------------------
Return the smallest (closest to negative infinity) floating-point value that is greater than or equal to the argument and is equal to a mathematical integer. Return type [Double](types/Double.md).

exp([Number](types/Number.md) **x**)
-------------------
The exponential function: e^x. Return type [Double](types/Double.md).

expm1([Number](types/Number.md) **x**)
-------------------
The function: e^x - 1. Return type [Double](types/Double.md).

floor([Number](types/Number.md) **x**)
-------------------
Return the largest (closest to positive infinity) floating-point value that less than or equal to the argument and is equal to a mathematical integer. Return type [Double](types/Double.md).

log([Number](types/Number.md) **x**)
-------------------
The natural logarithm (base e) function: ln(x). Return type [Double](types/Double.md).

log10([Number](types/Number.md) **x**)
-------------------
Return the base 10 logarithm of x. Return type [Double](types/Double.md).

log1p([Number](types/Number.md) **x**)
-------------------
Return the natural logarithm of x + 1, namely ln(x + 1). Return type [Double](types/Double.md).

logFact([Number](types/Number.md) **x**)
-------------------
Return the natural log of the factorial of x. Return type [Double](types/Double.md).

logGamma([Number](types/Number.md) **x**)
-------------------
Return the value of ln(Gamma(x)), or Double.NaN if x <= 0.0. Return type [Double](types/Double.md).

logit([Number](types/Number.md) **x**)
-------------------
The logarithm of the odds: ln(x/(1-x)), if x is a probability. Return type [Double](types/Double.md).

phi([Number](types/Number.md) **x**)
-------------------
The cumulative distribution function (CDF) of the standard normal distribution (mean = 0, standard deviation = 1). Return type [Double](types/Double.md).

probit([Number](types/Number.md) **x**)
-------------------
The inverse of the cumulative distribution function of the standard normal distribution: 
[probit(x) = sqrt(2) * inverse_erf(2x-1)](https://en.wikipedia.org/wiki/Probit#Computation). Return type [Double](types/Double.md).

round([Number](types/Number.md) **x**)
-------------------
Return the closest integer to x, but convert to double type. Return type [Double](types/Double.md).

signum([Number](types/Number.md) **x**)
-------------------
Return 0 if x is 0, 1.0 if x > 0, or -1.0 if x < 0. Return type [Double](types/Double.md).

sqrt([Number](types/Number.md) **x**)
-------------------
Return the positive square root of x. Return type [Double](types/Double.md).

step([Number](types/Number.md) **x**)
-------------------
Return 1.0 if x > 0, otherwise return 0. Return type [Double](types/Double.md).
