/*
* File Name: Hooks.java
* Copyright 2015, Opex Software
* Apache License, Version 2.0
* This file contains the setup and teardown methods, browser initializations and screenshot functionality
*/

package net.continuumsecurity;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.continuumsecurity.web.drivers.DriverFactory;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriverService;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.gargoylesoftware.htmlunit.BrowserVersion;



public class Hooks {

	public static WebDriver driver;
	private String browser;

	// Contains declaration of all browsers (FF, Chrome, IE, Opera, Android)
	// This method is a hook which runs before every test
	public WebDriver getDriver() throws IOException, URISyntaxException {
		browser = System.getenv("BROWSER");
		if (browser == null) {
			browser = "firefox";
		}
		System.out.println("Browser selected is " + browser);
		if (browser.equalsIgnoreCase("chrome")) {
			
			driver = new ChromeDriver();
			driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
			driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
			driver.manage().window().maximize();
			
//			DriverFactory factory = new DriverFactory();
//			driver = factory.createProxyDriver("chrome");
			
			
		} else if (browser.equalsIgnoreCase("device")) {
			// driver = new ChromeDriver();
			// driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
			// driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
			// driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
			// driver.manage().window().maximize();

			String deviceName = System.getenv("VERSION");
			deviceName = deviceName.replace("_", " ");
			Map<String, String> mobileEmulation = new HashMap<String, String>();
			mobileEmulation.put("deviceName", deviceName);

			Map<String, Object> chromeOptions = new HashMap<String, Object>();
			chromeOptions.put("mobileEmulation", mobileEmulation);
			DesiredCapabilities capabilities = DesiredCapabilities.chrome();
			capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
			driver = new ChromeDriver(capabilities);
			driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
			driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);

		} else if (browser.equalsIgnoreCase("ie")) {
			DesiredCapabilities cap = new DesiredCapabilities();
			cap.setJavascriptEnabled(true);
			cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			driver = new RemoteWebDriver(new URL("http://localhost:5555"),cap);
			driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
			driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
			driver.manage().window().maximize();
		} else if (browser.equalsIgnoreCase("opera")) {
			DesiredCapabilities cap = DesiredCapabilities.operaBlink();
			cap.setBrowserName("opera");
			OperaOptions options = new OperaOptions();
			options.setBinary("/usr/bin/opera");
			options.addArguments("--ignore-certificate-errors");
			cap.setCapability(OperaOptions.CAPABILITY, options);
			OperaDriverService service = new OperaDriverService.Builder()
					.usingDriverExecutable(new File("/usr/local/bin/operadriver"))
					.usingAnyFreePort().build();
			service.start();
			driver = new RemoteWebDriver(service.getUrl(),cap);
			driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
			driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
			driver.manage().window().maximize();
		} else if (browser.equalsIgnoreCase("android")) {
			driver = new RemoteWebDriver(DesiredCapabilities.android());
		} else {
			FirefoxProfile profile = new FirefoxProfile();
			driver = new FirefoxDriver(profile);
			driver.manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
			driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
			driver.manage().window().maximize();
			
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return driver;
	}
	
	
	public static void quitAll() {
		System.out.println("====================Quittng all the Browsers=======================");
        try {
            if (driver != null) driver.quit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	// This method kills the browser after the test is over
	// It also takes a screenshot and embeds it in the report if the test fails
	// This method is a hook which runs after every test
	/*public void afterEach(Scenario scenario) {
		if (scenario.isFailed()) {
			try {
				scenario.write("Current Page URL is " + driver.getCurrentUrl());
				byte[] screenshot = ((TakesScreenshot) driver)
						.getScreenshotAs(OutputType.BYTES);
				scenario.embed(screenshot, "image/png");
			} catch (WebDriverException somePlatformsDontSupportScreenshots) {
				System.err.println(somePlatformsDontSupportScreenshots
						.getMessage());
			}

		}
		if (System.getenv("IS_UPA").equalsIgnoreCase("true")) {
			String scenarioName = scenario.getName().replace(" ", "_");
			File harFolder = new File(System.getProperty("user.dir") + "/target/reports/har/");
			File[] listOfFiles = harFolder.listFiles();
			int cnt = 0;
			ArrayList<String> upaLinks = new ArrayList<String>();
			for (int i = 0; i < listOfFiles.length; i++) {
				File file = listOfFiles[i];
				if (file.getName().contains("+")) {
					String htmlName = scenarioName + "_" + cnt++;
					File newFile = new File(file.getParent(), htmlName + ".har");
					file.renameTo(newFile);
					String simpleHarExec = "simplehar " + newFile.getAbsolutePath() + " " + newFile.getParent()
							+ File.separator + htmlName + ".html";

					try {
						Runtime.getRuntime().exec(simpleHarExec);
					} catch (IOException e) {
						System.out.println("Unable to execute Simplehar");
						e.printStackTrace();
					}
					upaLinks.add(htmlName + ".html");
				}
			}
			UPAResult result = new UPAResult();
			result.setScenarioName(scenarioName);
			result.setUpaReportLinks(upaLinks);
			reporter.addDetails(result);
			reporter.writeResults();
		}
		driver.quit();

	}*/

}