package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.exception.CommandNotFoundException;
import info.hugoyu.mytraincontrol.registry.CommandRegistry;

public class CommandUtil {

    public static void runCommand(String[] args) {
        String commandKey = args[0];
        Command command = CommandRegistry.getCommands().get(commandKey);

        if (command == null) {
            throw new CommandNotFoundException(commandKey);
        }

        try {
            command.execute(args);
        } catch (Exception e) {
            throw new CommandInvalidUsageException(command, e);
        }
    }
}
