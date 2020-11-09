package info.hugoyu.mytraincontrol;

import info.hugoyu.mytraincontrol.exceptions.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.exceptions.CommandNotFoundException;
import info.hugoyu.mytraincontrol.util.BaseStationUtil;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.implementation.JmriConfigurationManager;
import jmri.jmrix.dccpp.serial.DCCppAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Main {

    private static Map<Integer, Trainset> locomotives = new HashMap<>();

    public static void main(String[] args) throws Exception {
        selectPort();
        BaseStationUtil.turnOnPower();

        // register N700A
        CommandProvider.runCommand(new String[]{"r", "3", "N700A"});

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

        ConfigureManager cm = new JmriConfigurationManager();
        InstanceManager.setDefault(ConfigureManager.class, cm);

        System.out.println("Port opened: " + selectedPortName);
    }



    private static void listenCommands() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;
        System.out.println("Type command: ");

        while ((line = br.readLine()) != null) {
            try {
                String[] args = line.split(" +");
                CommandProvider.runCommand(args);
            } catch (CommandInvalidUsageException e) {
                System.err.println(e.getMessage());
            } catch (CommandNotFoundException e) {

            } catch (Exception e) {
                // should not run into this case
            }

            System.out.println("Type command: ");
        }
    }

    private static void cleanup() throws JmriException {
        BaseStationUtil.turnOffPower();
    }


}
