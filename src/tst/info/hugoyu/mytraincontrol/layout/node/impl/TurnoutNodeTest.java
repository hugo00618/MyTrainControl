package info.hugoyu.mytraincontrol.layout.node.impl;

import com.google.common.collect.Range;
import info.hugoyu.mytraincontrol.layout.Connection;
import info.hugoyu.mytraincontrol.layout.Vector;
import info.hugoyu.mytraincontrol.switchable.impl.Turnout;
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

public class TurnoutNodeTest {

    @Mock
    Trainset train1, train2;

    @Mock
    Turnout turnout;

    private static final int ID = 147;
    private static final int ID_CLOSED = 145;
    private static final int ID_THROWN = 135;
    private static final int LENGTH_CLOSED = 186;
    private static final int LENGTH_THROWN = 188;

    private Vector UPLINK_CLOSED_VECTOR = new Vector(ID, ID_CLOSED);
    private Vector DOWNLINK_CLOSED_VECTOR = new Vector(ID_CLOSED, ID);
    private Vector UPLINK_THROWN_VECTOR = new Vector(ID, ID_THROWN);

    private Range<Integer> RANGE_FIRST_HALF = Range.closedOpen(0, LENGTH_CLOSED / 2);
    private Range<Integer> RANGE_SECOND_HALF = Range.closedOpen(LENGTH_CLOSED / 2, LENGTH_CLOSED);

    private TurnoutNode sut;

    @BeforeEach
    public void setUp() {
        initMocks(this);

        when(train1.getAddress()).thenReturn(1);
        when(train2.getAddress()).thenReturn(2);

        sut = new TurnoutNode(ID, ID_CLOSED, ID_THROWN, LENGTH_CLOSED, LENGTH_THROWN,
                TurnoutNode.Type.DIVERGE, 50, true, turnout);
    }

    @Test
    public void testGetConnections() {
        Set<Connection> connections = sut.getConnections();

        assertEquals(2, connections.size());

        Connection closedConnection = connections.stream()
                .filter(connection -> connection.getDist() == LENGTH_CLOSED)
                .findFirst()
                .get();
        assertTrue(closedConnection.isBidirectional());
        assertEquals(UPLINK_CLOSED_VECTOR, closedConnection.getVector());
        assertTrue(closedConnection.isUplink());

        Connection thrownConnection = connections.stream()
                .filter(connection -> connection.getDist() == LENGTH_THROWN)
                .findFirst()
                .get();
        assertTrue(thrownConnection.isBidirectional());
        assertEquals(UPLINK_THROWN_VECTOR, thrownConnection.getVector());
        assertTrue(closedConnection.isUplink());
    }

    @Test
    public void testIsFree() {
        assertTrue(sut.isFree(train1, UPLINK_CLOSED_VECTOR, RANGE_FIRST_HALF));
    }

    @Test
    public void testIsFreeWithRangeOccupiedBySameTrain() {
        sut.setOccupier(train1, UPLINK_CLOSED_VECTOR, RANGE_FIRST_HALF);

        assertTrue(sut.isFree(train1, UPLINK_CLOSED_VECTOR, RANGE_FIRST_HALF));
    }

    @Test
    public void testIsFreeWithRangeOccupiedByDifferentTrain() {
        sut.setOccupier(train1, UPLINK_CLOSED_VECTOR, RANGE_FIRST_HALF);

        assertFalse(sut.isFree(train2, UPLINK_CLOSED_VECTOR, RANGE_FIRST_HALF));
    }

    @Test
    public void testIsFreeWithRangeOccupiedByDifferentTrainNonOverlappingRange() {
        sut.setOccupier(train1, UPLINK_CLOSED_VECTOR, RANGE_FIRST_HALF);

        assertFalse(sut.isFree(train2, UPLINK_CLOSED_VECTOR, RANGE_SECOND_HALF));
    }

    @Test
    public void testIsFreeWithRangeOccupiedByDifferentVector() {
        sut.setOccupier(train1, UPLINK_CLOSED_VECTOR, RANGE_FIRST_HALF);

        assertFalse(sut.isFree(train1, UPLINK_THROWN_VECTOR, RANGE_FIRST_HALF));
    }

    @Test
    public void testIsFreeWithRangeOccupiedByDifferentDirectionVector() {
        sut.setOccupier(train1, UPLINK_CLOSED_VECTOR, RANGE_FIRST_HALF);

        assertFalse(sut.isFree(train1, DOWNLINK_CLOSED_VECTOR, RANGE_FIRST_HALF));
    }

    @Test
    public void testGetOccupiedRangeEmpty() {
        assertTrue(sut.getOccupiedRange(UPLINK_CLOSED_VECTOR, train1).isEmpty());
    }

    @Test
    public void testGetOccupiedRange() {
        sut.setOccupier(train1, UPLINK_CLOSED_VECTOR, RANGE_FIRST_HALF);

        assertEquals(RANGE_FIRST_HALF, sut.getOccupiedRange(UPLINK_CLOSED_VECTOR, train1).get());
    }

    @Test
    public void testGetOccupiedRangeUnoccupiedTrainset() {
        sut.setOccupier(train1, UPLINK_CLOSED_VECTOR, RANGE_FIRST_HALF);

        assertTrue(sut.getOccupiedRange(UPLINK_CLOSED_VECTOR, train2).isEmpty());
    }

    @Test
    public void testGetOccupiedRangeUnoccupiedVector() {
        sut.setOccupier(train1, UPLINK_CLOSED_VECTOR, RANGE_FIRST_HALF);

        assertTrue(sut.getOccupiedRange(DOWNLINK_CLOSED_VECTOR, train1).isEmpty());
    }
}