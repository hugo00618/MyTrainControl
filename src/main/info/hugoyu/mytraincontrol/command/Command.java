package info.hugoyu.mytraincontrol.command;

public interface Command {
    /**
     * parse command arguments
     * @param args
     * @return true if args are valid, false otherwise
     */
    boolean parseArgs(String[] args);

    void execute();

    String[] expectedArgs();
}
