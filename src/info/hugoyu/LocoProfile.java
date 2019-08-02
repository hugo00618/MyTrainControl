package info.hugoyu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocoProfile {

    private double accRate, decRate;
    private Map<Double, Integer> speedMap = new HashMap<>();
    private List<Double> speedPoints = new ArrayList<>();

    public LocoProfile(String profilePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(profilePath));

        accRate = Double.parseDouble(br.readLine()) * Loco.ACC_RATE_COEF;
        decRate = Double.parseDouble(br.readLine()) * Loco.DEC_RATE_COEF;

        String line;
        while ((line = br.readLine()) != null) {
            String[] args = line.split(" ");
            int throttleByte = Integer.parseInt(args[0]);
            double speed = Double.parseDouble(args[1]);
            speedMap.put(speed, throttleByte);
            speedPoints.add(speed);
        }
    }

    public double getAccRate() {
        return accRate;
    }

    public double getDecRate() {
        return decRate;
    }

    public double getThrottleByte(double speed) throws IllegalArgumentException {
        for (int i = 0; i < speedPoints.size(); i++) {
            double speedPoint = speedPoints.get(i);
            if (speed <= speedPoint) {
                int hi = speedMap.get(speedPoint);
                if (speed == speedPoint) return hi;
                double prevSpeedPoint = speedPoints.get(i-1);
                int lo = speedMap.get(prevSpeedPoint);
                return lo + (speed - prevSpeedPoint) / (speedPoint - prevSpeedPoint) * (hi - lo);
            }
        }
        throw new IllegalArgumentException("invalid speed: " + speed);
    }

    public double getMaxSpeed() {
        return speedPoints.get(speedPoints.size() - 1);
    }

    public boolean isNeedToStop(double speed, double moveDist) {
        double stoppingDist = Math.pow(speed, 2) / 2 / -decRate;
        return stoppingDist >= moveDist;
    }
}
