package info.hugoyu.mytraincontrol.layout.node.impl;

import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.json.layout.CrossoverJson;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.layout.node.Connection;
import info.hugoyu.mytraincontrol.switchable.impl.Crossover;
import info.hugoyu.mytraincontrol.trainset.Trainset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrossoverNode extends AbstractTrackNode {

    private final long uplinkId0, uplinkId1;
    private final long downlinkId0, downlinkId1;

    private final int length;

    private Crossover crossover;

    private final List<Connection> crossConnections;

    protected CrossoverNode(long uplinkId0, long uplinkId1,
                            long downlinkId0, long downlinkId1,
                            int length,
                            Crossover crossover,
                            List<Connection> crossConnections
    ) {
        super();

        this.uplinkId0 = uplinkId0;
        this.uplinkId1 = uplinkId1;
        this.downlinkId0 = downlinkId0;
        this.downlinkId1 = downlinkId1;

        this.length = length;

        this.crossover = crossover;

        this.crossConnections = crossConnections;
    }

    public CrossoverNode(CrossoverJson crossoverJson, List<Connection> crossConnections, Crossover crossover) {
        this(crossoverJson.getUplinkId0(),
                crossoverJson.getUplinkId1(),
                crossoverJson.getDownlinkId0(),
                crossoverJson.getDownlinkId1(),
                crossoverJson.getLength(),
                crossover,
                crossConnections);
    }

    @Override
    public BlockSectionResult alloc(Trainset trainset, int dist, Long nextNodeId, Long previousNodeId) throws NodeAllocationException {
        return null;
    }

    @Override
    public BlockSectionResult free(Trainset trainset, int dist) throws NodeAllocationException {
        return null;
    }

    @Override
    public void freeAll(Trainset trainset) throws NodeAllocationException {

    }

    @Override
    public List<Connection> getConnections() {
        List<Connection> connections = new ArrayList<>(crossConnections);
        connections.add(new Connection(uplinkId0, uplinkId1, length, true, false));
        connections.add(new Connection(downlinkId0, downlinkId1, length, false, false));
        return connections;
    }

    @Override
    public List<Long> getIds() {
        return List.of(uplinkId0, downlinkId0);
    }

    @Override
    public String getOwnerStatus(int ownerId) {
        return null;
    }

    @Override
    public Map<Integer, String> getOwnerSummary() {
        return null;
    }

}
