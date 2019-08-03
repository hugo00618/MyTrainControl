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

    static long lastTrackedSliderMovementTime = System.currentTimeMillis();
    static final long trackSliderMinInterval = 200;

    private static ThrottleControlThread instance;

    static class ThrottleControlTask {
        Throttle dccThrottle;
        float throttle;

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
            System.out.println("wake");
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
                            if (System.currentTimeMillis() - lastTrackedSliderMovementTime >= trackSliderMinInterval) {
                                int taskAddr = taskAddrs.poll();
                                if (taskMap.containsKey(taskAddr)) {
                                    ThrottleControlTask task = taskMap.get(taskAddr);
                                    taskMap.remove(taskAddr);
                                    System.out.println("throttle set: " + task.throttle);

                                    task.dccThrottle.setSpeedSetting(task.throttle);

                                    lastTrackedSliderMovementTime = System.currentTimeMillis();
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
