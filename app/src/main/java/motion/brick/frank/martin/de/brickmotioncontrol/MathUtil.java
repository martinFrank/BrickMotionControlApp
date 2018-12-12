package motion.brick.frank.martin.de.brickmotioncontrol;

public class MathUtil {

    private MathUtil() {
    }

        public static double limit(double value) {
            return limit(-100,value,100);
        }

        public static double limit(double min, double value, double max) {
            double limit = value;
            limit = Math.min(max,limit);
            limit = Math.max(min, limit);
            return limit;
        }
}
