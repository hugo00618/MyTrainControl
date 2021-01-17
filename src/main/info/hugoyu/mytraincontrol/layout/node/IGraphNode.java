package info.hugoyu.mytraincontrol.layout.node;

import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.trainset.Trainset;

public interface IGraphNode {
    /**
     * Allocates a section:
     * allocates section [id, id + distance) if not owning any current section, or
     * allocates section [previousOwnedSectionFrom, previousOwnedSectionTo + distance)
     * if [previousOwnedSectionFrom, previousOwnedSectionTo) is previously owned
     *
     * @param trainset   trainset
     * @param dist       distance
     * @param nextNodeId next node, nullable
     * @return remaining remaining distance to allocate
     */
    BlockSectionResult alloc(Trainset trainset, int dist, String nextNodeId) throws NodeAllocationException;

    /**
     * Frees the section [previousOwnedSectionFrom, previousOwnedSectionFrom + distance)
     *
     * @param trainset
     * @param dist
     * @return remaining distance to free
     */
    BlockSectionResult free(Trainset trainset, int dist) throws NodeAllocationException;
}
