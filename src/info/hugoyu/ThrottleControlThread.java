package info.hugoyu;

import jmri.Throttle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ThrottleControlThread extends Thread {
    private static Map<Integer, ThrottleControlTask> taskMap = new HashMap<>();
    private static Queue<Integer> taskAddrs = new LinkedList<>();
    private static Object tasksLock = new Object();

    private static boolean stop = false;

    static long lastUpdatedTime = System.currentTimeMillis();
    static final long MIN_UPDATE_INTERVAL = 20;

    private static ThrottleControlThread instance;

    static class ThrottleControlTask {
        Loco loco;
        Throttle dccThrottle;
        float throttle;
        double speed;

        public ThrottleControlTask(Loco loco, Throttle dccThrottle, float throttle, double speed) {
            this.loco = loco;
            this.dccThrottle = dccThrottle;
            this.throttle = throttle;
            this.speed = speed;
        }

        /**
         * throttle overwrite task
         * @param dccThrottle
         * @param throttle
         */
        public ThrottleControlTask(Throttle dccThrottle, float throttle) {
            this.dccThrottle = dccThrottle;
            this.throttle = throttle;
        }
    }

    private ThrottleControlThread(Runnable runnable) {
        super(runnable);
    }

    public void addTask(int addr, ThrottleControlTask task) {
        synchronized (tasksLock) {
            taskAddrs.add(addr);
            taskMap.put(addr, task);
            tasksLock.notify();
        }
    }

    private static void initInstance() {
        instance = new ThrottleControlThread(new Runnable() {
            @Override
            public void run() {
                synchronized (tasksLock) {
                    while (!stop) {
                        while (!taskAddrs.isEmpty()) {
                            if (System.currentTimeMillis() - lastUpdatedTime >= MIN_UPDATE_INTERVAL) {
                                int taskAddr = taskAddrs.poll();
                                if (taskMap.containsKey(taskAddr)) {
                                    ThrottleControlTask task = taskMap.get(taskAddr);
                                    taskMap.remove(taskAddr);

                                    task.dccThrottle.setSpeedSetting(task.throttle);
                                    lastUpdatedTime = System.currentTimeMillis();
                                    if (task.loco != null)
                                        task.loco.addRealSpeedUpdate(new Loco.SpeedUpdate(lastUpdatedTime, task.speed));
                                }
                            }
                        }

                        try {
                            tasksLock.wait();
                        } catch (InterruptedException e) {
                            // swallow
                        }
                    }
                }
            }
        });

        instance.start();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        synchronized (tasksLock) {
            stop = true;
            tasksLock.notify();
        }
    }

    public static ThrottleControlThread getInstance() {
        if (instance == null) initInstance();
        return instance;
    }

}
