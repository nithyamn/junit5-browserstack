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
    HashMap<String, String> allCaps,commonCapsMap;
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
        List<TestTemplateInvocationContext> webDriverTestInvocationContexts = new ArrayList<>();
        String profile = System.getProperty("config");

        try{
            testConfig = (JSONObject) mainConfig.get("tests");
            profileConfig = (JSONObject) testConfig.get(profile);
            platformConfig = (JSONObject) profileConfig.get("platform");
            commonCapsConfig = (JSONObject) profileConfig.get("common_caps");
            commonCapsMap = (HashMap<String, String>)commonCapsConfig;
            Iterator platformIterator = platformConfig.keySet().iterator();

            while (platformIterator.hasNext()){
                Iterator commonCapsIterator = commonCapsMap.entrySet().iterator();
                capabilities = new DesiredCapabilities();
                while (commonCapsIterator.hasNext()){
                    Map.Entry capsName = (Map.Entry) commonCapsIterator.next();
                    capabilities.setCapability((String) capsName.getKey(),capsName.getValue());
                }
                String platformName = (String) platformIterator.next();
                browserConfig = (JSONObject) platformConfig.get(platformName);
                allCaps = (HashMap<String, String>) browserConfig;
                Iterator finalCapsIterator = allCaps.entrySet().iterator();
                while (finalCapsIterator.hasNext()){
                    Map.Entry pair = (Map.Entry) finalCapsIterator.next();
                    capabilities.setCapability((String) pair.getKey(),pair.getValue());
                    displayName = capabilities.getCapability("name")+" "+capabilities.getCapability("browser");
                }
                webDriverTestInvocationContexts.add(invocationContext(capabilities));

            }
        }catch (Exception e){
            System.out.println(e);
        }
        return webDriverTestInvocationContexts.stream();
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
