package info.hugoyu.mytraincontrol.exceptions;

import info.hugoyu.mytraincontrol.commands.ICommand;

public class CommandInvalidUsageException extends Exception {

    public CommandInvalidUsageException(ICommand cmd) {
        super(cmd.help());
    }

}
