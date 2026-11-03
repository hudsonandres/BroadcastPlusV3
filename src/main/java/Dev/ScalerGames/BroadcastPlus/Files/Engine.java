package Dev.ScalerGames.BroadcastPlus.Files;

import Dev.ScalerGames.BroadcastPlus.Main;
import Dev.ScalerGames.BroadcastPlus.Utils.Messages;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Engine {

    private static FileConfiguration engineConfig = null;
    private static File engineFile = null;

    public static void reloadEngine() {
        if (engineFile == null)
            engineFile = new File(Main.getInstance().getDataFolder(), "engine.yml");

        engineConfig = YamlConfiguration.loadConfiguration(engineFile);

        InputStream defaultStream = Main.getInstance().getResource("engine.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
            engineConfig.setDefaults(defaultConfig);
        }
    }

    public static FileConfiguration getEngineConfig() {
        if (engineConfig == null)
            reloadEngine();

        return engineConfig;
    }

    public static void saveEngine() {
        if (engineConfig == null || engineFile == null)
            return;

        try {
            getEngineConfig().save(engineFile);
        } catch (IOException ex) {
            Messages.logger("&4Could not save engine.yml");
        }
    }

    public static void saveDefaultEngine() {
        if (engineFile == null)
            engineFile = new File(Main.getInstance().getDataFolder(), "engine.yml");

        if (!engineFile.exists()) {
            Main.getInstance().saveResource("engine.yml", false);
        }
    }

}

