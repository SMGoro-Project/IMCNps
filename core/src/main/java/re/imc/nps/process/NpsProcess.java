package re.imc.nps.process;

import lombok.Getter;
import re.imc.nps.ClientMain;
import re.imc.nps.ErrorInfo;
import re.imc.nps.Info;
import re.imc.nps.config.NpsConfig;
import re.imc.nps.i18n.LocaleMessage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;

@Getter
public class NpsProcess {

    private final String npsPath;
    private final Info.SystemType systemType;
    private Process process;
    private Thread executeThread;
    private Thread readThread;
    private NpsConfig config;

    @Getter
    private boolean stop = false;

    public NpsProcess(String npsPath, Info.SystemType systemType, NpsConfig config) {
        this.npsPath = npsPath;
        this.systemType = systemType;
        this.config = config;
    }

    public void start() {
        executeThread = new Thread(() -> {
            try {
                Runtime run = Runtime.getRuntime();
                if (systemType == Info.SystemType.WINDOWS) {
                    process = run.exec("taskkill /im npc.exe /f");
                    BufferedReader output = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    output.readLine();
                    output.close();
                    process = run.exec(npsPath + "/npc.exe -server=" + config.getNpsUrl() + " -vkey=" + config.getKey());
                }
                if (systemType == Info.SystemType.LINUX) {
                    process = run.exec(npsPath + "/npc -server=" + config.getNpsUrl() + " -vkey=" + config.getKey());
                }
                startRead();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Executors.newSingleThreadScheduledExecutor().execute(executeThread::start);

    }

    public void startRead() {
        readThread = new Thread(this::readNps);
        Executors.newSingleThreadScheduledExecutor().execute(readThread::start);
    }

    public void readNps() {
        InputStream inputStream = process.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader out = new BufferedReader(inputStreamReader);
        String info;
        while (true) {
            if (!process.isAlive()) {
                if (stop()) {
                    ClientMain.getNpsLogger().logInfo(LocaleMessage.message("check_disconnect_reconnect"));
                    ClientMain.start(ClientMain.DATA_PATH);
                    return;
                }
            }
            try {
                info = out.readLine();
                if (info == null) {
                    continue;
                }
                
                ClientMain.getNpsLogger().logNpsProcess(info);
                
                if (info.contains(ErrorInfo.CLIENT_CLOSE) || info.contains(ErrorInfo.CONNECT_FAILED)) {
                    if (stop()) {
                        ClientMain.getNpsLogger().logInfo(LocaleMessage.message("check_disconnect_reconnect"));
                        ClientMain.start(ClientMain.DATA_PATH);
                    }
                }
            } catch (Exception e) {
                ClientMain.getNpsLogger().logInfo(e.getMessage());
            }
        }
    }

    public boolean stop() {
        if (stop) {
            return false;
        }
        stop = true;
        executeThread.interrupt();
        process.destroy();
        return true;
    }
}
