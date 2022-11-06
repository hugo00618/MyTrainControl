package info.hugoyu.mytraincontrol.util;

import java.util.Collection;
import java.util.Random;

public class GeneralUtil {

    public static <T> T getRandom(Collection<T> collection) {
        return collection.stream()
                .skip(new Random().nextInt(collection.size()))
                .findFirst()
                .orElse(null);
    }

}
