package info.hugoyu.mytraincontrol;

import info.hugoyu.mytraincontrol.commandstation.CommandStationRunnable;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.exception.CommandNotFoundException;
import info.hugoyu.mytraincontrol.util.BaseStationPowerUtil;
import info.hugoyu.mytraincontrol.util.CommandUtil;
import info.hugoyu.mytraincontrol.util.LayoutUtil;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.implementation.JmriConfigurationManager;
import jmri.jmrix.dccpp.serial.DCCppAdapter;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

@Log4j2
public class Main {

    public static void main(String[] args) throws Exception {
        log.info("System start");

        selectPort();
        BaseStationPowerUtil.turnOnPower();

        // register trains
        CommandUtil.runCommand(new String[]{"reg", "3", "N700A Series", "n700a-4000.json"});
        CommandUtil.runCommand(new String[]{"reg", "4", "500 Series", "500-4000.json"});
//        CommandUtil.runCommand(new String[]{"reg", "5", "E6", "e6-4000.json"});
        CommandUtil.runCommand(new String[]{"reg", "12", "E2", "e2-4000.json", "true"});
        CommandUtil.runCommand(new String[]{"reg", "13", "E3", "e3-4000.json", "true"});
        CommandUtil.runCommand(new String[]{"reg", "14", "E4", "e4-4000.json", "true"});
        CommandUtil.runCommand(new String[]{"reg", "15", "E5E6", "e5e6-4300.json", "true"});
        CommandUtil.runCommand(new String[]{"reg", "17", "E7", "e7-4000.json", "true"});

        LayoutUtil.registerLayout();

        LayoutUtil.restoreLayoutState();

        CommandStationRunnable.getInstance();
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

        String errorMessage = adapter.openPort(selectedPortName, "ProjectTrainControl");
        if (errorMessage != null) {
            System.err.println("Failed to open port: " + errorMessage);
        }
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
            String[] args = line.split(" +");
            try {
                CommandUtil.runCommand(args);
            } catch (CommandNotFoundException e) {
                System.err.println("Command not found");
            } catch (CommandInvalidUsageException e) {
                e.printStackTrace();
                System.err.println("Usage: " + args[0] + " " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Type command: ");
        }
    }

    private static void cleanup() {
        LayoutUtil.saveLayoutState();
        BaseStationPowerUtil.turnOffPower();
    }


}
