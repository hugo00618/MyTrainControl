package info.hugoyu.mytraincontrol.layout.node.track.impl;

import info.hugoyu.mytraincontrol.LayoutTestBase;
import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        RegularTrackNode node = (RegularTrackNode) LayoutUtil.getNode(10002);
        assertEquals(10002, node.getId());
    }

    @Test
    public void testAllocFree() throws NodeAllocationException {
        // node length is 992
        RegularTrackNode node = (RegularTrackNode) LayoutUtil.getNode(10002);

        BlockSectionResult blockSectionResult;

        assertThrows(NodeAllocationException.class, () -> node.free(train1, 1)); // freeing unowned track

        blockSectionResult = node.alloc(train1, 100, null, null); // owning [0, 100)
        assertEquals(100, blockSectionResult.getConsumedDist());
        assertEquals(0, blockSectionResult.getRemainingDist());

        assertThrows(NodeAllocationException.class, () -> node.free(train1, 101)); // freeing unowned range

        blockSectionResult = node.alloc(train1, 1408, null, null); // owning [0, 1508)
        assertEquals(1408, blockSectionResult.getConsumedDist());
        assertEquals(0, blockSectionResult.getRemainingDist());

        blockSectionResult = node.free(train1, 100); // owning [100, 1508]
        assertEquals(100, blockSectionResult.getConsumedDist());
        assertEquals(0, blockSectionResult.getRemainingDist());

        blockSectionResult = node.alloc(train1, 1, null, null); // allocating out-of-bound range
        assertEquals(0, blockSectionResult.getConsumedDist());
        assertEquals(1, blockSectionResult.getRemainingDist());

        blockSectionResult = node.free(train1, 1500); // freeing all remaining range (1508 - 100 = 1408)
        assertEquals(1408, blockSectionResult.getConsumedDist());
        assertEquals(92, blockSectionResult.getRemainingDist()); // remaining = 1500 - 1408 = 92

        assertThrows(NodeAllocationException.class, () -> node.free(train1, 1)); // freeing unowned track
    }

    @Test
    public void testAllocFreeWithTwoTrains() throws NodeAllocationException {
        // node length is 992
        RegularTrackNode node = (RegularTrackNode) LayoutUtil.getNode(10002);

        final BlockSectionResult[] blockSectionResult2 = new BlockSectionResult[1];

        node.alloc(train1, 100, null, null); // t1 owning [0, 100)

        // t2 trying to allocate [0, 100), going to hang
        Thread thread = new Thread(() -> {
            try {
                blockSectionResult2[0] = node.alloc(train2, 100, null, null);
                assertEquals(100, blockSectionResult2[0].getConsumedDist());
                assertEquals(0, blockSectionResult2[0].getRemainingDist());
            } catch (NodeAllocationException e) {
                e.printStackTrace();
            }
        });
        thread.start();

        // t2 waiting to alloc
        assertTrue(thread.isAlive());
        assertNull(blockSectionResult2[0]);

        node.alloc(train1, 100, null, null); // t1 owning [0, 200)
        // t2 still waiting to alloc
        assertTrue(thread.isAlive());
        assertNull(blockSectionResult2[0]);

        // t2 will finish allocating after t1 frees the section
        node.free(train1, 100); // t1 owning [100, 200)
    }

}