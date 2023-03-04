package info.hugoyu.mytraincontrol.layout.node;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.trainset.Trainset;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public interface Allocatable {

    /**
     * @return occupier lock object
     */
    Lock getOccupierLock();

    /**
     *
     */
    Condition getOccupierChangeCondition();

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
     * @param trainset
     * @return current owned range of vector, or null if trainset doesn't own the vector
     */
    Optional<Range<Integer>> getOccupiedRange(Vector vector, Trainset trainset);

    /**
     *
     * @param vector
     * @param trainset
     * @return non-block version of getOccupiedRange
     */
    Optional<Range<Integer>> getOccupiedRangeImmediately(Vector vector, Trainset trainset);

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
