package info.hugoyu.mytraincontrol.commands;

import info.hugoyu.mytraincontrol.util.TrainUtil;
import info.hugoyu.mytraincontrol.exceptions.CommandInvalidUsageException;

public class RegisterCommand implements ICommand {

    @Override
    public void execute(String[] args) throws CommandInvalidUsageException {
        try {
            int address = Integer.parseInt(args[1]);
            TrainUtil.registerTrainset(address, args[2], args[3]);
        } catch (NumberFormatException e) {
            throw new CommandInvalidUsageException(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String help() {
        return "r {address} {name} {configFileName}";
    }

    @Override
    public int numberOfArgs() {
        return 4;
    }
}
