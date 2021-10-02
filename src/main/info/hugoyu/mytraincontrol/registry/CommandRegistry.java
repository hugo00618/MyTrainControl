package info.hugoyu.mytraincontrol.registry;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.command.debug.impl.MoveDistCommand;
import info.hugoyu.mytraincontrol.command.debug.impl.SetThrottleCommand;
import info.hugoyu.mytraincontrol.command.impl.AllocateCommand;
import info.hugoyu.mytraincontrol.command.impl.EmergencyKillCommand;
import info.hugoyu.mytraincontrol.command.impl.LightControlCommand;
import info.hugoyu.mytraincontrol.command.impl.MoveCommand;
import info.hugoyu.mytraincontrol.command.impl.PowerControlCommand;
import info.hugoyu.mytraincontrol.command.impl.PrintCommand;
import info.hugoyu.mytraincontrol.command.impl.RegisterCommand;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {

    private CommandRegistry() {

    }

    @Getter
    private static Map<String, Command> commands;

    static {
        commands = new HashMap<>();

        commands.put("help", new Command() {
            @Override
            public void execute(String[] args) throws Exception {
                System.out.println("List of commands: ");
                for (String command : commands.keySet()) {
                    System.out.println(command);
                }
            }

            @Override
            public String[] expectedArgs() {
                return new String[0];
            }

        });

        commands.put("alloc", new AllocateCommand());
        commands.put("e", new EmergencyKillCommand());
        commands.put("light", new LightControlCommand());
        commands.put("mv", new MoveCommand());
        commands.put("print", new PrintCommand());
        commands.put("pwr", new PowerControlCommand());
        commands.put("reg", new RegisterCommand());

        // debug
        commands.put("st", new SetThrottleCommand());
        commands.put("mvdist", new MoveDistCommand());
    }

}
