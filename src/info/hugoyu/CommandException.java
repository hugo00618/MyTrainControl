package info.hugoyu;

public class CommandException extends Exception {

    public static final String COMMAND_F = "f addr [speed (0.0 - 1.0)]";
    public static final String COMMAND_S = "s addr";

    public CommandException(String msg) {
        super(msg);
    }
}
