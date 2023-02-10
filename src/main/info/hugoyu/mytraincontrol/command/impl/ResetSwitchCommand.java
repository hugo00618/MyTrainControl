package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.SwitchUtil;

public class ResetSwitchCommand implements Command {
    @Override
    public void execute(String[] args) {
        SwitchUtil.invalidateCachedState();
    }

    @Override
    public String[] expectedArgs() {
        return new String[0];
    }
}
