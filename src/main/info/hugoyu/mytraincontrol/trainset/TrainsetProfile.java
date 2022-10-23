package info.hugoyu.mytraincontrol.trainset;

import info.hugoyu.mytraincontrol.json.JsonParsable;
import info.hugoyu.mytraincontrol.util.SpeedUtil;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Setter
@Getter
public class TrainsetProfile implements JsonParsable {

    private static final double ACC_RATE_COEF = 15;
    private static final double DEC_RATE_COEF = 15;

    private int[] controlCarLengths, passengerCarLengths;
    private int[] numControlCars, numPassengerCars;
    private double accRate, decRate;
    private double topSpeed;
    private Map<Integer, Double> throttleSpeedMap;

    private List<Integer> throttleList;
    private TreeMap<Double, Integer> speedThrottleMap;

    @Override
    public void postDeserialization() {
        // convert speed from km/h to scale-equivalent mm/s
        topSpeed = SpeedUtil.toMMps(topSpeed);
        throttleSpeedMap.replaceAll((throttle, speed) -> speed = SpeedUtil.toMMps(speed));

        throttleList = throttleSpeedMap.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        speedThrottleMap = new TreeMap<>();
        throttleSpeedMap.forEach((throttle, speed) -> speedThrottleMap.put(speed, throttle));

        // adjust acc/dec rate with coefficient
        accRate *= ACC_RATE_COEF;
        decRate *= DEC_RATE_COEF;
    }

    public int getTotalLength() {
        int res = 0;
        for (int i = 0; i < controlCarLengths.length; i++) {
            res += controlCarLengths[i] * numControlCars[i];
        }
        for (int i = 0; i < passengerCarLengths.length; i++) {
            res += passengerCarLengths[i] * numPassengerCars[i];
        }
        return res;
    }

    /**
     * @param speed target speed, mm/s
     * @return throttle value, [0.0, 1.0]
     */
    public float getThrottle(double speed) {
        final double throttleDouble = getThrottlePercentage(speed) / 100.0;

        BigDecimal bd = new BigDecimal(throttleDouble);
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    private double getThrottlePercentage(double speed) {
        Map.Entry<Double, Integer> floorEntry = speedThrottleMap.floorEntry(speed);
        Map.Entry<Double, Integer> ceilingEntry = speedThrottleMap.ceilingEntry(speed);

        if (floorEntry == null) { // target speed is lower than min speed, return min throttle
            return ceilingEntry.getValue();
        }
        if (ceilingEntry == null) { // target speed is higher than max speed, return max throttle
            return floorEntry.getValue();
        }

        final double floorSpeed = floorEntry.getKey();
        final double floorThrottle = floorEntry.getValue();

        if (floorSpeed == speed) { // target speed is an exact match, return throttle
            return floorThrottle;
        }

        final double deltaSpeed = ceilingEntry.getKey() - floorSpeed;
        final double deltaThrottle = ceilingEntry.getValue() - floorThrottle;

        return (speed - floorSpeed) / deltaSpeed * deltaThrottle + floorThrottle;
    }

    public double getMinimumStoppingDistance(double speed) {
        return -Math.pow(speed, 2) / 2 / decRate;
    }

}
