binaryRateMatrix function
=========================
binaryRateMatrix(Number **lambda**)
-----------------------------------

The binary trait instantaneous rate matrix. Takes a lambda and produces an instantaneous rate matrix:

    Q = ⎡-1  1⎤
        ⎣ λ -λ⎦

### Parameters

- Number **lambda** - the lambda parameter of the binary process. Rate of 0->1 is 1, rate of 1->0 is lambda.

### Return type

- Double[][]



