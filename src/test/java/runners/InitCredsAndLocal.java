package runners;

import com.browserstack.local.Local;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.FileReader;
import java.util.HashMap;

public class InitCredsAndLocal implements BeforeAllCallback, AfterAllCallback {
    Local local;
    public JSONObject mainConfig;
    JSONParser parse;
    public String server, username, accesskey;

    public void setupCredentials(){
        try{
            parse = new JSONParser();
            mainConfig = (JSONObject) parse.parse(new FileReader("src/test/resources/caps.json"));
            server = (String) mainConfig.get("server");
            username = System.getenv("BROWSERSTACK_USERNAME");
            if(username == null){
                username = (String) mainConfig.get("username");
            }
            accesskey = System.getenv("BROWSERSTACK_ACCESS_KEY");
            if(accesskey == null){
                accesskey = (String) mainConfig.get("accesskey");
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {

        setupCredentials();

        try{
            local = new Local();
            HashMap<String, String> bsLocalArgs = new HashMap<String, String>();
            bsLocalArgs.put("key", accesskey);
            local.start(bsLocalArgs);

            System.out.println("is running:"+local.isRunning());
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
