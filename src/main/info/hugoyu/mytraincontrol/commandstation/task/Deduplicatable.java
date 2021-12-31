package info.hugoyu.mytraincontrol.commandstation.task;

public interface Deduplicatable {
    boolean isDuplicate(Deduplicatable task);
    void dedupe(Deduplicatable task);
}
