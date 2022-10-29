package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.sensor.SensorChangeListener;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import jmri.Sensor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SpeedProfilingUtil {

    private static final int NUMBER_OF_DATA_POINTS_TO_COLLECT = 10;
    private static final double OUTLIERS_RANGE = 0.02;

    @RequiredArgsConstructor
    private static class ProfilingListener implements SensorChangeListener {
        @NonNull
        Trainset trainset;

        long t0, t1;

        CompletableFuture<Long> deltaT;

        CompletableFuture<Long> getProfilingResult() {
            deltaT = new CompletableFuture<>();
            return deltaT;
        }

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
                long deltaTVal = t1 - t0;
                t0 = 0;
                t1 = 0;
                deltaT.obtrudeValue(deltaTVal);
            }
        }
    }

    /**
     * @param trainset      Trainset being profiled
     * @param sensor        Pin for sensor
     * @param sectionLength section length, in mm
     * @param startThrottle starting throttle
     * @param endThrottle   ending throttle
     * @param step          increment step
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void speedProfile(Trainset trainset, int sensor, int sectionLength,
                                    int startThrottle, int endThrottle, int step) throws ExecutionException, InterruptedException {
        validateThrottleParams(startThrottle, endThrottle, step);

        ProfilingListener profilingListener = new ProfilingListener(trainset);
        SensorUtil.getSensor(sensor, profilingListener);

        System.out.println(String.format("Profiling %s", trainset.getName()));

        Map<Integer, List<Double>> speedMap = new HashMap<>();
        for (int throttle = startThrottle; throttle <= endThrottle; throttle += step) {
            while (!isSufficientDatapointsCollected(speedMap, throttle)) {
                // forward
                measureThrottle(trainset, speedMap, throttle, true, profilingListener, sectionLength);

                // backward
                measureThrottle(trainset, speedMap, throttle, false, profilingListener, sectionLength);
            }
        }

        System.out.println("speedMap:");
        System.out.println(speedMap);
    }

    private static void measureThrottle(Trainset trainset, Map<Integer, List<Double>> speedMap,
                                        int throttle, boolean isForward,
                                        ProfilingListener profilingListener, int sectionLength)
            throws ExecutionException, InterruptedException {
        System.out.println(String.format("Measuring throttle %d, %d of %d datapoints",
                throttle,
                getNumberOfDataPointsCollected(speedMap, throttle) + 1,
                NUMBER_OF_DATA_POINTS_TO_COLLECT));
        TrainUtil.setThrottle(trainset, isForward ? throttle : -throttle);
        recordSpeed(profilingListener, sectionLength, throttle, speedMap);

        // continue moving the train away from the timing zone
        Thread.sleep(2000);
        TrainUtil.setThrottle(trainset, 0);
        Thread.sleep(1000);
    }

    private static int getNumberOfDataPointsCollected(Map<Integer, List<Double>> speedMap, int throttle) {
        return speedMap.containsKey(throttle) ? speedMap.get(throttle).size() : 0;
    }

    private static boolean isSufficientDatapointsCollected(Map<Integer, List<Double>> speedMap, int throttle) {
        return getNumberOfDataPointsCollected(speedMap, throttle) >= NUMBER_OF_DATA_POINTS_TO_COLLECT;
    }

    private static void recordSpeed(ProfilingListener profilingListener, int sectionLength, int throttle,
                                    Map<Integer, List<Double>> speedMap) throws ExecutionException, InterruptedException {
        long deltaT = profilingListener.getProfilingResult().get();
        double speedKph = getSpeed(deltaT, sectionLength);

        List<Double> datapoints = speedMap.getOrDefault(throttle, new ArrayList<>());
        datapoints.add(speedKph);

        if (datapoints.size() >= NUMBER_OF_DATA_POINTS_TO_COLLECT) {
            datapoints = MathUtil.removeOutliers(datapoints, OUTLIERS_RANGE);
        }

        speedMap.put(throttle, datapoints);
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
