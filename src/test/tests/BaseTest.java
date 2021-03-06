package test.tests;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class BaseTest {
    AndroidDriver driver;

    private static DesiredCapabilities createCapabilities(String value) throws FileNotFoundException {
        FileReader file = new FileReader("src/test/config/platforms.yml");
        Map<String, Object> platforms = new Yaml().load(file);

        Map<String, Object> platform = (Map<String, Object>) platforms.get(value);
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("platformName", platform.get("platformName"));
        capabilities.setCapability("platformVersion", platform.get("platformVersion"));
        capabilities.setCapability("deviceName", platform.get("deviceName"));

        if (platform.get("browserName") == null) {
            capabilities.setCapability("app", platform.get("app"));
            capabilities.setCapability("appWaitActivity", platform.get("appWaitActivity"));
        } else {
            capabilities.setCapability("browserName", platform.get("browserName"));
        }

        return capabilities;
    }

    @BeforeMethod
    public void setup(Method method) throws MalformedURLException, FileNotFoundException {
        String url;
        DesiredCapabilities capabilities;

        String platformProperty = System.getProperty("PLATFORM");

        if (System.getProperty("USE_SAUCE") != null) {
            String platform = (platformProperty != null) ? platformProperty : "androidChromeSauce";
            capabilities = createCapabilities(platform);

            String USER = System.getenv("SAUCE_USERNAME");
            String KEY = System.getenv("SAUCE_ACCESS_KEY");

            capabilities.setCapability("name", method.getName());
            String buildEnv = System.getenv("BUILD_TAG");
            if (buildEnv != null) {
                capabilities.setCapability("build", buildEnv);
            }

            url = "https://" + USER + ":" + KEY + "@ondemand.saucelabs.com/wd/hub";
        } else {
            String platform = (platformProperty != null) ? platformProperty : "androidApp";
            capabilities = createCapabilities(platform);
            url = "http://localhost:4723/wd/hub";
        }

        driver = new AndroidDriver<>(new URL(url), capabilities);
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        String status = result.isSuccess() ? "passed" : "failed";

        if (System.getProperty("USE_SAUCE") == null) {
            System.out.println(status);
        } else {
            JavascriptExecutor js = driver;
            js.executeScript("sauce:job-result=" + status);
        }

        driver.quit();
    }

}