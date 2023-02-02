package info.hugoyu.mytraincontrol.layout.node.impl;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class TurnoutNodeTest extends LayoutTestBase {

    @Mock
    Trainset train1, train2;

    @BeforeEach
    public void setUp() {
        super.setUp();

        initMocks(this);

        when(train1.getAddress()).thenReturn(1);
        when(train2.getAddress()).thenReturn(2);
    }

    @Test
    public void testAllocFreeDiverge() throws NodeAllocationException {
//        TurnoutNode node = (TurnoutNode) LayoutUtil.getNode(10001);
//
//        BlockSectionResult blockSectionResult;
//
//        assertThrows(NodeAllocationException.class, () -> node.free(train1, 1)); // freeing unowned track
//
//        // total length: 188
//        blockSectionResult = node.alloc(train1, 100, 10103L, null); // owning [0, 100)
//        assertEquals(100, blockSectionResult.getConsumedDist());
//        assertEquals(0, blockSectionResult.getRemainingDist());
//
//        assertThrows(NodeAllocationException.class, () -> node.free(train1, 101)); // freeing unowned range
//
//        blockSectionResult = node.alloc(train1, 88, null, null); // owning [0, 188)
//        assertEquals(88, blockSectionResult.getConsumedDist());
//        assertEquals(0, blockSectionResult.getRemainingDist());
//
//        blockSectionResult = node.free(train1, 100); // owning [100, 188]
//        assertEquals(100, blockSectionResult.getConsumedDist());
//        assertEquals(0, blockSectionResult.getRemainingDist());
//
//        blockSectionResult = node.alloc(train1, 1, null, null); // allocating out-of-bound range
//        assertEquals(0, blockSectionResult.getConsumedDist());
//        assertEquals(1, blockSectionResult.getRemainingDist());
//
//        blockSectionResult = node.free(train1, 188); // freeing all remaining range,  dist = 188 - 100 = 88
//        assertEquals(88, blockSectionResult.getConsumedDist());
//        assertEquals(100, blockSectionResult.getRemainingDist()); // remaining = 188 - 88 = 100
//
//        assertThrows(NodeAllocationException.class, () -> node.free(train1, 1)); // freeing unowned track
    }

    @Test
    public void testAllocFreeMerge() throws NodeAllocationException {
//        TurnoutNode node = (TurnoutNode) LayoutUtil.getNode(10003);
//
//        BlockSectionResult blockSectionResult;
//
//        assertThrows(NodeAllocationException.class, () -> node.free(train1, 1)); // freeing unowned track
//
//        // total length: 186
//        blockSectionResult = node.alloc(train1, 100, null, 10101L); // owning [0, 100)
//        assertEquals(100, blockSectionResult.getConsumedDist());
//        assertEquals(0, blockSectionResult.getRemainingDist());
//
//        assertThrows(NodeAllocationException.class, () -> node.free(train1, 101)); // freeing unowned range
//
//        blockSectionResult = node.alloc(train1, 86, null, null); // owning [0, 186)
//        assertEquals(86, blockSectionResult.getConsumedDist());
//        assertEquals(0, blockSectionResult.getRemainingDist());
//
//        blockSectionResult = node.free(train1, 100); // owning [100, 186]
//        assertEquals(100, blockSectionResult.getConsumedDist());
//        assertEquals(0, blockSectionResult.getRemainingDist());
//
//        blockSectionResult = node.alloc(train1, 1, null, null); // allocating out-of-bound range
//        assertEquals(0, blockSectionResult.getConsumedDist());
//        assertEquals(1, blockSectionResult.getRemainingDist());
//
//        blockSectionResult = node.free(train1, 186); // freeing all remaining range,  dist = 186 - 100 = 86
//        assertEquals(86, blockSectionResult.getConsumedDist());
//        assertEquals(100, blockSectionResult.getRemainingDist()); // remaining = 186 - 86 = 100
//
//        assertThrows(NodeAllocationException.class, () -> node.free(train1, 1)); // freeing unowned track
    }

    @Test
    public void testAllocFreeWithTwoTrains() throws Exception {
//        TurnoutNode node = (TurnoutNode) LayoutUtil.getNode(10001);
//
//        final BlockSectionResult[] blockSectionResult2 = new BlockSectionResult[1];
//
//        // total length: 188
//        node.alloc(train1, 186, 10103L, null);
//        node.free(train1, 100);  // t1 owning [100, 186)
//
//        // t2 trying to allocate [0, 100), going to hang
//        Thread thread = new Thread(() -> {
//            try {
//                blockSectionResult2[0] = node.alloc(train2, 100, 10101L, null);
//            } catch (NodeAllocationException e) {
//                e.printStackTrace();
//            }
//        });
//        thread.start();
//
//        // t2 waiting to alloc
//        assertTrue(thread.isAlive());
//        assertNull(blockSectionResult2[0]);
//
//        node.free(train1, 85); // t1 owning [185, 186)
//        // t2 still waiting to alloc
//        assertTrue(thread.isAlive());
//        assertNull(blockSectionResult2[0]);
//
//        // t2 will finish allocating after t1 frees the section
//        node.free(train1, 1);
//        while (thread.isAlive()) {
//            Thread.sleep(10);
//        }
//        assertEquals(100, blockSectionResult2[0].getConsumedDist());
//        assertEquals(0, blockSectionResult2[0].getRemainingDist());
    }
}