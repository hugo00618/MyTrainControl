package info.hugoyu.mytraincontrol.command;

public interface ICommand {
    void execute(String[] args) throws Exception;

    String argList();

    int numberOfArgs();
}
