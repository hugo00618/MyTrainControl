package info.hugoyu.mytraincontrol.exception;

import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

@Getter
@Log4j
public class NodeAllocationException extends Exception {

    public static enum ExceptionType {
        // alloc
        ALLOCATING_OCCUPIED_SECTION,

        // free
        FREEING_UNOWNED_SECTION,
    }

    private ExceptionType exceptionType;

    public NodeAllocationException(ExceptionType exceptionType, Trainset trainset, Vector vector, int dist) {
        super(String.format("%s while %s alloc/freeing node %s for distance %d",
                exceptionType, trainset.getName(), vector.toString(), dist));

        log.error(String.format("%s: exception %s while alloc/freeing node %s for distance %d",
                trainset.getName(), exceptionType, vector.toString(), dist));

        this.exceptionType = exceptionType;
    }
}
