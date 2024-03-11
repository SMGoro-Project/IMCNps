package re.imc.nps;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import re.imc.nps.i18n.LocaleMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class StandaloneMain {

    private static ComponentLogger logger = ComponentLogger.logger("IMCNps");

    public static void main(String[] args) {
        try {
            ClientMain.TOKEN = args[0];
        } catch (Throwable throwable) {
        }

        ClientMain.setup((new File(System.getProperty("user.dir"))).toPath(), Info.Platform.STANDALONE);

        ClientMain.setOutHandler(System.out::println);
        ClientMain.setLogHandler(StandaloneMain::info);
        ClientMain.setStartHandler((process) -> {
            if (ClientMain.getConfig() != null) {
                // info("=======================");
                info(LocaleMessage.message("room_id_tip")
                        .replaceAll("%room_id%", String.valueOf(ClientMain.getConfig().getRoomId())));
                // info("=======================");
            }
        });
        setToken((new File(System.getProperty("user.dir"))).toPath());
        ClientMain.start((new File(System.getProperty("user.dir"))).toPath(), Info.Platform.STANDALONE, 25565);
        registerDaemonThread();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void info(String msg) {
        logger.info(LegacyComponentSerializer.legacySection().deserialize(msg));

    }
    public static void setToken(Path path) {
        String token = System.getProperty("nps.accesstoken", null);
        if (token == null) {
            Path file = path.resolve("token.txt");
            if (!file.toFile().exists()) {
                info(LocaleMessage.message("not_found_token"));

                try {
                    InputStream in = ClientMain.class.getClassLoader().getResourceAsStream("token.txt");
                    Files.copy(in, file);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                Scanner scanner = new Scanner(System.in);
                String line = null;

                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    if (line != null) {
                        scanner.close();
                        break;
                    }
                }

                try {
                    if (line != null) {
                        Files.write(file, line.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void registerDaemonThread() {
        Thread thread = new Thread(() -> {
            try {
                ClientMain.registerCloseHook();

                while (true) {
                    Thread.sleep(1000L);
                    System.gc();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
