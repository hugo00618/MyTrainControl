package info.hugoyu.mytraincontrol.util;

import info.hugoyu.mytraincontrol.command.ICommand;
import info.hugoyu.mytraincontrol.command.impl.AllocateCommand;
import info.hugoyu.mytraincontrol.command.impl.LightControlCommand;
import info.hugoyu.mytraincontrol.command.impl.ListCommand;
import info.hugoyu.mytraincontrol.command.impl.MoveCommand;
import info.hugoyu.mytraincontrol.command.impl.MoveDistCommand;
import info.hugoyu.mytraincontrol.command.impl.PowerControlCommand;
import info.hugoyu.mytraincontrol.command.impl.RegisterCommand;
import info.hugoyu.mytraincontrol.command.impl.SetThrottleCommand;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.exception.CommandNotFoundException;

import java.util.HashMap;
import java.util.Map;

public class CommandProvider {

    private static Map<String, ICommand> commands;

    static {
        commands = new HashMap<>();

        commands.put("help", new ICommand() {
            @Override
            public void execute(String[] args) throws Exception {
                System.out.println("List of commands: ");
                for (String command : commands.keySet()) {
                    System.out.println(command);
                }
            }

            @Override
            public String argList() {
                return "";
            }

            @Override
            public int numberOfArgs() {
                return 1;
            }
        });

        commands.put("alloc", new AllocateCommand());
        commands.put("light", new LightControlCommand());
        commands.put("list", new ListCommand());
        commands.put("mv", new MoveCommand());
        commands.put("pwr", new PowerControlCommand());
        commands.put("reg", new RegisterCommand());

        // debug
        commands.put("st", new SetThrottleCommand());
        commands.put("mvdist", new MoveDistCommand());
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
