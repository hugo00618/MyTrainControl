package info.hugoyu.mytraincontrol.exception;

import info.hugoyu.mytraincontrol.command.Command;

import java.util.Arrays;

public class CommandInvalidUsageException extends Exception {

    public CommandInvalidUsageException(Command cmd) {
        super(Arrays.toString(cmd.expectedArgs()));
    }

}
