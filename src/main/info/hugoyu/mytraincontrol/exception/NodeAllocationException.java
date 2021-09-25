package info.hugoyu.mytraincontrol.exception;

import info.hugoyu.mytraincontrol.layout.node.track.AbstractTrackNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import lombok.Getter;
import lombok.extern.log4j.Log4j;

@Getter
@Log4j
public class NodeAllocationException extends Exception {

    public static enum ExceptionType {
        // alloc

        // free
        FREEING_UNOWNED_SECTION,
    }

    private ExceptionType exceptionType;

    public NodeAllocationException(ExceptionType exceptionType, Trainset trainset, AbstractTrackNode node, int dist) {
        super(String.format("%s while %s alloc/freeing node %s for distance %d",
                exceptionType, trainset.getName(), node.getId(), dist));

        log.error(String.format("%s: exception %s while alloc/freeing node %s for distance %d",
                trainset.getName(), exceptionType, node.getId(), dist));

        this.exceptionType = exceptionType;
    }
}
