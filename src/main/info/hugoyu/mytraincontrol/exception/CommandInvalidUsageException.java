package info.hugoyu.mytraincontrol.exception;

import info.hugoyu.mytraincontrol.command.Command;

public class CommandInvalidUsageException extends Exception {

    public CommandInvalidUsageException(Command cmd) {
        super(cmd.argList());
    }

}
