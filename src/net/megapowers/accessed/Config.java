package net.megapowers.accessed;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import org.bukkit.util.config.Configuration;

public class Config {

    public static final String PLUGIN_NAME = "Accessed";
    public static final String CONFIG_LOCATION = "plugins" + File.separator + PLUGIN_NAME + File.separator + "config.yml";
    private Configuration config;
    private Map<String, Object> configMap;

    public Config() {
        File file = new File(CONFIG_LOCATION);
        config = new Configuration(file);
        reload();
    }

    public final void reload() {
        config.load();
        configMap = config.getAll();
    }

    public Object getConfig(String key) {
        if (configMap.containsKey(key)) {
            return configMap.get(key);
        }
        return null;
    }

    public ArrayList getConfig(String[] key) {
        ArrayList out = new ArrayList();
        for (String k : key) {
            Object value = getConfig(k);
            if (value != null) {
                out.add(value);
            }
        }
        return out;
    }
}
