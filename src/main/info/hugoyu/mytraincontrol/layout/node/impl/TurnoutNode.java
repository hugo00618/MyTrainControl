package info.hugoyu.mytraincontrol.layout.node.impl;

import info.hugoyu.mytraincontrol.exception.NodeAllocationException;
import info.hugoyu.mytraincontrol.json.layout.TurnoutJson;
import info.hugoyu.mytraincontrol.layout.BlockSectionResult;
import info.hugoyu.mytraincontrol.layout.node.AbstractTrackNode;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.LayoutUtil;

public class TurnoutNode extends AbstractTrackNode {

    public enum Type {
        DIVERGE("diverge"),
        MERGE("merge");

        String type;

        Type(String type) {
            this.type = type;
        }
    }

    private long id1, id2;
    private int dist1, dist2;
    private Type type;

    public TurnoutNode(long id, long id1, long id2, Long id3, int dist1, int dist2, Type type) {
        super(id);

        this.id1 = id1;
        this.id2 = id2;
        this.dist1 = dist1;
        this.dist2 = dist2;
        this.type = type;

        switch (type) {
            case MERGE:
                LayoutUtil.getNode(id1).addConnection(id, dist1);
                LayoutUtil.getNode(id2).addConnection(id, dist2);
                addConnection(id3, 0);
                break;
            case DIVERGE:
                addConnection(id1, dist1);
                addConnection(id2, dist2);
                break;
            default:
                break;
        }
    }

    public TurnoutNode(TurnoutJson turnoutJson) {
        this(turnoutJson.getId0(),
                turnoutJson.getId1(),
                turnoutJson.getId2(),
                turnoutJson.getId3(),
                turnoutJson.getDist1(),
                turnoutJson.getDist2(),
                turnoutJson.getType());
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
    public String getOwnerStatus(int ownerId) {
        return null;
    }
}
