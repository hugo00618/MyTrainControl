package info.hugoyu.mytraincontrol.ato;

import info.hugoyu.mytraincontrol.layout.Route;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.GeneralUtil;
import info.hugoyu.mytraincontrol.util.MathUtil;
import info.hugoyu.mytraincontrol.util.RouteUtil;
import info.hugoyu.mytraincontrol.util.TrainUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

public class AutomaticTrainOperationThread extends Thread {

    private AutomaticTrainOperationRunnable runnable;

    public AutomaticTrainOperationThread(Trainset trainset) {
        this(new AutomaticTrainOperationRunnable(trainset));
    }

    private AutomaticTrainOperationThread(AutomaticTrainOperationRunnable runnable) {
        super(runnable);
        this.runnable = runnable;
    }

    public void signalTerminate() {
        runnable.terminate = true;
    }

    @RequiredArgsConstructor
    static class AutomaticTrainOperationRunnable implements Runnable {

        private static final int MIN_SLEEP_MILLIS = 30 * 1000; // 30 secs
        private static final int MAX_SLEEP_MILLIS = 60 * 1000; // 60 secs

        @NonNull
        private Trainset trainset;

        private boolean terminate = false;

        @Override
        public void run() {
            while (!terminate) {
                moveToRandomStation();
                try {
                    Thread.sleep(MathUtil.random(MIN_SLEEP_MILLIS, MAX_SLEEP_MILLIS));
                } catch (InterruptedException e) {

                }
            }
        }

        private void moveToRandomStation() {
            List<Route> nextRoutes = RouteUtil.findReachableStations(trainset, trainset.getAllocatedStationTrack().get());
            if (nextRoutes.isEmpty()) {
                throw new RuntimeException(String.format("%s: no available route to other stations", trainset.getName()));
            }

            Route randomRoute = GeneralUtil.getRandom(nextRoutes);
            TrainUtil.moveTo(trainset, randomRoute);
            try {
                trainset.waitForCurrentMoveToFinish();
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while waiting for train to finish moving");
            }
        }
    }
}
