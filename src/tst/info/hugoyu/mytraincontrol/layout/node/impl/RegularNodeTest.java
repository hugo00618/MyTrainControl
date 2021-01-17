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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegularNodeTest extends LayoutTestBase {

    RegularNode regularNode;

    Trainset trainset;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        regularNode = (RegularNode) layout.getNodes().get("0"); // section [0, 1508)
        trainset = new Trainset(3, "N700A", "n700a-6000.json");
    }

    @Test
    void allocEntireSectionSharp() {
        BlockSectionResult res = regularNode.alloc(trainset, 100, null); // [0, 100)
        assertEquals(0, res.getRemainingDist());
        assertFalse(res.isSectionComplete());

        res = regularNode.alloc(trainset, 200, null); // [0, 300)
        assertEquals(0, res.getRemainingDist());
        assertFalse(res.isSectionComplete());

        res = regularNode.alloc(trainset, 1208, null); // [0, 1508), entire section allocated
        assertEquals(0, res.getRemainingDist());
        assertTrue(res.isSectionComplete());
    }

    @Test
    void allocWithRemainingDist() {
        BlockSectionResult res = regularNode.alloc(trainset, 100, null); // [0, 100)
        assertEquals(0, res.getRemainingDist());
        assertFalse(res.isSectionComplete());

        res = regularNode.alloc(trainset, 1500, null); // [0, 1508), remaining 1600 - 1508 = 92
        assertEquals(92, res.getRemainingDist());
        assertTrue(res.isSectionComplete());
    }

    @Test
    void freeUnowned() {
        NodeAllocationException e = assertThrows(NodeAllocationException.class, () -> regularNode.free(trainset, 100));
        assertEquals(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION, e.getExceptionType());

        regularNode.alloc(trainset, 200, null); // [0, 200)
        e = assertThrows(NodeAllocationException.class, () -> regularNode.free(trainset, 300));
        assertEquals(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION, e.getExceptionType());
    }

    @Test
    void freeEntireSectionSharp() throws NodeAllocationException {
        regularNode.alloc(trainset, 1508, null); // [0, 1508)

        BlockSectionResult res = regularNode.free(trainset, 300); // [300, 1508), allocated length: 1208
        assertEquals(0, res.getRemainingDist());
        assertFalse(res.isSectionComplete());

        res = regularNode.free(trainset, 1208); // entire section freed
        assertEquals(0, res.getRemainingDist());
        assertTrue(res.isSectionComplete());
    }

    @Test
    void freeWithRemainingDist() throws NodeAllocationException {
        regularNode.alloc(trainset, 1508, null); // [0, 1508)

        BlockSectionResult res = regularNode.free(trainset, 300); // [300, 1508), allocated length: 1208
        assertEquals(0, res.getRemainingDist());
        assertFalse(res.isSectionComplete());

        res = regularNode.free(trainset, 1300); // remainingDist: 1300 - 1208 = 92
        assertEquals(92, res.getRemainingDist());
        assertTrue(res.isSectionComplete());
    }

    @Test
    void combineAllocAndFree() throws NodeAllocationException {
        BlockSectionResult res  = regularNode.alloc(trainset, 200, null); // [0, 200)
        assertEquals(0, res.getRemainingDist());
        assertFalse(res.isSectionComplete());

        NodeAllocationException e = assertThrows(NodeAllocationException.class, () -> regularNode.free(trainset, 300));
        assertEquals(NodeAllocationException.ExceptionType.FREEING_UNOWNED_SECTION, e.getExceptionType());

        res  = regularNode.free(trainset, 100); // [100, 200)
        assertEquals(0, res.getRemainingDist());
        assertFalse(res.isSectionComplete());

        res = regularNode.alloc(trainset, 1400, null); // [100, 1508), remaining 1600 - 1508 = 92
        assertEquals(92, res.getRemainingDist());
        assertTrue(res.isSectionComplete());

        res = regularNode.free(trainset, 400); // [500, 1508), length: 1508 - 500 = 1008
        assertEquals(0, res.getRemainingDist());
        assertFalse(res.isSectionComplete());

        res = regularNode.free(trainset, 1100); // remaining: 1100 - 1008 = 92
        assertEquals(92, res.getRemainingDist());
        assertTrue(res.isSectionComplete());
    }

}