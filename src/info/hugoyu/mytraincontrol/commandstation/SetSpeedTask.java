package info.hugoyu.mytraincontrol.commandstation;

import info.hugoyu.mytraincontrol.registries.ThrottleRegistry;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.trainset.TrainsetProfile;
import jmri.DccThrottle;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Map;

public class SetSpeedTask implements CommandStationTask {

    public static final double ACC_RATE_COEF = 20;
    public static final double DEC_RATE_COEF = 20;

    private Trainset trainset;
    private long taskCreationTime;

    public SetSpeedTask(Trainset trainset) {
        this.trainset = trainset;
        this.taskCreationTime = System.currentTimeMillis();
//        log.info(trainset.getName() + ": SetSpeedTask created");
    }

    @Override
    public void execute() {
//        log.info(trainset.getName() + ": SetSpeedTask executed");
        if (trainset.getCSpeed() != trainset.getTSpeed()) {
            boolean isAcc = trainset.getCSpeed() < trainset.getTSpeed();

            double t = (System.currentTimeMillis() - taskCreationTime) / 1000.0;

            TrainsetProfile profile = trainset.getProfile();
            double a = isAcc ? profile.getAccRate() * ACC_RATE_COEF : profile.getDecRate() * DEC_RATE_COEF;

            double cSpeed = trainset.getCSpeed() + a * t;
            if (isAcc) {
                cSpeed = Math.min(cSpeed, trainset.getTSpeed());
            } else {
                cSpeed = Math.max(cSpeed, trainset.getTSpeed());
            }

//            log.info(trainset.getName() + ": setting speed from " + trainset.getCSpeed() + " to " + cSpeed);
            trainset.setCSpeed(cSpeed);

            DccThrottle throttle = ThrottleRegistry.getInstance().getThrottle(trainset.getAddress());
            float throttleValue = getThrottle(cSpeed);
//            log.info(trainset.getName() + ": setting throttle to " + throttleValue);
            throttle.setSpeedSetting(throttleValue);

            trainset.onSetSpeedTaskCompleted();
        }
    }

    private float getThrottle(double speed) {
        TrainsetProfile profile = trainset.getProfile();
        Map<Integer, Double> throttleSpeedMap = profile.getThrottleSpeedMap();
        List<Integer> throttleList = profile.getThrottleList();

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
