package info.hugoyu.mytraincontrol.layout.node;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Setter
@Getter
public abstract class AbstractGraphNode implements IGraphNode {

    @NonNull
    protected String id;

    private Map<String, Integer> nextNodes = new HashMap<>();

    // destination node id -> minimum cost
    private Map<String, Integer> minCostToNode = new HashMap<>();

    // destination node -> next node's id that goes to destination with min cost
    private Map<String, String> minCostNextNode = new HashMap<>();

    public AbstractGraphNode(String id, String nextNodeId, int cost) {
        this(id);
        this.add(nextNodeId, cost);
    }

    public void add(String nodeId, int cost) {
        nextNodes.put(nodeId, cost);
    }

    public Set<String> getNextNodes() {
        return nextNodes.keySet();
    }

    public int getCostToNextNode(String nodeId) {
        return nextNodes.get(nodeId);
    }

    public String getMinCostNextNode(String toNode) {
        return minCostNextNode.get(toNode);
    }

    public int getMinCostToNode(String node) {
        if (!minCostToNode.containsKey(node)) {
            minCostToNode.put(node, Integer.MAX_VALUE);
        }
        return minCostToNode.get(node);
    }

    public void setMinCostToNode(String toNode, String nextNode, int cost) {
        if (!minCostToNode.containsKey(toNode)) {
            minCostToNode.put(toNode, Integer.MAX_VALUE);
        }
        if (cost < minCostToNode.get(toNode)) {
            minCostToNode.put(toNode, cost);
            minCostNextNode.put(toNode, nextNode);
        }
    }


}
