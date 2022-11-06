package info.hugoyu.mytraincontrol.util;

import com.google.common.annotations.VisibleForTesting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MathUtil {

    private static final int DEFAULT_SCALE = 4;

    /**
     * @param data
     * @param range   range of +/- percentage of median to be included in data
     *                  e.g. range = 0.05, median = 90 -> function will include data in the range
     *                  (90 * 0.95, 90 * 1.05)
     * @return  data with outliers removed
     */
    public static List<Double> removeOutliers(List<Double> data, double range) {
        final double median = median(data);
        final double lowerBound = median * (1 - range);
        final double upperBound = median * (1 + range);

        return data.stream()
                .filter(ele -> ele > lowerBound && ele < upperBound)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param min
     * @param max
     * @return a random number within range [min, max]
     */
    public static int random(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    private static double median(List<Double> data) {
        List<Double> backup = new ArrayList<>(data);
        Collections.sort(backup);
        if (backup.size() % 2 == 0) {
            return (backup.get(backup.size() / 2) + backup.get(backup.size() / 2 - 1)) / 2.0;
        } else {
            return backup.get(backup.size() / 2);
        }
    }

    @VisibleForTesting
    static BigDecimal mean(List<Double> data) {
        return mean(data, DEFAULT_SCALE);
    }

    private static BigDecimal mean(List<Double> data, int scale) {
        final int intermScale = scale + 1;
        return data.stream()
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add) // sum
                .divide(BigDecimal.valueOf(data.size()), intermScale, RoundingMode.HALF_UP) // divide by N
                .setScale(scale, RoundingMode.HALF_UP);
    }
}
