package re.imc.nps.imcnpsspigot;

import org.bukkit.plugin.java.JavaPlugin;
import re.imc.nps.ClientMain;
import re.imc.nps.config.NpsConfig;

public final class SpigotMain extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        ClientMain.start(getDataFolder().toPath());
        NpsConfig config = ClientMain.getConfig();
        getLogger().info("=======================");
        getLogger().info("房间号: " + config.getRoomId());
        getLogger().info("可输入/jr " + config.getRoomId() + " 进入服务器");
        getLogger().info("=======================");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
