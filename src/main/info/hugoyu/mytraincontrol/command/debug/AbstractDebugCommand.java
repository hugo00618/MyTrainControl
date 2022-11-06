package info.hugoyu.mytraincontrol.command.debug;

import info.hugoyu.mytraincontrol.command.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDebugCommand implements Command {

    public interface EventListener {
        void onCommandExecuted(AbstractDebugCommand command);
    }

    private static List<EventListener> eventListeners = new ArrayList<>();

    @Override
    public final void execute(String[] args) {
        System.out.println("Warning: This is a debugging command which violates block section and may result in vehicle collision.");
        System.out.println("All active ATO operation will be canceled.");
        System.out.println("Do you want to proceed? (yes/no)");

        String line = "";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            line = br.readLine().toLowerCase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!line.equals("yes")) {
            System.out.println("Command aborted");
            return;
        }

        executeCommand(args);

        eventListeners.forEach(eventListener -> eventListener.onCommandExecuted(this));
    }

    public static void subscribe(EventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public abstract void executeCommand(String[] args);

}
