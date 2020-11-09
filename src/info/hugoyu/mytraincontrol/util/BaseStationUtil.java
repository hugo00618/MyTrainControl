package info.hugoyu.mytraincontrol.util;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;

public class BaseStationUtil {

    public static void turnOnPower() throws JmriException {
        PowerManager powerManager = InstanceManager.getNullableDefault(jmri.PowerManager.class);

        try {
            powerManager.setPower(PowerManager.ON);
            System.out.println("Power on");
        } catch (JmriException e) {
            e.printStackTrace();
        }

        System.out.println("Power state: " + powerManager.getPower());
    }

    public static void turnOffPower() throws JmriException {
        PowerManager powerManager = InstanceManager.getNullableDefault(jmri.PowerManager.class);
        if (powerManager.getPower() != PowerManager.OFF) {
            powerManager.setPower(PowerManager.OFF);
        }

        System.out.println("Power off");
    }
}
