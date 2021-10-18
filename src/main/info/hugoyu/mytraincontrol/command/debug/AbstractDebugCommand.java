package info.hugoyu.mytraincontrol.command.debug;

import info.hugoyu.mytraincontrol.command.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class AbstractDebugCommand implements Command {

    @Override
    public final void execute() {
        System.out.println("Warning: This is a debugging command which violates block section and may result in vehicle collision.");
        System.out.println("Do you want to proceed? (yes/no)");

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String line = br.readLine();
            if (!line.equals("yes")) {
                System.out.println("Command aborted");
                return;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        executeCommand();
    }

    public abstract void executeCommand();

}
