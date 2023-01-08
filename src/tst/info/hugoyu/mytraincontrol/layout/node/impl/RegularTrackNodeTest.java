package info.hugoyu.mytraincontrol.layout.node.impl;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class RegularTrackNodeTest extends LayoutTestBase {

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
    public void testLayoutProvider() {
        RegularTrackNode node = (RegularTrackNode) LayoutUtil.getNode(151);
        assertEquals(List.of(151L), node.getIds());
    }

    @Test
    public void testAllocFree() throws NodeAllocationException {
        // node length is 1301
        RegularTrackNode node = (RegularTrackNode) LayoutUtil.getNode(151);

        BlockSectionResult blockSectionResult;

        assertThrows(NodeAllocationException.class, () -> node.free(train1, 1)); // freeing unowned track

        blockSectionResult = node.alloc(train1, 100, null, null); // owning [0, 100)
        assertEquals(100, blockSectionResult.getConsumedDist());
        assertEquals(0, blockSectionResult.getRemainingDist());

        assertThrows(NodeAllocationException.class, () -> node.free(train1, 101)); // freeing unowned range

        blockSectionResult = node.alloc(train1, 1200, null, null); // owning [0, 1300)
        assertEquals(1200, blockSectionResult.getConsumedDist());
        assertEquals(0, blockSectionResult.getRemainingDist());

        blockSectionResult = node.free(train1, 100); // owning [100, 1300)
        assertEquals(100, blockSectionResult.getConsumedDist());
        assertEquals(0, blockSectionResult.getRemainingDist());

        // allocating [100, 1301] with 1 remaining
        blockSectionResult = node.alloc(train1, 2, null, null);
        assertEquals(1, blockSectionResult.getConsumedDist());
        assertEquals(1, blockSectionResult.getRemainingDist());

        blockSectionResult = node.free(train1, 1300); // freeing all remaining range (1201)
        assertEquals(1201, blockSectionResult.getConsumedDist());
        assertEquals(99, blockSectionResult.getRemainingDist()); // 99 remaining

        assertThrows(NodeAllocationException.class, () -> node.free(train1, 1)); // freeing unowned track
    }

    @Test
    public void testAllocFreeWithTwoTrains() throws Exception {
        // node length is 1301
        RegularTrackNode node = (RegularTrackNode) LayoutUtil.getNode(151);

        node.alloc(train1, 100, null, null); // t1 owning [0, 100)

        // t2 trying to allocate [0, 100), going to hang
        final BlockSectionResult[] blockSectionResult2 = new BlockSectionResult[1];
        Thread t2Thread = new Thread(() -> {
            try {
                blockSectionResult2[0] = node.alloc(train2, 100, null, null);
            } catch (NodeAllocationException e) {
                e.printStackTrace();
            }
        });
        t2Thread.start();

        // assert t2 is waiting to alloc
        assertTrue(t2Thread.isAlive());
        assertNull(blockSectionResult2[0]);

        node.alloc(train1, 100, null, null); // t1 owning [0, 200)

        // t2 still waiting to alloc
        assertTrue(t2Thread.isAlive());
        assertNull(blockSectionResult2[0]);

        // t2 will finish allocating after t1 frees the section
        node.free(train1, 101); // t1 owning [101, 200)
        Thread.sleep(20);

        assertFalse(t2Thread.isAlive());
        assertEquals(100, blockSectionResult2[0].getConsumedDist());
        assertEquals(0, blockSectionResult2[0].getRemainingDist());
    }

}