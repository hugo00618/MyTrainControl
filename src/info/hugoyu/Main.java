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

    private static Map<Integer, Loco> locomotives = new HashMap<>();

    public static void main(String[] args) throws Exception {
        selectPort();
        turnOnPower();

        // register N700A
        try {
            registerLoco(3, "N700A", "n700a-1000.profile");
        } catch (IOException e) {
            System.err.println("Failed to read locoProfile");
        }

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
        System.out.println("Power on");
    }

    private static void turnOffPower() throws JmriException {
        DCCppPowerManager powerManager = (DCCppPowerManager) InstanceManager.getNullableDefault(jmri.PowerManager.class);
        if (powerManager.getPower() != PowerManager.OFF) {
            powerManager.setPower(PowerManager.OFF);
        }

        System.out.println("Power off");
    }

    private static void registerLoco(int address, String name, String locoProfile) throws IOException {
        locomotives.put(address, new Loco(address, name, new LocoProfile(locoProfile)));
    }

    private static void listenCommands() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        System.out.println("Type command: ");

        while ((line = br.readLine()) != null) {
            try {
                String[] args = line.split(" +");
                if (line.startsWith("ms ")) { // measure
//                    if (args.length != 3) throw new CommandException(CommandException.COMMAND_MS);
//
//                    try {
//                        locomotives.get(Integer.parseInt(args[1])).setThrottleHard((float) (Integer.parseInt(args[2]) / 128.0));
//                    } catch (NumberFormatException e) {
//                        throw new CommandException(CommandException.COMMAND_MS);
//                    }
                } else if (line.startsWith("mv ")) { // move
                    if (args.length < 3 || args.length > 4)
                        throw new CommandException(CommandException.COMMAND_MV);

                    try {
                        if (args.length == 3) {
                            locomotives.get(Integer.parseInt(args[1])).move(Integer.parseInt(args[2]));
                        } else {
                            locomotives.get(Integer.parseInt(args[1])).move(Integer.parseInt(args[2]), Float.parseFloat(args[3]));
                        }
                    } catch (NumberFormatException e) {
                        throw new CommandException(CommandException.COMMAND_MV);
                    }
                } else if (line.startsWith("r ")) {

                } else if (line.startsWith("s ")) {
                    if (args.length != 2) throw new CommandException(CommandException.COMMAND_S);

                    try {
                        locomotives.get(Integer.parseInt(args[1])).stop();
                    } catch (NumberFormatException e) {
                        throw new CommandException(CommandException.COMMAND_S);
                    }
                } else if (line.startsWith("ss ")) {
//                    if (args.length != 3) throw new CommandException(CommandException.COMMAND_SS);
//
//                    try {
//                        setSpeed(args[1], args[2], false);
//                    } catch (CommandException e) {
//                        throw new CommandException(CommandException.COMMAND_SS);
//                    }
                } else if (line.startsWith("ssh ")) { // hard-set speed (take effect right away)
//                    if (args.length != 3) throw new CommandException(CommandException.COMMAND_SSH);
//
//                    try {
//                        setSpeed(args[1], args[2], true);
//                    } catch (CommandException e) {
//                        throw new CommandException(CommandException.COMMAND_SSH);
//                    }
                }
            } catch (CommandException e) {
                System.err.println(e.getMessage());
            }

            System.out.println("Type command: ");
        }
    }

    private static void cleanup() throws JmriException {
        // end loco's controlling thread
        for (Map.Entry<Integer, Loco> locomotive : locomotives.entrySet()) {
            locomotive.getValue().stopControlThread();
        }

        turnOffPower();
    }


}
