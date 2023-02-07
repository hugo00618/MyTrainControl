package info.hugoyu.mytraincontrol.layout.node;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.trainset.Trainset;

import java.util.Optional;
import java.util.concurrent.Future;

public interface Allocatable {

    /**
     * @return occupier lock object
     */
    Object getOccupierLock();

    /**
     * @param vector
     * @return whether the given range in vector has no occupiers.
     * If the range can only be occupied by a single trainset, return whether 'trainset' is the current occupier
     */
    boolean isFree(Trainset trainset, Vector vector, Range<Integer> range);

    /**
     * sets trainset to be the occupier of vector, with range
     *
     * @param trainset
     * @param vector
     * @param range
     */
    void setOccupier(Trainset trainset, Vector vector, Range<Integer> range);

    /**
     * makes hardware change if needed (e.g. change switch state)
     */
    Future<Void> updateHardware();

    /**
     * @param vector
     * @return section length of the requesting vector
     */
    int getSectionLength(Vector vector);

    /**
     * @param vector
     * @return current owned range of vector, or null if trainset doesn't own the vector
     */
    Optional<Range<Integer>> getOccupiedRange(Vector vector, Trainset trainset);

    /**
     * sets the occupier of vector to be trainset, with newOccupiedRange
     *
     * @param vector
     * @param trainset
     * @param newOccupiedRange
     */
    void setOccupiedRange(Vector vector, Trainset trainset, Range<Integer> newOccupiedRange);

    /**
     * remove trainset from the occupiers list
     *
     * @param vector
     * @param trainset
     */
    void removeOccupier(Vector vector, Trainset trainset);

    /**
     * Frees the entire section that trainset owns
     *
     * @param trainset
     */
    void freeAll(Trainset trainset);

    /**
     * @return whether the section allows bidirectional operation
     */
    boolean isBidirectional();

}
