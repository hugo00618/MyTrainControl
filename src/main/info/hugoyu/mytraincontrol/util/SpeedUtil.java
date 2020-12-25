package info.hugoyu.mytraincontrol.util;

public class SpeedUtil {

    private static final double SCALE = 150; // n scale
    private static final double MMPS_TO_KPH = 0.0036;

    public static double toMMps(double kph) {
        return kph / MMPS_TO_KPH / SCALE;
    }

}
