package info.hugoyu.mytraincontrol.commands;

public interface ICommand {
    void execute(String[] args) throws Exception;

    String help();

    int numberOfArgs();
}
