package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.sensor.SensorChangeListener;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import jmri.Sensor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SpeedProfilingUtil {

    private static final int NUMBER_OF_DATA_POINTS_TO_COLLECT = 10;
    private static final double OUTLIERS_RANGE = 0.05;

    private static class ProfilingListener {
        private final Sensor s1, s2;

        long t0, t1;

        CompletableFuture<Long> deltaT;

        CompletableFuture<Long> getProfilingResult() {
            deltaT = new CompletableFuture<>();
            return deltaT;
        }

        private final SensorChangeListener sensorChangeListener = new SensorChangeListener() {
            @Override
            public void onEnter(Sensor sensor) {
                if (t0 == 0) {
                    t0 = System.currentTimeMillis();
                } else {
                    t1 = System.currentTimeMillis();
                }
            }

            @Override
            public void onExit(Sensor sensor) {
                if (t1 != 0) {
                    deltaT.obtrudeValue(t1 - t0);
                    t0 = 0;
                    t1 = 0;
                }
            }
        };

        public ProfilingListener(int s1Address, int s2Address) {
            s1 = SensorUtil.getSensor(s1Address, sensorChangeListener);
            s2 = SensorUtil.getSensor(s2Address, sensorChangeListener);
        }
    }

    /**
     * @param trainset          Trainset being profiled
     * @param sensor1           sensor 1 pin number
     * @param sensor2           sensor 2 pin number
     * @param sectionLength     section length, in mm
     * @param startThrottle     starting throttle
     * @param endThrottle       ending throttle
     * @param step              increment step
     * @param isStartingForward whether the initial direction is forward
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void profileSpeed(Trainset trainset,
                                    int sensor1,
                                    int sensor2,
                                    int sectionLength,
                                    int startThrottle,
                                    int endThrottle,
                                    int step,
                                    boolean isStartingForward)
            throws ExecutionException, InterruptedException {
        validateThrottleParams(startThrottle, endThrottle, step);

        ProfilingListener profilingListener = new ProfilingListener(sensor1, sensor2);

        Map<Integer, List<Double>> speedRecords = new HashMap<>();
        TreeMap<String, Double> speedMap = new TreeMap<>(Comparator.comparing(Double::valueOf));
        for (int throttle = startThrottle; throttle <= endThrottle; throttle += step) {
            while (!isSufficientDatapointsCollected(speedRecords, throttle)) {
                // forward
                measureThrottle(trainset, speedRecords, throttle, isStartingForward, profilingListener, sectionLength);

                // backward
                measureThrottle(trainset, speedRecords, throttle, !isStartingForward, profilingListener, sectionLength);
            }

            // calculate mean speed
            speedMap.put(String.valueOf(throttle), MathUtil.mean(speedRecords.get(throttle)).doubleValue());

            // write to file
            String fileName = trainset.getName() + ".json";
            FileUtil.writeJson(fileName, speedMap);
        }
    }

    private static void measureThrottle(Trainset trainset,
                                        Map<Integer, List<Double>> speedRecords,
                                        int throttle,
                                        boolean isForward,
                                        ProfilingListener profilingListener,
                                        int sectionLength)
            throws ExecutionException, InterruptedException {
        System.out.println(String.format("%s: measuring throttle %d, %d of %d datapoints",
                trainset.getName(),
                throttle,
                getNumberOfDataPointsCollected(speedRecords, throttle) + 1,
                NUMBER_OF_DATA_POINTS_TO_COLLECT));

        TrainUtil.setThrottle(trainset, isForward ? throttle : -throttle);

        // wait for deltaT to be available
        long deltaT = profilingListener.getProfilingResult().get();
        recordSpeed(deltaT, sectionLength, throttle, speedRecords);

        // continue moving the train away from the timing zone
        Thread.sleep(2000);
        TrainUtil.setThrottle(trainset, 0);
        Thread.sleep(1000);
    }

    private static int getNumberOfDataPointsCollected(Map<Integer, List<Double>> speedRecords, int throttle) {
        return speedRecords.containsKey(throttle) ? speedRecords.get(throttle).size() : 0;
    }

    private static boolean isSufficientDatapointsCollected(Map<Integer, List<Double>> speedRecords, int throttle) {
        return getNumberOfDataPointsCollected(speedRecords, throttle) >= NUMBER_OF_DATA_POINTS_TO_COLLECT;
    }

    private static void recordSpeed(long deltaT,
                                    int sectionLength,
                                    int throttle,
                                    Map<Integer, List<Double>> speedRecords) {
        double speedKph = getSpeed(deltaT, sectionLength);

        List<Double> datapoints = speedRecords.getOrDefault(throttle, new ArrayList<>());
        datapoints.add(speedKph);

        if (datapoints.size() >= NUMBER_OF_DATA_POINTS_TO_COLLECT) {
            datapoints = MathUtil.removeOutliers(datapoints, OUTLIERS_RANGE);
        }

        speedRecords.put(throttle, datapoints);
    }

    private static double getSpeed(long deltaT, int sectionLength) {
        double deltaTSec = deltaT / 1000.0;
        double speedMmps = sectionLength / deltaTSec;
        return SpeedUtil.toKph(speedMmps);
    }

    private static void validateThrottleParams(int startThrottle, int endThrottle, int step) {
        if (!(startThrottle > 0 && startThrottle <= 100) ||
                !(endThrottle > 0 && endThrottle <= 100) ||
                !(step > 0)) {
            throw new RuntimeException("Invalid throttle parameters");
        }
    }
}
