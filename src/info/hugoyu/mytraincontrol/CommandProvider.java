package info.hugoyu.mytraincontrol;

import info.hugoyu.mytraincontrol.commands.ICommand;
import info.hugoyu.mytraincontrol.commands.RegisterCommand;
import info.hugoyu.mytraincontrol.commands.SetSpeedCommand;
import info.hugoyu.mytraincontrol.commands.StopCommand;
import info.hugoyu.mytraincontrol.exceptions.CommandNotFoundException;
import info.hugoyu.mytraincontrol.exceptions.CommandInvalidUsageException;

import java.util.HashMap;
import java.util.Map;

public class CommandProvider {

    private static Map<String, ICommand> commands;

    static {
        commands = new HashMap<>();

        commands.put("r", new RegisterCommand());
        commands.put("s", new StopCommand());
        commands.put("ss", new SetSpeedCommand());
    }

    public static void runCommand(String[] args) throws Exception {
        String commandKey = args[0];
        ICommand command = commands.get(commandKey);

        if (command == null) {
            throw new CommandNotFoundException(commandKey);
        }
        if (command.numberOfArgs() != args.length) {
            throw new CommandInvalidUsageException(command);
        }

        command.execute(args);
    }

}
