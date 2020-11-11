package info.hugoyu.mytraincontrol.commandstation;

public class CommandStationRunnable implements Runnable {

    private static CommandStationRunnable instance;

    private static final long MIN_UPDATE_INTERVAL = 20;

    private static long lastUpdated = 0;
    private static CommandStation commandStation;

    private CommandStationRunnable() {
        commandStation = CommandStation.getInstance();
    }

    public static CommandStationRunnable getInstance() {
        if (instance == null) {
            instance = new CommandStationRunnable();
            new Thread(instance).start();
        }

        return instance;
    }

    @Override
    public void run() {
        while (true) {

            if (System.currentTimeMillis() - lastUpdated >= MIN_UPDATE_INTERVAL) {
                if (commandStation.hasTasks()) {
                    commandStation.getTask().execute();
                    lastUpdated = System.currentTimeMillis();
                }
            } else {
                try {
                    long sleepTime = lastUpdated + MIN_UPDATE_INTERVAL - System.currentTimeMillis();
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }

                } catch (InterruptedException e) {

                }
            }
        }
    }
}
