package info.hugoyu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocoProfile {
    public static final double ACC_RATE_COEF = 45;
    public static final double DEC_RATE_COEF = 45;

    private int length;
    private double accRate, decRate;
    private Map<Double, Integer> speedMap = new HashMap<>(); // speed -> throttle
    private Map<Integer, Double> throttleMap = new HashMap<>(); // throttle -> speed
    private List<Double> speedPoints = new ArrayList<>();
    private List<Integer> throttlePoints = new ArrayList<>();

    public LocoProfile(String profilePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(profilePath));

        int lengthControlCar = Integer.parseInt(br.readLine());
        int lengthPassengerCar = Integer.parseInt(br.readLine());
        int numControlCars = Integer.parseInt(br.readLine());
        int numPassengerCars = Integer.parseInt(br.readLine());
        length = numControlCars * lengthControlCar + numPassengerCars + lengthPassengerCar;

        accRate = Double.parseDouble(br.readLine()) * ACC_RATE_COEF;
        decRate = Double.parseDouble(br.readLine()) * DEC_RATE_COEF;

        String line;
        while ((line = br.readLine()) != null) {
            String[] args = line.split(" ");
            int throttleByte = Integer.parseInt(args[0]);
            double speed = Double.parseDouble(args[1]);
            speedMap.put(speed, throttleByte);
            speedPoints.add(speed);
            throttleMap.put(throttleByte, speed);
            throttlePoints.add(throttleByte);
        }
    }

    public double getAccRate() {
        return accRate;
    }

    public double getDecRate() {
        return decRate;
    }

    public float getThrottle(double speed) throws IllegalArgumentException {
        for (int i = 0; i < speedPoints.size(); i++) {
            double speedPoint = speedPoints.get(i);
            if (speed <= speedPoint) {
                int hi = speedMap.get(speedPoint);
                if (speed == speedPoint) return hi / 128.0f;
                double prevSpeedPoint = speedPoints.get(i-1);
                int lo = speedMap.get(prevSpeedPoint);
                return (float) ((lo + (speed - prevSpeedPoint) / (speedPoint - prevSpeedPoint) * (hi - lo)) / 128.0);
            }
        }
        throw new IllegalArgumentException("invalid speed: " + speed);
    }

    public double getSpeed(int throttleByte) throws IllegalArgumentException {
        for (int i = 0; i < throttlePoints.size(); i++) {
            int throttlePoint = throttlePoints.get(i);
            if (throttleByte <= throttlePoint) {
                double hi = throttleMap.get(throttlePoint);
                if (throttleByte == throttlePoint) return hi;
                int prevThrottlePoint = throttlePoints.get(i-1);
                double lo = throttleMap.get(prevThrottlePoint);
                return lo + (throttleByte - prevThrottlePoint) * 1.0 / (throttlePoint - prevThrottlePoint) * (hi - lo);
            }
        }
        throw new IllegalArgumentException("invalid throttleByte: " + throttleByte);
    }

    public double getMaxSpeed() {
        return speedPoints.get(speedPoints.size() - 1);
    }

}
