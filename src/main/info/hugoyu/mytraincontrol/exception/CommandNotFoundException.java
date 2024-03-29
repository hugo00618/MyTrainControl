package info.hugoyu.mytraincontrol.exception;

public class CommandNotFoundException extends RuntimeException {

    private static final String ERR_MSG = "ERROR: command not found: ";

    public CommandNotFoundException(String command) {
        super(ERR_MSG + command);
    }
}
