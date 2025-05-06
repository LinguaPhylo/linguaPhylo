package lphy.base.evolution.continuous;


/**
 * Demonstration: compute E[Z] by a purely analytic Taylor expansion,
 * without any "step" for numeric integration.
 *
 * E[Z] = (1/t)* ∫_{s=0..t} exp(v0 + x(s)) ds
 * where x(s) = ( (vt - v0)/t )*s + (sigma^2 / (2*t))* s*(t-s).
 *
 * We expand exp(v0 + x) ~ exp(v0)* ∑_{k=0..N-1} [ x^k / k! ],
 * then each (x(s))^k is integrated term-by-term as a polynomial in s.
 */
public class MeanZCalculator {

    /**
     * Compute E(Z) by analytic expansion + polynomial integration.
     * @param r0     branch start rate (>0)
     * @param rt     branch end rate   (>0)
     * @param t      branch length     (>0)
     * @param phi    sigma^2 (Brownian variance scale, >=0)
     * @param order  Taylor expansion order, e.g. 10
     * @return approximate E(Z)
     */
    public static double computeMeanZ(double r0, double rt,
                                      double t, double phi,
                                      int order)
    {
        if (t <= 0.0 || order < 1) {
            throw new IllegalArgumentException("Invalid t or order.");
        }
        if (r0 <= 0.0 || rt <= 0.0) {
            throw new IllegalArgumentException("Rates must be positive.");
        }
        // v0, vT
        double v0 = Math.log(r0);
        double vT = Math.log(rt);


        double a = (vT - v0) / t;
        double b = phi / (2.0 * t);

        // Then x(s) = (a + b*t)* s  -  b * s^2
        double A = a + b*t;
        double B = b; // so x(s) = A*s - B*s^2


        double sumAll = 0.0;

        for (int k = 0; k < order; k++) {

            double term_k = 0.0;
            for (int m = 0; m <= k; m++) {
                long c = binomial(k, m);
                double sign = ((m % 2) == 0) ? 1.0 : -1.0;
                // coefficient
                double cAB = c * sign * Math.pow(A, k - m) * Math.pow(B, m);
                // power of s is k - m + 2*m = k + m
                int p = k + m;
                double factor = cAB * (1.0 / (p+1.0)) * Math.pow(t, p+1.0);
                term_k += factor;
            }

            double factInv = 1.0 / factorial(k);
            sumAll += factInv * term_k;
        }

        double result = Math.exp(v0) * sumAll / t;
        return result;
    }

    /** A simple factorial function for small k (<= 20 or so). */
    private static double factorial(int n) {

        double f = 1.0;
        for (int i = 1; i <= n; i++) {
            f *= i;
        }
        return f;
    }

    /** Compute binomial coefficient "n choose k" in a basic integer manner. */
    private static long binomial(int n, int k) {

        if (k < 0 || k > n) return 0;
        long c = 1;
        for (int i = 0; i < k; i++) {
            c = c * (n - i) / (i + 1);
        }
        return c;
    }

    // ------------------------------------------------------------------

    public static void main(String[] args) {
        // Example usage:
        double r0   = 2.0;   // start rate
        double rt   = 3.5;   // end rate
        double tLen = 1.0;   // length
        double phi  = 0.2;   // sigma^2
        int order   = 16;    // Taylor expansion order

        double val = computeMeanZ(r0, rt, tLen, phi, order);
        System.out.println("E[Z] approx by analytic Taylor, order="+order+": " + val);

        // Compare to Simpson approach, for instance:
        double valSimpson = compareSimpson(r0, rt, tLen, phi, 200000);
        System.out.println("Simpson(200 steps) = " + valSimpson);
        System.out.println("difference = " + Math.abs(val - valSimpson));
    }

    // A quick Simpson method to compare, if you want:
    private static double compareSimpson(double r0, double rt, double t, double phi, int steps) {
        double v0 = Math.log(r0);
        double vT = Math.log(rt);
        double h = t / steps;
        double f0 = integrand(v0, vT, 0.0, t, phi);
        double fN = integrand(v0, vT, t, t, phi);
        double sum = f0 + fN;
        for (int i = 1; i < steps; i++) {
            double s = i*h;
            double val = integrand(v0, vT, s, t, phi);
            if ((i % 2) == 1) sum += 4*val; else sum += 2*val;
        }
        double integral = sum*(h/3.0);
        return integral/t;
    }
    // same integrand as before
    private static double integrand(double v0, double vT, double s, double t, double phi) {
        double meanVs = v0 + (vT - v0)*(s/t);
        double varVs  = phi*(s*(t-s)/t);
        return Math.exp(meanVs + 0.5*varVs);
    }
}
