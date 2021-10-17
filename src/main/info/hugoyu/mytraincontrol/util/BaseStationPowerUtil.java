package info.hugoyu.mytraincontrol.util;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;

public class BaseStationPowerUtil {

    public static void turnOnPower() throws JmriException {
        int powerState = setPower(true);
        System.out.println("Power state: " + powerState);
    }

    public static void turnOffPower() throws JmriException {
        setPower(false);
        System.out.println("Power is off");
    }

    private static int setPower(boolean on) throws JmriException {
        PowerManager powerManager = InstanceManager.getNullableDefault(jmri.PowerManager.class);
        if (powerManager == null) {
            throw new RuntimeException("jmri.PowerManager is null");
        }

        int powerState = on ? PowerManager.ON : PowerManager.OFF;
        powerManager.setPower(powerState);
        return powerManager.getPower();
    }
}
