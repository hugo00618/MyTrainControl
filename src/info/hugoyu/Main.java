package info.hugoyu;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrix.dccpp.serial.DCCppAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class Main {

    public static void main(String[] args) throws Exception {
        selectPort();
        turnOnPower();

        // listen for commands
    }

    private static void selectPort() throws IOException {
        DCCppAdapter adapter = new DCCppAdapter();
        Vector<String> portNames = adapter.getPortNames();

        System.out.println("Select ports: ");
        for (int i = 0; i < portNames.size(); i++) {
            System.out.println(i + ": " + portNames.get(i));
        }

        int selectedPort = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
        String selectedPortName = portNames.get(selectedPort);

        adapter.openPort(selectedPortName, "JMRI");
        adapter.configure();

        System.out.println("Port opened: " + selectedPortName);
    }

    private static void turnOnPower() throws JmriException {
        PowerManager powerManager = InstanceManager.getNullableDefault(jmri.PowerManager.class);
        if (powerManager.getPower() != PowerManager.ON) {
            powerManager.setPower(PowerManager.ON);
        }

        System.out.println("Power on");
    }


}
