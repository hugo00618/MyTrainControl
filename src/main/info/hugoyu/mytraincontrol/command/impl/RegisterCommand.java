package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.TrainUtil;
import lombok.extern.log4j.Log4j;

@Log4j
public class RegisterCommand implements Command {

    @Override
    public boolean execute(String[] args) {
        int address;
        try {
            address = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        String name = args[2];
        String profileFileName = args[3];

        TrainUtil.registerTrainset(address, name, profileFileName);
        TrainUtil.setLight(address, true);
        System.out.println(String.format("Trainset registered: %s at address %s", name, address));

        return true;
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address", "name", "profileFileName"};
    }
}
