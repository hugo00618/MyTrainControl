package info.hugoyu.mytraincontrol.exception;

import info.hugoyu.mytraincontrol.command.ICommand;

public class CommandInvalidUsageException extends Exception {

    public CommandInvalidUsageException(ICommand cmd) {
        super(cmd.argList());
    }

}
