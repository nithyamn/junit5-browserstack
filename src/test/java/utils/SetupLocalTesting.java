package utils;

import com.browserstack.local.Local;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import runners.BstackRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

public class SetupLocalTesting implements BeforeAllCallback, AfterAllCallback {
    Local local;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        BstackRunner runner = new BstackRunner();
        String accesskey = runner.setupCredentials()[1];

        try{
            local = new Local();
            System.out.println("Is local binary already running in the system : "+isLocalRunning());
            if(!isLocalRunning())
            {
                HashMap<String, String> bsLocalArgs = new HashMap<String, String>();
                bsLocalArgs.put("key", accesskey);
                local.start(bsLocalArgs);

                System.out.println("Has local binary started :"+local.isRunning());
            }else
            {
                System.out.println("Local binary is already running!");
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        try {
            local.stop();
            System.out.println("Is local binary running : "+local.isRunning());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean isLocalRunning(){
        boolean flag = false;
        ProcessBuilder processBuilder;
        Process p;
        BufferedReader input;
        String output, cmd;
        if(OSUtils.isMac() || OSUtils.isUnix()){
            try {
                cmd = "ps -ax | grep 'BrowserStack'";
                processBuilder  = new ProcessBuilder("bash", "-c", cmd);
                p = processBuilder.start();
                input = new BufferedReader(new InputStreamReader(p.getInputStream()));

                while ((output = input.readLine()) != null) {
                    if(!output.contains("--key"))
                        flag = false;
                    else
                        flag = true;
                }
                input.close();
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }else{
            //Command for windows
        }
        return flag;
    }
}
