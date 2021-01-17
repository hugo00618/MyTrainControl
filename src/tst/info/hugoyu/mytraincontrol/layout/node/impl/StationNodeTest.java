package info.hugoyu.mytraincontrol.layout.node.impl;

import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.LayoutTestBase;
import info.hugoyu.mytraincontrol.layout.node.BlockSectionResult;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StationNodeTest extends LayoutTestBase {

    StationNode stationNode;

    Trainset trainset;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        stationNode = (StationNode) layout.getNodes().get("3017");
        trainset = new Trainset(3, "N700A", "n700a-6000.json");
    }

    @Test
    void allocWithNoRemainingDist() {
        assertEquals(992, stationNode.getLength());
        assertEquals(960, trainset.getProfile().getTotalLength());

        BlockSectionResult res = stationNode.alloc(trainset, 200, null);

        assertEquals(trainset, stationNode.getOwner());
        assertEquals(0, res.getRemainingDist());
        assertFalse(res.isSectionComplete());
    }

    @Test
    void allocEntireSectionSharp() {
        assertEquals(992, stationNode.getLength());
        assertEquals(960, trainset.getProfile().getTotalLength());

        BlockSectionResult res = stationNode.alloc(trainset, 992, null);

        assertEquals(trainset, stationNode.getOwner());
        assertEquals(0, res.getRemainingDist()); // 992 - 992 = 0
        assertTrue(res.isSectionComplete());
    }

    @Test
    void allocWithRemainingDist() {
        assertEquals(992, stationNode.getLength());
        assertEquals(960, trainset.getProfile().getTotalLength());

        BlockSectionResult res = stationNode.alloc(trainset, 1000, null);

        assertEquals(trainset, stationNode.getOwner());
        assertEquals(8, res.getRemainingDist()); // 1000 - 992 = 8
        assertTrue(res.isSectionComplete());
    }

    @Test
    void freeUnowned() {
        NodeAllocationException e = assertThrows(NodeAllocationException.class, () -> stationNode.free(trainset, 200));
        assertEquals(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION, e.getExceptionType());

        assertEquals(992, stationNode.getLength());
        assertEquals(960, trainset.getProfile().getTotalLength());

        stationNode.alloc(trainset, 200, null);
        e = assertThrows(NodeAllocationException.class, () -> stationNode.free(trainset, 300));
        assertEquals(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION, e.getExceptionType());
    }

    @Test
    void freeWithNoRemainingDist() throws NodeAllocationException {
        assertEquals(992, stationNode.getLength());
        assertEquals(960, trainset.getProfile().getTotalLength());

        stationNode.alloc(trainset, 992, null);
        BlockSectionResult res = stationNode.free(trainset, 900);

        assertEquals(trainset, stationNode.getOwner());
        assertEquals(0, res.getRemainingDist());
        assertFalse(res.isSectionComplete());
    }

    @Test
    void freeEntireSectionSharp() throws NodeAllocationException {
        assertEquals(992, stationNode.getLength());
        assertEquals(960, trainset.getProfile().getTotalLength());

        stationNode.alloc(trainset, 992, null);
        BlockSectionResult res = stationNode.free(trainset, 992);

        assertNull(stationNode.getOwner());
        assertEquals(0, res.getRemainingDist());
        assertTrue(res.isSectionComplete());
    }

    @Test
    void freeWithRemainingDist() throws NodeAllocationException {
        assertEquals(992, stationNode.getLength());
        assertEquals(960, trainset.getProfile().getTotalLength());

        stationNode.alloc(trainset, 992, null);
        BlockSectionResult res = stationNode.free(trainset, 1000); // remainingDist: 1000 - 992 = 8

        assertNull(stationNode.getOwner());
        assertEquals(8, res.getRemainingDist());
        assertTrue(res.isSectionComplete());
    }

    @Test
    void combineAllocAndFree() throws NodeAllocationException {
        assertEquals(992, stationNode.getLength());
        assertEquals(960, trainset.getProfile().getTotalLength());

        BlockSectionResult res = stationNode.alloc(trainset, 200, null); // [0, 200), section remaining: 992 - 200 = 792
        assertEquals(trainset, stationNode.getOwner());
        assertEquals(0, res.getRemainingDist());
        assertFalse(res.isSectionComplete());

        res = stationNode.alloc(trainset, 800, null); // remainingDist 800 - 792 = 8
        assertEquals(8, res.getRemainingDist());
        assertTrue(res.isSectionComplete());

        res = stationNode.free(trainset, 100); // [100, 992), section remaining: 992 - 100 = 892
        assertEquals(trainset, stationNode.getOwner());
        assertEquals(0, res.getRemainingDist());
        assertFalse(res.isSectionComplete());

        res = stationNode.free(trainset, 900); // remainingDist: 900 - 892 = 8
        assertEquals(null, stationNode.getOwner());
        assertEquals(8, res.getRemainingDist());
        assertTrue(res.isSectionComplete());
    }
}