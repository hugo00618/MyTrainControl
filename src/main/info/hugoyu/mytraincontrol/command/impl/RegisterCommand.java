package info.hugoyu.mytraincontrol.command.impl;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.util.TrainUtil;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import lombok.extern.log4j.Log4j;

@Log4j
public class RegisterCommand implements Command {

    @Override
    public void execute(String[] args) throws CommandInvalidUsageException {
        try {
            int address = Integer.parseInt(args[1]);
            TrainUtil.registerTrainset(address, args[2], args[3]);
            TrainUtil.setLight(address, true);
            System.out.println("Trainset registered: " + args[2] + " at address " + address);
        } catch (NumberFormatException e) {
            throw new CommandInvalidUsageException(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String argList() {
        return "{address} {name} {configFileName}";
    }

    @Override
    public int numberOfArgs() {
        return 4;
    }
}
