package info.hugoyu.mytraincontrol.util;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;

import java.util.List;

public class BaseStationPowerUtil {

    public static void turnOnPower() throws JmriException {
        // find PowerManager and return
        List<PowerManager> managers = InstanceManager.getList(PowerManager.class);
        for (PowerManager mgr : managers) {
            System.out.println("mgr name: " + mgr.getUserName());
        }

        PowerManager powerManager = InstanceManager.getNullableDefault(jmri.PowerManager.class);
        System.out.println("default mgr: " + powerManager.getUserName());

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
