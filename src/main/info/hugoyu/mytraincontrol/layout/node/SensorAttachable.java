package info.hugoyu.mytraincontrol.layout.node;

import info.hugoyu.mytraincontrol.layout.Position;
import info.hugoyu.mytraincontrol.trainset.Trainset;

public interface SensorAttachable {
    Trainset getOccupier(Position sensorPosition);
}
