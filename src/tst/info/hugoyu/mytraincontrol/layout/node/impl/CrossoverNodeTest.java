package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.json.layout.CrossoverJson;
import info.hugoyu.mytraincontrol.json.layout.VectorJson;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.switchable.impl.Crossover;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class CrossoverNodeTest {

    private static final VectorJson UPLINK_STRAIGHT_VECTOR_JSON = VectorJson.builder().id0(152).id1(154).build();
    private static final Vector UPLINK_STRAIGHT_VECTOR = new Vector(UPLINK_STRAIGHT_VECTOR_JSON);

    private static final VectorJson DOWNLINK_STRAIGHT_VECTOR_JSON = VectorJson.builder().id0(149).id1(151).build();
    private static final Vector DOWNLINK_STRAIGHT_VECTOR = new Vector(DOWNLINK_STRAIGHT_VECTOR_JSON);

    private static final VectorJson UPLINK_CROSS_VECTOR_JSON = VectorJson.builder().id0(152).id1(149).build();
    private static final Vector UPLINK_CROSS_VECTOR = new Vector(UPLINK_CROSS_VECTOR_JSON);

    private static final VectorJson DOWNLINK_CROSS_VECTOR_JSON = VectorJson.builder().id0(154).id1(151).build();
    private static final Vector DOWNLINK_CROSS_VECTOR = new Vector(DOWNLINK_CROSS_VECTOR_JSON);

    private static final Range<Integer> RANGE = Range.closedOpen(0, 100);

    @Mock
    private Crossover crossover;

    @Mock
    private Trainset trainset1, trainset2;

    private CrossoverNode sut;

    @BeforeEach
    public void setUp() {
        initMocks(this);

        when(trainset1.getAddress()).thenReturn(1);
        when(trainset2.getAddress()).thenReturn(2);

        sut = new CrossoverNode(
                CrossoverJson.builder()
                        .length(310)
                        .crossLength(312)
                        .address(60)
                        .uplinkStraight(UPLINK_STRAIGHT_VECTOR_JSON)
                        .downlinkStraight(DOWNLINK_STRAIGHT_VECTOR_JSON)
                        .uplinkCross(UPLINK_CROSS_VECTOR_JSON)
                        .downlinkCross(DOWNLINK_CROSS_VECTOR_JSON)
                        .build(),
                crossover
        );
    }

    @Test
    public void testIsFree() {
        assertTrue(sut.isFree(trainset1, UPLINK_STRAIGHT_VECTOR, RANGE));
        assertTrue(sut.isFree(trainset1, DOWNLINK_STRAIGHT_VECTOR, RANGE));
        assertTrue(sut.isFree(trainset1, UPLINK_CROSS_VECTOR, RANGE));
        assertTrue(sut.isFree(trainset1, DOWNLINK_CROSS_VECTOR, RANGE));
    }

    @Test
    public void testIsFreeWithStraightConnectionOccupiedByTheSameTrainset() {
        sut.setOccupier(trainset1, UPLINK_STRAIGHT_VECTOR, RANGE);

        assertTrue(sut.isFree(trainset1, UPLINK_STRAIGHT_VECTOR, RANGE));
        assertTrue(sut.isFree(trainset1, DOWNLINK_STRAIGHT_VECTOR, RANGE));
        assertFalse(sut.isFree(trainset1, UPLINK_CROSS_VECTOR, RANGE));
        assertFalse(sut.isFree(trainset1, DOWNLINK_CROSS_VECTOR, RANGE));
    }

    @Test
    public void testIsFreeWithStraightConnectionOccupiedByDifferentTrainset() {
        sut.setOccupier(trainset1, UPLINK_STRAIGHT_VECTOR, RANGE);

        assertFalse(sut.isFree(trainset2, UPLINK_STRAIGHT_VECTOR, RANGE));
        assertTrue(sut.isFree(trainset2, DOWNLINK_STRAIGHT_VECTOR, RANGE));
        assertFalse(sut.isFree(trainset2, UPLINK_CROSS_VECTOR, RANGE));
        assertFalse(sut.isFree(trainset2, DOWNLINK_CROSS_VECTOR, RANGE));
    }

    @Test
    public void testIsFreeWithCrossConnectionOccupiedByTheSameTrainset() {
        sut.setOccupier(trainset1, UPLINK_CROSS_VECTOR, RANGE);

        assertFalse(sut.isFree(trainset1, UPLINK_STRAIGHT_VECTOR, RANGE));
        assertFalse(sut.isFree(trainset1, DOWNLINK_STRAIGHT_VECTOR, RANGE));
        assertTrue(sut.isFree(trainset1, UPLINK_CROSS_VECTOR, RANGE));
        assertFalse(sut.isFree(trainset1, DOWNLINK_CROSS_VECTOR, RANGE));
    }

    @Test
    public void testIsFreeWithCrossConnectionOccupiedByDifferentTrainset() {
        sut.setOccupier(trainset1, UPLINK_CROSS_VECTOR, RANGE);

        assertFalse(sut.isFree(trainset2, UPLINK_STRAIGHT_VECTOR, RANGE));
        assertFalse(sut.isFree(trainset2, DOWNLINK_STRAIGHT_VECTOR, RANGE));
        assertFalse(sut.isFree(trainset2, UPLINK_CROSS_VECTOR, RANGE));
        assertFalse(sut.isFree(trainset2, DOWNLINK_CROSS_VECTOR, RANGE));
    }

}