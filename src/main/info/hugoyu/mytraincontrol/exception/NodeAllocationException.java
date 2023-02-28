package info.hugoyu.mytraincontrol.exception;

import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Getter
@Log4j2
public class NodeAllocationException extends Exception {

    public enum ExceptionType {
        // alloc
        ALLOCATING_OCCUPIED_SECTION,

        // free
        FREEING_UNOWNED_SECTION,
    }

    private final ExceptionType exceptionType;

    private final String trainsetName;
    private final Vector vector;
    private final int dist;

    public NodeAllocationException(ExceptionType exceptionType, Trainset trainset, Vector vector, int dist) {
        super();

        this.exceptionType = exceptionType;
        this.trainsetName = trainset.getName();
        this.vector = vector;
        this.dist = dist;
    }
}
