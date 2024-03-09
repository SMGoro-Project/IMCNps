package re.imc.nps.imcnpsspigot;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import re.imc.nps.ClientMain;
import re.imc.nps.Info;
import re.imc.nps.config.NpsConfig;
import re.imc.nps.i18n.LocaleMessage;

public final class SpigotMain extends JavaPlugin {

    @Override
    public void onEnable() {

        ClientMain.setup(getDataFolder().toPath(), Info.Platform.SPIGOT);
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
            if (config == null) {
                return;
            }
            // getLogger().info("=======================");
            getLogger().info(LocaleMessage.message("room_id_tip")
                    .replaceAll("%room_id%", String.valueOf(config.getRoomId())));
            // getLogger().info("=======================");
        });
        ClientMain.start(getDataFolder().toPath(), Info.Platform.SPIGOT, Bukkit.getPort());


    }

    @Override
    public void onDisable() {
        ClientMain.getProcess().stop();
    }
}
