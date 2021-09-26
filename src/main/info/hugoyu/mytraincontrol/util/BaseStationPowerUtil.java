package info.hugoyu.mytraincontrol.util;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;

public class BaseStationPowerUtil {

    public static void turnOnPower() throws JmriException {
        PowerManager powerManager = InstanceManager.getNullableDefault(jmri.PowerManager.class);

        if (powerManager.getPower() != PowerManager.ON) {
            try {
                powerManager.setPower(PowerManager.ON);
            } catch (JmriException e) {
                e.printStackTrace();
            }
        }

        // retry
        if (powerManager.getPower() != PowerManager.ON) {
            powerManager.setPower(PowerManager.OFF);
            powerManager.setPower(PowerManager.ON);
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
