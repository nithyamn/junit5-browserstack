package runners;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.extension.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.FileReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

public class BstackRunner implements TestTemplateInvocationContextProvider {
    JSONObject mainConfig, browserConfig, profileConfig, testConfig, platformConfig, commonCapsConfig;
    HashMap<String, String> allCapsMap,commonCapsMap,bstackOptions, bstackOptionsCommonCaps,bstackOptionsPlatform;
    WebDriver driver;
    DesiredCapabilities capabilities;
    String displayName, username, accesskey, server;

    public String[] setupCredentials(){
        String server = null, username = null, accesskey = null;
        try{
            JSONParser parse = new JSONParser();
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
        return (new String[]{username,accesskey,server});
    }

    public BstackRunner(){
       this.username = setupCredentials()[0];
       this.accesskey = setupCredentials()[1];
       this.server = setupCredentials()[2];
    }

    public WebDriver setupWebDriver(DesiredCapabilities capabilities){
        try{
            driver = new RemoteWebDriver(new URL("https://" + username + ":" + accesskey + "@"+server+"/wd/hub"),capabilities);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return driver;
    }
    @Override
    public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        List<TestTemplateInvocationContext> desiredCapsInvocationContexts = new ArrayList<>();
        String profile = System.getProperty("config");
        displayName = profile;
        try{
            testConfig = (JSONObject) mainConfig.get("tests");
            profileConfig = (JSONObject) testConfig.get(profile);
            platformConfig = (JSONObject) profileConfig.get("platform");
            commonCapsConfig = (JSONObject) profileConfig.get("common_caps");
            commonCapsMap = (HashMap<String, String>)commonCapsConfig;

            Iterator platformIterator = platformConfig.keySet().iterator();
            while (platformIterator.hasNext()){

                capabilities = new DesiredCapabilities();
                Iterator commonCapsIterator = commonCapsMap.entrySet().iterator();
                while (commonCapsIterator.hasNext()){
                    Map.Entry capsName = (Map.Entry) commonCapsIterator.next();
                    if(capsName.getKey().equals("bstack:options")){
                        bstackOptionsCommonCaps = (HashMap<String, String>)commonCapsConfig.get("bstack:options");
                    }else{
                        capabilities.setCapability((String) capsName.getKey(),capsName.getValue());
                    }
                }

                String platformType = (String) platformIterator.next();
                browserConfig = (JSONObject) platformConfig.get(platformType);
                allCapsMap = (HashMap<String, String>) browserConfig;

                Iterator finalCapsIterator = allCapsMap.entrySet().iterator();
                while (finalCapsIterator.hasNext()){
                    Map.Entry platformName = (Map.Entry) finalCapsIterator.next();
                    if(platformName.getKey().equals("bstack:options")){
                        bstackOptionsPlatform = (HashMap<String, String>) browserConfig.get("bstack:options");
                    }else{
                        capabilities.setCapability((String) platformName.getKey(),platformName.getValue());
                    }
                }
                bstackOptions = new HashMap<>();
                bstackOptions.putAll(bstackOptionsCommonCaps);
                bstackOptions.putAll(bstackOptionsPlatform);
                displayName = capabilities.getCapability("sessionName")+" "+capabilities.getCapability("browserName");
                capabilities.setCapability("bstack:options",bstackOptions);
                desiredCapsInvocationContexts.add(invocationContext(capabilities));
            }
        }catch (Exception e){
            System.out.println(e);
        }
        return desiredCapsInvocationContexts.stream();
    }

    private TestTemplateInvocationContext invocationContext(DesiredCapabilities caps) {
        return new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return displayName;
            }

            @Override
            public List<Extension> getAdditionalExtensions() {

                return Collections.singletonList(new ParameterResolver() {
                    @Override
                    public boolean supportsParameter(ParameterContext parameterContext,
                                                     ExtensionContext extensionContext) {
                        return parameterContext.getParameter().getType().equals(DesiredCapabilities.class);
                    }

                    @Override
                    public Object resolveParameter(ParameterContext parameterContext,
                                                   ExtensionContext extensionContext) {
                        return caps;
                    }
                });
            }
        };
    }
}
