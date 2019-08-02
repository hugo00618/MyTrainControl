package info.hugoyu;

public class CommandException extends Exception {

    public static final String COMMAND_MS = "ms addr [speed (0 - 128)]";
    public static final String COMMAND_MV = "mv addr [dist] {[max speed (0.0 - 1.0)]}";
    public static final String COMMAND_S = "s addr";
    public static final String COMMAND_SS = "ss addr [speed (0.0 - 1.0)]";
    public static final String COMMAND_SSH = "ssh addr [speed (0.0 - 1.0)]";

    public CommandException(String msg) {
        super(msg);
    }
}
