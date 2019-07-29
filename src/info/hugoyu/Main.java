package info.hugoyu;

import jmri.*;
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
        registerLoco(3, "N700A");

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
        PowerManager powerManager = InstanceManager.getNullableDefault(jmri.PowerManager.class);
        powerManager.setPower(PowerManager.ON);
        System.out.println("Power on");
    }

    private static void turnOffPower() throws JmriException {
        PowerManager powerManager = InstanceManager.getNullableDefault(jmri.PowerManager.class);
        if (powerManager.getPower() != PowerManager.OFF) {
            powerManager.setPower(PowerManager.OFF);
        }

        System.out.println("Power off");
    }

    private static void registerLoco(int address, String name) {
        locomotives.put(address, new Loco(address, name));
    }

    private static void listenCommands() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        System.out.println("Type command: ");

        while ((line = br.readLine()) != null) {
            try {
                String[] args = line.split(" +");
                if (line.startsWith("f")) {
                    if (args.length != 3) throw new CommandException(CommandException.COMMAND_F);

                    try {
                        setSpeed(args[1], args[2]);
                    } catch (CommandException e) {
                        throw new CommandException(CommandException.COMMAND_F);
                    }
                } else if (line.startsWith("r")) {

                } else if (line.startsWith("s")) {
                    if (args.length != 2) throw new CommandException(CommandException.COMMAND_S);

                    try {
                        setSpeed(args[1], "0");
                    } catch (CommandException e) {
                        throw new CommandException(CommandException.COMMAND_S);
                    }
                }
            } catch (CommandException e) {
                System.err.println(e.getMessage());
            }

            System.out.println("Type command: ");
        }
    }

    private static void setSpeed(String addressStr, String speedStr) throws CommandException {
        try {
            int address = Integer.parseInt(addressStr);
            float speed =  Float.parseFloat(speedStr);
            locomotives.get(address).setSpeed(speed);
        } catch (NumberFormatException e) {
            throw new CommandException("");
        }
    }

    private static void cleanup() throws JmriException {
        turnOffPower();
    }


}
