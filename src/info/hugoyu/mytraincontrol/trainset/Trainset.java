package info.hugoyu.mytraincontrol.trainset;

import info.hugoyu.mytraincontrol.commandstation.CommandStation;
import info.hugoyu.mytraincontrol.commandstation.SetSpeedTask;
import lombok.Getter;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class Trainset implements Runnable {
    private int address;
    private String name;

    private TrainsetProfile profile;

    double cSpeed, tSpeed; // current, target

    public Trainset(int address, String name, String profileFilename) throws FileNotFoundException {
        this.address = address;
        this.name = name;

        profile = TrainsetProfileParser.parseJSON(profileFilename);

        cSpeed = 0;
        tSpeed = 0;
    }

    public void setSpeed(double speed) {
        Map<Integer, Double> throttleSpeedMap = profile.getThrottleSpeedMap();
        List<Integer> throttleList = profile.getThrottleList();
        double minSpeed = throttleSpeedMap.get(throttleList.get(0));
        double maxSpeed = throttleSpeedMap.get(throttleList.get(throttleList.size() - 1));
        speed = Math.max(speed, minSpeed);
        speed = Math.min(speed, maxSpeed);

        tSpeed = speed;
        sendSetSpeedTask();
    }

    private void sendSetSpeedTask() {
        if (cSpeed != tSpeed) {
            CommandStation.getInstance().addTask(new SetSpeedTask(this));
        }
    }

    public void onSetSpeedTaskCompleted() {
        sendSetSpeedTask();
    }

    @Override
    public void run() {

    }
}
