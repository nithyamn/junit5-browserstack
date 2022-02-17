package runners;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.extension.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

public class BstackRunner extends InitCredsAndLocal implements TestTemplateInvocationContextProvider {
    JSONObject browserConfig, profileConfig, testConfig, platformConfig, commonCapsConfig;
    HashMap<String, String> finalCapsMap,commonCapsMap;
    WebDriver driver;
    DesiredCapabilities capabilities;
    String displayName;
    @Override
    public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        List<TestTemplateInvocationContext> webDriverTestInvocationContexts = new ArrayList<>();
        String profile = System.getProperty("config");
        System.out.println("Runner");
        try{
            System.out.println("Parallel count: "+System.getProperty("junit.jupiter.execution.parallel.config.fixed.parallelism"));
            testConfig = (JSONObject) mainConfig.get("tests");
            profileConfig = (JSONObject) testConfig.get(profile);
            platformConfig = (JSONObject) profileConfig.get("platform");
            commonCapsConfig = (JSONObject) profileConfig.get("common_caps");
            commonCapsMap = (HashMap<String, String>)commonCapsConfig;

            Iterator platformIterator = platformConfig.keySet().iterator();
            Iterator commonCapsIterator = commonCapsMap.entrySet().iterator();
            capabilities = new DesiredCapabilities();

            while (commonCapsIterator.hasNext()){
                Map.Entry capsName = (Map.Entry) commonCapsIterator.next();
                capabilities.setCapability((String) capsName.getKey(),capsName.getValue());
            }
            while (platformIterator.hasNext()){
                String platformName = (String) platformIterator.next();
                browserConfig = (JSONObject) platformConfig.get(platformName);
                finalCapsMap = (HashMap<String, String>) browserConfig;
                Iterator finalCapsIterator = finalCapsMap.entrySet().iterator();
                while (finalCapsIterator.hasNext()){
                    Map.Entry pair = (Map.Entry) finalCapsIterator.next();
                    capabilities.setCapability((String) pair.getKey(),pair.getValue());
                    displayName = capabilities.getCapability("name")+" "+capabilities.getCapability("browser");
                }

                driver = new RemoteWebDriver(new URL("https://" + username + ":" + accesskey + "@"+server+"/wd/hub"),capabilities);
                webDriverTestInvocationContexts.add(invocationContext(driver));
            }
        }catch (Exception e){
            System.out.println(e);
        }
        return webDriverTestInvocationContexts.stream();
    }

    private TestTemplateInvocationContext invocationContext(WebDriver driver) {
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
                        return parameterContext.getParameter().getType().equals(WebDriver.class);
                    }

                    @Override
                    public Object resolveParameter(ParameterContext parameterContext,
                                                   ExtensionContext extensionContext) {
                        return driver;
                    }
                });
            }
        };
    }
}
