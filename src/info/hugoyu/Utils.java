package info.hugoyu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static Utils instance;

    private static final String PATH_ALIASES = "data/aliases";
    private static final String PATH_PROFILES = "data/profiles";

    private Map<Integer, String> locoAliases = new HashMap<>();
    private Map<Integer, Map<String, String>> locoProfiles = new HashMap<>();

    private Utils() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(PATH_ALIASES));

            String line;
            while ((line = br.readLine()) != null) {
                String[] args = line.split(" ");
                locoAliases.put(Integer.parseInt(args[0]), args[1]);
            }

            br = new BufferedReader(new FileReader(PATH_PROFILES));
            while ((line = br.readLine()) != null) {
                String[] args = line.split(" ");
                int addr = Integer.parseInt(args[0]);
                if (!locoProfiles.containsKey(addr)) locoProfiles.put(addr, new HashMap<>());
                locoProfiles.get(addr).put(args[1], args[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Utils getInstance() {
        if (instance == null) instance = new Utils();
        return instance;
    }

    public String getLocoAlias(int addr) {
        return locoAliases.get(addr);
    }

    public String getLocoProfilePath(int addr, String bandai) {
        return locoProfiles.get(addr).get(bandai);
    }
}
