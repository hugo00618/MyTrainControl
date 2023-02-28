package info.hugoyu.mytraincontrol.util;

import com.google.common.collect.Range;

import java.util.Collection;
import java.util.Random;

public class GeneralUtil {

    public static <T> T getRandom(Collection<T> collection) {
        return collection.stream()
                .skip(new Random().nextInt(collection.size()))
                .findFirst()
                .orElse(null);
    }

    public static <T extends Comparable<T>> boolean isOverlapping(Range<T> range1, Range<T> range2) {
        return range1.isConnected(range2) && !range1.intersection(range2).isEmpty();
    }

}
