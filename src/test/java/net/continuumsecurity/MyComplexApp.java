package net.continuumsecurity;

import net.continuumsecurity.behaviour.INavigable;
import net.continuumsecurity.web.WebApplication;

public class MyComplexApp extends WebApplication implements INavigable {

    public void navigate() {
        driver.get(Config.getInstance().getBaseUrl());
        /*UserPassCredentials creds = new UserPassCredentials(Config.getInstance().getDefaultCredentials());
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(creds.getUsername());
        driver.findElement(By.id("password")).clear();
        driver.findElement(By.id("password")).sendKeys(creds.getPassword());
        driver.findElement(By.name("_action_login")).click();

        //Click on the "tasks" link
        findAndWaitForElement(By.linkText("Tasks")).click();

        //Enter a search query
        driver.findElement(By.id("q")).clear();
        driver.findElement(By.id("q")).sendKeys("test");
        driver.findElement(By.id("search")).click();*/
    }
}