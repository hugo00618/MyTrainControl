package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.layout.Connection;
import info.hugoyu.mytraincontrol.layout.Vector;
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

public class RegularTrackNodeTest {

    @Mock
    Trainset train1, train2;

    private static final int ID0 = 151;
    private static final int ID1 = 153;
    private static final int LENGTH = 248;

    private RegularTrackNode sut;

    private Vector UPLINK_VECTOR = new Vector(ID0, ID1);
    private Vector DOWNLINK_VECTOR = new Vector(ID1, ID0);

    private Range<Integer> RANGE_FIRST_HALF = Range.closedOpen(0, LENGTH / 2);
    private Range<Integer> RANGE_SECOND_HALF = Range.closedOpen(LENGTH / 2, LENGTH);

    @BeforeEach
    public void setUp() {
        initMocks(this);

        when(train1.getAddress()).thenReturn(1);
        when(train2.getAddress()).thenReturn(2);

        sut = new RegularTrackNode(ID0, ID1, LENGTH, true, true);
    }

    @Test
    public void testGetConnections() {
        Set<Connection> connections = sut.getConnections();

        assertEquals(1, connections.size());
        Connection connection = connections.iterator().next();

        assertEquals(UPLINK_VECTOR, connection.getVector());
        assertEquals(LENGTH, connection.getDist());
        assertTrue(connection.isUplink());
        assertTrue(connection.isBidirectional());
    }

    @Test
    public void testIsFree() {
        assertTrue(sut.isFree(train1, UPLINK_VECTOR, RANGE_FIRST_HALF));
    }

    @Test
    public void testIsFreeWithRangeOccupiedBySameTrain() {
        sut.setOccupier(train1, UPLINK_VECTOR, RANGE_FIRST_HALF);

        assertTrue(sut.isFree(train1, UPLINK_VECTOR, RANGE_FIRST_HALF));
    }

    @Test
    public void testIsFreeWithRangeOccupiedByDifferentTrain() {
        sut.setOccupier(train1, UPLINK_VECTOR, RANGE_FIRST_HALF);

        assertFalse(sut.isFree(train2, UPLINK_VECTOR, RANGE_FIRST_HALF));
    }

    @Test
    public void testIsFreeWithRangeOccupiedByDifferentTrainNonOverlappingRange() {
        sut.setOccupier(train1, UPLINK_VECTOR, RANGE_FIRST_HALF);

        assertTrue(sut.isFree(train2, UPLINK_VECTOR, RANGE_SECOND_HALF));
    }

    @Test
    public void testIsFreeWithRangeOccupiedByDifferentVector() {
        sut.setOccupier(train1, UPLINK_VECTOR, RANGE_FIRST_HALF);

        assertFalse(sut.isFree(train1, DOWNLINK_VECTOR, RANGE_FIRST_HALF));
    }

    @Test
    public void testGetOccupiedRangeEmpty() {
        assertTrue(sut.getOccupiedRange(UPLINK_VECTOR, train1).isEmpty());
    }

    @Test
    public void testGetOccupiedRange() {
        sut.setOccupier(train1, UPLINK_VECTOR, RANGE_FIRST_HALF);

        assertEquals(RANGE_FIRST_HALF, sut.getOccupiedRange(UPLINK_VECTOR, train1).get());
    }

    @Test
    public void testGetOccupiedRangeUnoccupiedTrainset() {
        sut.setOccupier(train1, UPLINK_VECTOR, RANGE_FIRST_HALF);

        assertTrue(sut.getOccupiedRange(UPLINK_VECTOR, train2).isEmpty());
    }

    @Test
    public void testGetOccupiedRangeUnoccupiedVector() {
        sut.setOccupier(train1, UPLINK_VECTOR, RANGE_FIRST_HALF);

        assertTrue(sut.getOccupiedRange(DOWNLINK_VECTOR, train1).isEmpty());
    }

}