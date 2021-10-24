package info.hugoyu.mytraincontrol.command;

public interface Command {
    /**
     *
     * @param args
     * @return true if args are valid, false otherwise
     */
    boolean execute(String[] args);

    String[] expectedArgs();
}
