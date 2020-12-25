package info.hugoyu.mytraincontrol.util;

import com.google.common.collect.Range;

import java.util.Comparator;

public class RangeComparator implements Comparator<Range> {
    @Override
    public int compare(Range o1, Range o2) {
        return o1.lowerEndpoint().compareTo(o2.lowerEndpoint());
    }
}
