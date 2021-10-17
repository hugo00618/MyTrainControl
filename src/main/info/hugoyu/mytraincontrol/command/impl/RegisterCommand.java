package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.TrainUtil;
import lombok.extern.log4j.Log4j;

@Log4j
public class RegisterCommand implements Command {

    private int address;
    private String name, profileFileName;

    @Override
    public boolean parseArgs(String[] args) {
        try {
            address = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        name = args[2];
        profileFileName = args[3];
        return true;
    }

    @Override
    public void execute() {
        TrainUtil.registerTrainset(address, name, profileFileName);
        TrainUtil.setLight(address, true);
        System.out.println(String.format("Trainset registered: %s at address %s", name, address));

    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "name", "configFileName"};
    }
}
