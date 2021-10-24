package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.TrainUtil;

public class FreeCommand implements Command {

    @Override
    public boolean execute(String[] args) {
        int address;
        try {
            address = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        TrainUtil.freeAllAllocatedNodes(address);
        return true;
    }

    @Override
    public String[] expectedArgs() {
        return new String[]{"address"};
    }
}
