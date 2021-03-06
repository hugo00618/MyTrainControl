package info.hugoyu.mytraincontrol;

import info.hugoyu.mytraincontrol.util.CommandProvider;
import info.hugoyu.mytraincontrol.commandstation.CommandStationRunnable;
import info.hugoyu.mytraincontrol.exception.CommandInvalidUsageException;
import info.hugoyu.mytraincontrol.exception.CommandNotFoundException;
import info.hugoyu.mytraincontrol.trainset.Trainset;
import info.hugoyu.mytraincontrol.util.BaseStationPowerUtil;
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
        CommandProvider.runCommand(new String[]{"reg", "3", "N700A", "n700a-6000.json"});
        CommandProvider.runCommand(new String[]{"reg", "4", "500 Series", "500-4000.json"});
        CommandProvider.runCommand(new String[]{"reg", "5", "E6 Series", "e6.json"});

        // alloc
        CommandProvider.runCommand(new String[]{"alloc", "3", "t1"});

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
            String[] args = line.split(" +");
            try {
                CommandProvider.runCommand(args);
            } catch (CommandNotFoundException e) {
                System.err.println("Command not found");
            } catch (CommandInvalidUsageException e) {
                System.err.println("Usage: " + args[0] + " " + e.getMessage());
            } catch (Exception e) {
                // should not run into this case
            }

            System.out.println("Type command: ");
        }
    }

    private static void cleanup() throws JmriException {
        BaseStationPowerUtil.turnOffPower();
    }


}
