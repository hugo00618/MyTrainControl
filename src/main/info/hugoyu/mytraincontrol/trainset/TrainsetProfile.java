package info.hugoyu.mytraincontrol.trainset;

import info.hugoyu.mytraincontrol.json.JsonParsable;
import info.hugoyu.mytraincontrol.util.SpeedUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
public class TrainsetProfile implements JsonParsable {

    private static final double ACC_RATE_COEF = 15;
    private static final double DEC_RATE_COEF = 15;

    private int controlCarLength, passengerCarLength;
    private int numControlCars, numPassengerCars;
    private double accRate, decRate;
    private double topSpeed;
    private Map<Integer, Double> throttleSpeedMap;

    private List<Integer> throttleList;

    @Override
    public void postDeserialization() {
        // convert speed from km/h to scale-equivalent mm/s
        topSpeed = SpeedUtil.toMMps(topSpeed);
        for (Map.Entry<Integer, Double> entry : throttleSpeedMap.entrySet()) {
            double mmps = SpeedUtil.toMMps(entry.getValue());
            throttleSpeedMap.put(entry.getKey(), mmps);
        }

        throttleList = new ArrayList<>(throttleSpeedMap.keySet());
        Collections.sort(throttleList);

        accRate *= ACC_RATE_COEF;
        decRate *= DEC_RATE_COEF;
    }

    public int getTotalLength() {
        return controlCarLength * numControlCars + passengerCarLength * numPassengerCars;
    }

    public float getThrottle(double speed) {
        int minThrottle = throttleList.get(0);
        double minSpeed = throttleSpeedMap.get(minThrottle);
        if (speed <= minSpeed) {
            return minThrottle / 100.0f;
        }
        int maxThrottle = throttleList.get(throttleList.size() - 1);
        double maxSpeed = throttleSpeedMap.get(maxThrottle);
        if (speed >= maxSpeed) {
            return maxThrottle / 100.0f;
        }

        int i = binarySearchInterval(speed, throttleSpeedMap, throttleList);
        int lowerBoundThrottle = throttleList.get(i);
        int upperBoundThrottle = throttleList.get(i + 1);
        int deltaThrottle = upperBoundThrottle - lowerBoundThrottle;
        double lowerBoundSpeed = throttleSpeedMap.get(lowerBoundThrottle);
        double upperBoundSpeed = throttleSpeedMap.get(upperBoundThrottle);
        double deltaSpeed = upperBoundSpeed - lowerBoundSpeed;

        return (float) ((lowerBoundThrottle + (speed - lowerBoundSpeed) / deltaSpeed * deltaThrottle) / 100.0);
    }

    public double getMinimumStoppingDistance(double speed) {
        return -Math.pow(speed, 2) / 2 / decRate;
    }

    /**
     * @param speed
     * @param throttleSpeedMap
     * @param throttleList
     * @return lower bound index of interval
     */
    private int binarySearchInterval(double speed, Map<Integer, Double> throttleSpeedMap, List<Integer> throttleList) {
        int i = 0, j = throttleList.size() - 1;
        while (i + 1 < j) {
            int mid = (i + j) / 2;
            int midThrottle = throttleList.get(mid);
            double midSpeed = throttleSpeedMap.get(midThrottle);
            if (speed < midSpeed) {
                j = mid;
            } else if (speed > midSpeed) {
                i = mid;
            } else {
                return i;
            }
        }
        return i;
    }

}
