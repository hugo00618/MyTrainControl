package info.hugoyu.mytraincontrol.trainset;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class TrainsetProfile {

    private int controlCarLength, passengerCarLength;
    private int numControlCars, numPassengerCars;
    private double accRate, decRate;
    private Map<Integer, Double> throttleSpeedMap;

    private List<Integer> throttleList;

    public List<Integer> getThrottleList() {
        if (throttleList == null) {
            throttleList = new ArrayList<>(throttleSpeedMap.keySet());
            Collections.sort(throttleList);
        }
        return throttleList;
    }
}
