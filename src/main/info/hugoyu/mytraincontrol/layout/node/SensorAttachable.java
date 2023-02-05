package info.hugoyu.mytraincontrol.layout.node;

import info.hugoyu.mytraincontrol.trainset.Trainset;

public interface SensorAttachable {
    Trainset getOccupier(int position);
}
