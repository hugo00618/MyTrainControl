package info.hugoyu.mytraincontrol.layout;

import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.trainset.Trainset;

public interface Allocatable {
    /**
     * Allocates a section
     *
     * @param trainset       trainset
     * @param dist           distance
     * @param nextNodeId     next node
     * @param previousNodeId previous node
     * @return remaining remaining distance to allocate
     */
    BlockSectionResult alloc(Trainset trainset, int dist, Long nextNodeId, Long previousNodeId) throws NodeAllocationException;

    /**
     * Frees the section [previousOwnedSectionFrom, previousOwnedSectionFrom + distance)
     *
     * @param trainset
     * @param dist
     * @return remaining distance to free
     */
    BlockSectionResult free(Trainset trainset, int dist) throws NodeAllocationException;

    /**
     * Frees the entire section that trainset owns
     * @param trainset
     */
    void freeAll(Trainset trainset) throws NodeAllocationException;
}
