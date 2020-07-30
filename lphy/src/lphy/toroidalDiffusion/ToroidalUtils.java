package lphy.toroidalDiffusion;

public class ToroidalUtils {
    public static double wrapToMaxAngle(double rawAngle, double MAX_ANGLE_VALUE) {
        if (rawAngle > MAX_ANGLE_VALUE) {
            int K = (int)Math.floor(rawAngle / MAX_ANGLE_VALUE);
            double fractionRemainder = rawAngle / MAX_ANGLE_VALUE - K;
            return fractionRemainder * MAX_ANGLE_VALUE;
        }

        if (rawAngle < 0.0) {
            int K = (int)Math.floor(-rawAngle / MAX_ANGLE_VALUE);
            double fractionRemainder = (-rawAngle / MAX_ANGLE_VALUE) - K;
            return MAX_ANGLE_VALUE - (fractionRemainder * MAX_ANGLE_VALUE);
        }

        return rawAngle;
    }
}
