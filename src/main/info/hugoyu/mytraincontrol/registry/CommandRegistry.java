package info.hugoyu.mytraincontrol.registry;

import info.hugoyu.mytraincontrol.command.Command;
import info.hugoyu.mytraincontrol.command.debug.impl.CrossoverControlCommand;
import info.hugoyu.mytraincontrol.command.debug.impl.MoveDistCommand;
import info.hugoyu.mytraincontrol.command.debug.impl.SetThrottleCommand;
import info.hugoyu.mytraincontrol.command.debug.impl.SpeedProfilingCommand;
import info.hugoyu.mytraincontrol.command.debug.impl.TurnoutControlCommand;
import info.hugoyu.mytraincontrol.command.impl.AllocateCommand;
import info.hugoyu.mytraincontrol.command.impl.AutomaticTrainOperationCommand;
import info.hugoyu.mytraincontrol.command.impl.EmergencyKillCommand;
import info.hugoyu.mytraincontrol.command.impl.FreeCommand;
import info.hugoyu.mytraincontrol.command.impl.LightControlCommand;
import info.hugoyu.mytraincontrol.command.impl.MoveCommand;
import info.hugoyu.mytraincontrol.command.impl.PowerControlCommand;
import info.hugoyu.mytraincontrol.command.impl.PrintCommand;
import info.hugoyu.mytraincontrol.command.impl.PrintNodeCommand;
import info.hugoyu.mytraincontrol.command.impl.RegisterCommand;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {

    public static final String COMMAND_MOVE = "mv";

    private CommandRegistry() {

    }

    @Getter
    private static Map<String, Command> commands;

    static {
        commands = new HashMap<>();

        commands.put("help", new Command() {
            @Override
            public void execute(String[] args) {
                System.out.println("List of commands: ");
                commands.keySet()
                        .forEach(System.out::println);
            }

            @Override
            public String[] expectedArgs() {
                return new String[0];
            }

        });

        EmergencyKillCommand emergencyKillCommand = new EmergencyKillCommand();

        commands.put("alloc", new AllocateCommand());
        commands.put("ato", new AutomaticTrainOperationCommand());

        commands.put("e", emergencyKillCommand);
        commands.put("kill", emergencyKillCommand);

        commands.put("free", new FreeCommand());
        commands.put("light", new LightControlCommand());
        commands.put(COMMAND_MOVE, new MoveCommand());
        commands.put("print", new PrintCommand());
        commands.put("printnode", new PrintNodeCommand());
        commands.put("pwr", new PowerControlCommand());
        commands.put("reg", new RegisterCommand());

        // debug
        commands.put("mvdist", new MoveDistCommand());
        commands.put("throttle", new SetThrottleCommand());
        commands.put("crossover", new CrossoverControlCommand());
        commands.put("turnout", new TurnoutControlCommand());
        commands.put("profile", new SpeedProfilingCommand());
    }

}
