package info.hugoyu.mytraincontrol.exception;

import info.hugoyu.mytraincontrol.command.Command;

import java.util.Arrays;

public class CommandInvalidUsageException extends RuntimeException {

    public CommandInvalidUsageException(Command cmd) {
        super(Arrays.toString(cmd.expectedArgs()));
    }

}
