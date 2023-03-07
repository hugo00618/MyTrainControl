package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.json.layout.CrossoverJson;
import info.hugoyu.mytraincontrol.json.layout.VectorJson;
import info.hugoyu.mytraincontrol.layout.Connection;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.switchable.impl.Crossover;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static final int SECTION_LENGTH_STRAIGHT = 310;
    private static final int SECTION_LENGTH_CROSS = 312;

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
                        .length(SECTION_LENGTH_STRAIGHT)
                        .crossLength(SECTION_LENGTH_CROSS)
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

    @Test
    public void testGetSectionLengthStraight() {
        assertEquals(SECTION_LENGTH_STRAIGHT, sut.getSectionLength(UPLINK_STRAIGHT_VECTOR));
        assertEquals(SECTION_LENGTH_STRAIGHT, sut.getSectionLength(DOWNLINK_STRAIGHT_VECTOR));
    }

    @Test
    public void testGetSectionLengthCross() {
        assertEquals(SECTION_LENGTH_CROSS, sut.getSectionLength(UPLINK_CROSS_VECTOR));
        assertEquals(SECTION_LENGTH_CROSS, sut.getSectionLength(DOWNLINK_CROSS_VECTOR));
    }

    @Test
    public void testGetOccupiedRange() {
        sut.setOccupier(trainset1, UPLINK_CROSS_VECTOR, RANGE);

        assertEquals(RANGE, sut.getOccupiedRange(UPLINK_CROSS_VECTOR, trainset1).get());
    }

    @Test
    public void testGetOccupiedRangeUnoccupiedTrainset() {
        sut.setOccupier(trainset1, UPLINK_CROSS_VECTOR, RANGE);

        assertTrue(sut.getOccupiedRange(UPLINK_CROSS_VECTOR, trainset2).isEmpty());
    }

    @Test
    public void testGetOccupiedRangeUnoccupiedVector() {
        sut.setOccupier(trainset1, UPLINK_CROSS_VECTOR, RANGE);

        assertTrue(sut.getOccupiedRange(DOWNLINK_CROSS_VECTOR, trainset1).isEmpty());
    }

    @Test
    public void testGetConnections() {
        Set<Connection> connections = sut.getConnections();

        assertEquals(4, connections.size());
    }

}