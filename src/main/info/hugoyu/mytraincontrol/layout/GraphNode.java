package info.hugoyu.mytraincontrol.layout;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor
public class GraphNode {

    Map<GraphNode, Integer> nextNodes = new HashMap<>();

    // destination node -> minimum cost
    Map<GraphNode, Integer> minCostToNode = new HashMap<>();
    // destination node -> next node that goes to destination with min cost
    Map<GraphNode, GraphNode> minCostNextNode = new HashMap<>();

    public GraphNode(GraphNode nextNode, int cost) {
        nextNodes.put(nextNode, cost);
    }

    public void add(GraphNode node, int cost) {
        nextNodes.put(node, cost);
    }

    public Set<GraphNode> getNextNodes() {
        return nextNodes.keySet();
    }

    public int getCostToNextNode(GraphNode node) {
        return nextNodes.get(node);
    }

    public GraphNode getMinCostNextNode(GraphNode to) {
        return minCostNextNode.get(to);
    }

    public int getMinCostToNode(GraphNode node) {
        if (!minCostToNode.containsKey(node)) {
            minCostToNode.put(node, Integer.MAX_VALUE);
        }
        return minCostToNode.get(node);
    }

    public void setMinCostToNode(GraphNode to, GraphNode next, int cost) {
        if (!minCostToNode.containsKey(to)) {
            minCostToNode.put(to, Integer.MAX_VALUE);
        }
        if (cost < minCostToNode.get(to)) {
            minCostToNode.put(to, cost);
            minCostNextNode.put(to, next);
        }
    }


}
