package info.hugoyu;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrix.dccpp.DCCppPowerManager;
import jmri.jmrix.dccpp.serial.DCCppAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Main {

    public static final String COMMAND_MS   = "ms addr [speed (0 - 128)]";
    public static final String COMMAND_MV   = "mv addr [dist] {[max speed (0.0 - 1.0)]}";
    public static final String COMMAND_R    = "r addr bandai";
    public static final String COMMAND_S    = "s addr";

    private static Map<Integer, Loco> locomotives = new HashMap<>();

    public static void main(String[] args) throws Exception {
        selectPort();
        turnOnPower();
        // construct dcc command thread and start
        ThrottleControlThread.getInstance();

        listenCommands();

        cleanup();
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
        DCCppPowerManager powerManager = (DCCppPowerManager) InstanceManager.getNullableDefault(jmri.PowerManager.class);
        if (powerManager.getPower() != PowerManager.ON) {
            powerManager.setPower(PowerManager.ON);
        }
    }

    private static void turnOffPower() throws JmriException {
        DCCppPowerManager powerManager = (DCCppPowerManager) InstanceManager.getNullableDefault(jmri.PowerManager.class);
        if (powerManager.getPower() != PowerManager.OFF) {
            powerManager.setPower(PowerManager.OFF);
        }
    }

    private static void registerLoco(int address, String bandai) throws IOException {
        String alias = Utils.getInstance().getLocoAlias(address);
        LocoProfile profile = new LocoProfile(Utils.getInstance().getLocoProfilePath(address, bandai));
        locomotives.put(address, new Loco(address, alias, profile));
        System.out.println("Registered: " + alias);
    }

    private static void listenCommands() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;

        while (true) {
            System.out.println("Type command: ");
            if ((line = br.readLine()) == null) break;

            try {
                String[] args = line.split(" +");
                if (line.startsWith("ms ")) { // measure throttle byte
                    if (args.length != 3) throw new IllegalArgumentException(COMMAND_MS);

                    try {
                        locomotives.get(Integer.parseInt(args[1])).setThrottleByte((Integer.parseInt(args[2])));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(COMMAND_MS);
                    }
                } else if (line.startsWith("mv ")) { // move
                    if (args.length < 3 || args.length > 4)
                        throw new IllegalArgumentException(COMMAND_MV);

                    try {
                        int locoAddr = Integer.parseInt(args[1]);
                        if (!locomotives.containsKey(locoAddr)) throw new IllegalArgumentException(COMMAND_MV);
                        Loco loco = locomotives.get(locoAddr);
                        if (args.length == 3) {
                            loco.move(Integer.parseInt(args[2]));
                        } else {
                            loco.move(Integer.parseInt(args[2]), Double.parseDouble(args[3]));
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(COMMAND_MV);
                    }
                } else if (line.startsWith("r ")) { // register loco
                    if (args.length != 3) throw new IllegalArgumentException(COMMAND_R);

                    try {
                        registerLoco(Integer.parseInt(args[1]), args[2]);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(COMMAND_R);
                    }
                } else if (line.startsWith("s ")) { // stop
                    if (args.length != 2) throw new IllegalArgumentException(COMMAND_S);

                    try {
                        locomotives.get(Integer.parseInt(args[1])).stop();
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(COMMAND_S);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown command");
                }
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static void cleanup() throws JmriException {
        // end loco's controlling thread
        for (Map.Entry<Integer, Loco> locomotive : locomotives.entrySet()) {
            locomotive.getValue().stopControlThread();
        }

        // end dcc command thread
        ThrottleControlThread.getInstance().interrupt();

        turnOffPower();
    }


}
