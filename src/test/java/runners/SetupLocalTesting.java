package runners;

import com.browserstack.local.Local;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.HashMap;

public class InitCredsAndLocal implements BeforeAllCallback, AfterAllCallback {
    Local local;


    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        BstackRunner runner = new BstackRunner();
        String accesskey = runner.setupCredentials()[1];

        runner.setupCredentials();
        try{
            String output, flag = null;
            String cmd = "ps -ax | grep 'BrowserStack'";
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", cmd);
            Process p = processBuilder.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            while ((output = input.readLine()) != null) {
                //System.out.println(output);
                if(!output.contains("--key"))
                    flag = "no";
            }

            input.close();
            local = new Local();

            if(flag.equals("no"))
            {
                HashMap<String, String> bsLocalArgs = new HashMap<String, String>();
                bsLocalArgs.put("key", accesskey);
                local.start(bsLocalArgs);

                System.out.println("is running:"+local.isRunning());
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
            System.out.println("is running:"+local.isRunning());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
