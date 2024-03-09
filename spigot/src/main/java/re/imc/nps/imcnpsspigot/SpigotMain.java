package re.imc.nps.imcnpsspigot;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import re.imc.nps.ClientMain;
import re.imc.nps.Info;
import re.imc.nps.config.NpsConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class SpigotMain extends JavaPlugin {

    @Override
    public void onEnable() {

        /*
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            if (!getServer().spigot().getConfig().getBoolean("settings.bungeecord", false)) {
                getLogger().severe("------------------------------------------------------------");
                getLogger().severe("byd 你应该去spigot.yml");
                getLogger().severe("把bungeecord打开");
                getLogger().severe("");
                getLogger().severe("bungeecord: false --→ bungeecord: true");
                getLogger().severe("------------------------------------------------------------");
                getServer().shutdown();
            }
        }, 5, TimeUnit.SECONDS);

         */
        // Plugin startup logic

        ClientMain.setLogHandler(s -> getLogger().info(s));

        ClientMain.setStartHandler(npsProcess -> {
             NpsConfig config = ClientMain.getConfig();
            if (config == null) return;
            getLogger().info("=======================");
            getLogger().info("房间号: " + config.getRoomId());
            getLogger().info("可输入/jr " + config.getRoomId() + " 进入服务器");
            getLogger().info("=======================");
        });
        ClientMain.start(getDataFolder().toPath(), Info.Platform.SPIGOT);


    }

    @Override
    public void onDisable() {
        ClientMain.getProcess().stop();
    }
}
