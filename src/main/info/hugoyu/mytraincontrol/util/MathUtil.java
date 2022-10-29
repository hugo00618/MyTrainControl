package info.hugoyu.mytraincontrol.util;

import com.google.common.annotations.VisibleForTesting;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class MathUtil {

    private static final int DEFAULT_SCALE = 4;

    /**
     * @param data
     * @param m    number of standard deviation to be included in data
     * @return
     */
    public static List<Double> removeOutliers(List<Double> data, double m) {
        BigDecimal mean = mean(data);
        BigDecimal sd = standardDeviation(data);
        BigDecimal threshold = sd.multiply(BigDecimal.valueOf(m));

        return data.stream()
                .filter(ele -> BigDecimal.valueOf(ele).subtract(mean).abs().compareTo(threshold) < 0)
                .collect(Collectors.toList());
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

    @VisibleForTesting
    static BigDecimal standardDeviation(List<Double> data) {
        return standardDeviation(data, DEFAULT_SCALE);
    }

    private static BigDecimal standardDeviation(List<Double> data, int scale) {
        final int intermScale = scale + 1;
        BigDecimal mean = mean(data, intermScale);

        return data.stream()
                .map(ele -> BigDecimal.valueOf(ele).subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add) // sum
                .divide(BigDecimal.valueOf(data.size()), intermScale, RoundingMode.HALF_UP) // divide by N
                .sqrt(new MathContext(intermScale))
                .setScale(scale, RoundingMode.HALF_UP);
    }
}
