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
     *
     * @param trainset      Trainset being profiled
     * @param sensor   Pin for sensor
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

        Map<Integer, List<Double>> speedMapForward = new HashMap<>();
        Map<Integer, List<Double>> speedMapBackward = new HashMap<>();
        for (int throttle = startThrottle; throttle <= endThrottle; throttle += step) {
            // forward
            System.out.println("Setting throttle to forward " + throttle);
            TrainUtil.setThrottle(trainset, throttle);
            recordSpeed(profilingListener, sectionLength, throttle, speedMapForward);

            // continue moving the train away from the timing zone
            Thread.sleep(2000);
            TrainUtil.setThrottle(trainset, 0);
            Thread.sleep(1000);

            // backward
            System.out.println("Setting throttle to backward " + throttle);
            TrainUtil.setThrottle(trainset, -throttle);
            recordSpeed(profilingListener, sectionLength, throttle, speedMapBackward);

            // continue moving the train away from the timing zone
            Thread.sleep(2000);
        }

        TrainUtil.setThrottle(trainset, 0);

        System.out.println("forward:");
        System.out.println(speedMapForward);
        System.out.println("backward:");
        System.out.println(speedMapBackward);
    }

    private static void recordSpeed(ProfilingListener profilingListener, int sectionLength, int throttle,
                                    Map<Integer, List<Double>> speedMap) throws ExecutionException, InterruptedException {
        long deltaT = profilingListener.getProfilingResult().get();
        double speedKph = getSpeed(deltaT, sectionLength);
        speedMap.putIfAbsent(throttle, new ArrayList<>());
        speedMap.get(throttle).add(speedKph);
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
