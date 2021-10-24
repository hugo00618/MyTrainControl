package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.exception.InvalidIdException;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class MoveCommand implements Command {

    @Override
    public boolean execute(String[] args) {
        int address;
        try {
            address = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        String stationId = args[2];
        try {
            TrainUtil.moveTo(address, stationId);
        } catch (InvalidIdException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "stationId"};
    }

}
