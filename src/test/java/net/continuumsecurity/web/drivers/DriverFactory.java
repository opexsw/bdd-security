/*******************************************************************************
 *    BDD-Security, application security testing framework
 *
 * Copyright (C) `2014 Stephen de Vries`
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see `<http://www.gnu.org/licenses/>`.
 ******************************************************************************/
package net.continuumsecurity.web.drivers;

import net.continuumsecurity.Config;

import org.apache.log4j.Logger;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.opera.OperaDriverService;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DriverFactory {
    private final static String CHROME = "chrome";
    private final static String FIREFOX = "firefox";
    private final static String HTMLUNIT = "htmlunit";
    private final static String OPERA = "opera";
    private final static String IE = "ie";

    private static DriverFactory dm;
    private static WebDriver driver;
    private static WebDriver proxyDriver;
    static Logger log = Logger.getLogger(DriverFactory.class.getName());


    public static DriverFactory getInstance() {
        if (dm == null)
            dm = new DriverFactory();
        return dm;
    }

    public static WebDriver getProxyDriver(String name) {
    	name = System.getenv("BROWSER");
    	System.out.println("==============================" + name + "=======================");
        return getDriver(name, true);
    }

    public static WebDriver getDriver(String name) {
    	
    	name = System.getenv("BROWSER");
    	System.out.println("++++++++++++++++++++++++++++++" + name + "++++++++++++++++++++++++");
        return getDriver(name, false);
    }


    // Return the desired driver and clear all its cookies
    private static WebDriver getDriver(String type, boolean isProxyDriver) {
        WebDriver retVal = getInstance().findOrCreate(type, isProxyDriver);
        try {
            if (!retVal.getCurrentUrl().equals("about:blank")) {
                retVal.manage().deleteAllCookies();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
        return retVal;
    }


    public static void quitAll() {
        log.debug("closing all webDrivers");
        try {
            if (driver != null) driver.quit();
            if (proxyDriver != null) proxyDriver.quit();
        } catch (Exception e) {
            log.error("Error quitting webDriver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
 * Re-use drivers to reduce startup times
 */
    private WebDriver findOrCreate(String type, boolean isProxyDriver) {
        if (isProxyDriver) {
            if (proxyDriver != null) return proxyDriver;
            proxyDriver = createProxyDriver(type);
            return proxyDriver;
        } else {
            if (driver != null) return driver;
            driver = createDriver(type);
            return driver;
        }
    }

    private WebDriver createDriver(String type) {
        if (type.equalsIgnoreCase(CHROME)) return createChromeDriver(new DesiredCapabilities());
        else if (type.equalsIgnoreCase(FIREFOX)) return createFirefoxDriver(null);
        else if (type.equalsIgnoreCase(HTMLUNIT)) return createHtmlUnitDriver(null);
        else if (type.equalsIgnoreCase(OPERA)) return getDriverTestNow();
        else if (type.equalsIgnoreCase(IE)) return getDriverTestNow();
        throw new RuntimeException("Unsupported WebDriver browser: "+type);
    	
//    	return getDriverTestNow();
    }

    private WebDriver createHtmlUnitDriver(DesiredCapabilities capabilities) {
        if (capabilities != null) return new HtmlUnitDriver(capabilities);
        capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        return new HtmlUnitDriver(capabilities);
    	
//    	return getDriverTestNow();
    }

    public WebDriver createProxyDriver(String type) {
        if (type.equalsIgnoreCase(CHROME)) return createChromeDriver(createProxyCapabilities());
        else if (type.equalsIgnoreCase(FIREFOX)) return createFirefoxDriver(createProxyCapabilities());
        else if (type.equalsIgnoreCase(HTMLUNIT)) return createHtmlUnitDriver(createProxyCapabilities());
        else return getDriverTestNow();
//        throw new RuntimeException("Unsupported WebDriver browser: "+type);
    	
//    	return getDriverTestNow();
    }

    public WebDriver createChromeDriver(DesiredCapabilities capabilities) {
        System.setProperty("webdriver.chrome.driver", Config.getInstance().getDefaultDriverPath());
        if (capabilities != null) {
            capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--test-type");
            capabilities.setCapability(ChromeOptions.CAPABILITY,options);
            return new ChromeDriver(capabilities);
        } else return new ChromeDriver();

    }

    public WebDriver createFirefoxDriver(DesiredCapabilities capabilities) {
        if (capabilities != null) {
            return new FirefoxDriver(capabilities);
        }

        ProfilesIni allProfiles = new ProfilesIni();
        FirefoxProfile myProfile = allProfiles.getProfile("WebDriver");
        if (myProfile == null) {
            File ffDir = new File(System.getProperty("user.dir")+ File.separator+"ffProfile");
            if (!ffDir.exists()) {
                ffDir.mkdir();
            }
            myProfile = new FirefoxProfile(ffDir);
        }
        myProfile.setAcceptUntrustedCertificates(true);
        myProfile.setAssumeUntrustedCertificateIssuer(true);
        myProfile.setPreference("webdriver.load.strategy", "unstable");
        if (capabilities == null) {
            capabilities = new DesiredCapabilities();
        }
        capabilities.setCapability(FirefoxDriver.PROFILE, myProfile);
        return new FirefoxDriver(capabilities);
    }

    public DesiredCapabilities createProxyCapabilities() {
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        Proxy proxy = new Proxy();
        proxy.setHttpProxy(Config.getInstance().getProxyHost() + ":" + Config.getInstance().getProxyPort());
        proxy.setSslProxy(Config.getInstance().getProxyHost() + ":" + Config.getInstance().getProxyPort());
        capabilities.setCapability("proxy", proxy);
        return capabilities;
    }
    
    
    public WebDriver getDriverTestNow(){
    	String browser = System.getenv("BROWSER");
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
			try {
				driver = new RemoteWebDriver(new URL("http://localhost:5555"),cap);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			try {
				service.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

}
