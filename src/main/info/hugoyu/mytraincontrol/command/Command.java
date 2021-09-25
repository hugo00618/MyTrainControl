package info.hugoyu.mytraincontrol.command;

public interface Command {
    void execute(String[] args) throws Exception;
    String argList();
    int numberOfArgs();
}
