package info.hugoyu.mytraincontrol.command;

public interface Command {

    void execute(String[] args);

    String[] expectedArgs();
}
