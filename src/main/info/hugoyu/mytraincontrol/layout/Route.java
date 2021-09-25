package info.hugoyu.mytraincontrol.layout;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Route implements Comparable<Route> {

    private List<Long> nodes;
    private int cost;

    @Override
    public int compareTo(Route o) {
        return this.getCost() - o.getCost();
    }
}
