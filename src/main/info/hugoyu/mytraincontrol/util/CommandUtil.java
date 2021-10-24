package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.exception.CommandNotFoundException;
import info.hugoyu.mytraincontrol.registry.CommandRegistry;

public class CommandUtil {

    public static void runCommand(String[] args) throws Exception {
        String commandKey = args[0];
        Command command = CommandRegistry.getCommands().get(commandKey);

        if (command == null) {
            throw new CommandNotFoundException(commandKey);
        }
        if (command.expectedArgs().length != args.length - 1 || // args size validation fails
                !command.execute(args) // args parsing fails
        ) {
            throw new CommandInvalidUsageException(command);
        }

    }
}
