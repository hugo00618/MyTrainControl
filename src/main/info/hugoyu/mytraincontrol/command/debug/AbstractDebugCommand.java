package info.hugoyu.mytraincontrol.command.debug;

import info.hugoyu.mytraincontrol.command.Command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class AbstractDebugCommand implements Command {

    @Override
    public final boolean execute(String[] args) {
        System.out.println("Warning: This is a debugging command which violates block section and may result in vehicle collision.");
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
            return true;
        }


        return executeCommand(args);
    }

    public abstract boolean executeCommand(String[] args);

}
