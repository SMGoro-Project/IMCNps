package re.imc.nps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Scanner;

public class StandaloneMain {
    public StandaloneMain() {
    }

    public static void main(String[] args) {
        try {
            ClientMain.TOKEN = args[0];
        } catch (Throwable throwable) {
        }

        ClientMain.setOutHandler(System.out::println);
        ClientMain.setLogHandler(System.out::println);
        ClientMain.setStartHandler((process) -> {
            if (ClientMain.getConfig() != null) {
                System.out.println("=======================");
                System.out.println("房间号: " + ClientMain.getConfig().getRoomId());
                System.out.println("可输入/jr " + ClientMain.getConfig().getRoomId() + " 进入服务器");
                System.out.println("=======================");
            }
        });
        setToken((new File(System.getProperty("user.dir"))).toPath());
        ClientMain.start((new File(System.getProperty("user.dir"))).toPath());
        registerDaemonThread();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setToken(Path path) {
        String token = System.getProperty("nps.accesstoken", (String)null);
        if (token == null) {
            Path file = path.resolve("token.txt");
            if (!file.toFile().exists()) {
                System.out.println("未发现Token! 请在这里填写你的Token");

                try {
                    InputStream in = ClientMain.class.getClassLoader().getResourceAsStream("token.txt");
                    Files.copy(in, file, new CopyOption[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                Scanner scanner = new Scanner(System.in);
                String line = null;

                while(scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    if (line != null) {
                        scanner.close();
                        break;
                    }
                }

                try {
                    if (line != null) {
                        Files.write(file, line.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
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

                while(true) {
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
