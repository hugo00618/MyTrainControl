package info.hugoyu.mytraincontrol;

import info.hugoyu.mytraincontrol.commandstation.CommandStationRunnable;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.exception.CommandNotFoundException;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.BaseStationPowerUtil;
import info.hugoyu.mytraincontrol.util.CommandUtil;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.implementation.JmriConfigurationManager;
import jmri.jmrix.dccpp.serial.DCCppAdapter;
import lombok.extern.log4j.Log4j;
import org.apache.log4j.PropertyConfigurator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

@Log4j
public class Main {

    private static final String LOG4J_CONFIG_PATH = "log4j.properties";

    private static Map<Integer, Trainset> locomotives = new HashMap<>();

    public static void main(String[] args) throws Exception {
        PropertyConfigurator.configure(LOG4J_CONFIG_PATH);
        log.info("System start");

        selectPort();
        BaseStationPowerUtil.turnOnPower();

        // register trains
        CommandUtil.runCommand(new String[]{"reg", "3", "N700A", "n700a-4000.json"});
        CommandUtil.runCommand(new String[]{"reg", "4", "500", "500-4000.json"});
        CommandUtil.runCommand(new String[]{"reg", "5", "E6", "e6-4000.json"});
        CommandUtil.runCommand(new String[]{"reg", "15", "E5", "e5-4000.json"});

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
                System.err.println("Usage: " + args[0] + " " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Type command: ");
        }
    }

    private static void cleanup() throws JmriException {
        BaseStationPowerUtil.turnOffPower();
    }


}
