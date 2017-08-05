package spidate.main.main;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import spidate.main.manager.HTMLServer;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Created by root on 02.08.2017.
 */
public class Main {

    private static WebDriver driver;
    private static long start, end;


    private static HashMap<String, List<String>> resources = new HashMap<>();

    private static HTMLServer server;


    private static MySQL sql;

    public static void main(String[] args) {
        sql = new spidate.main.main.MySQL();

        try{
            server = new HTMLServer(sql);
        }catch(Exception ex){
            ex.printStackTrace();
        }


        Timer t = new Timer();

        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                getVersionsofList();

            }
        }, 1000, (1000*60) * 12);


    }





    static DecimalFormat format = new DecimalFormat("###.##");


    public static List<String> getLatestResourceIDs(){



        List<String> urls = new ArrayList<>();

        int ss = 0;

        for(int s = 0; s < 15; s++){
            ss++;
            driver.navigate().to("https://www.spigotmc.org/resources/?page=" + s);

            List<WebElement> allResources = driver.findElements(By.className("resourceList"));
            WebElement manageableElemt;
            List<WebElement> allLinks = new ArrayList<>();
            for (WebElement element: allResources) {

                manageableElemt = element;

                allLinks = manageableElemt.findElements(By.className("title"));

            }

            for(int i = 0; i < allLinks.size(); i++){
                urls.add(allLinks.get(i).findElement(By.cssSelector("a")).getAttribute("href"));
                String perc = format.format(((double)urls.size() / 300) * 100);
                System.out.println("Number of Links : " + perc + "%");
            }
            System.out.println("Current page : " + s);

            try{

                if(s % 6 == 0){
                    System.out.println("Starting to write to file...");

                    FileWriter writer = new FileWriter("C:\\Users\\root\\Desktop\\allResources.txt");

                    writer.write("");

                    for(int write = 0; write < urls.size(); write++){
                        writer.write(urls.get(write) + "\n");
                        System.out.println("Wrote " + write + " urls to file...");
                    }
                    writer.close();

                    System.out.println("Finished writing!");

                }

                if(s % 12 == 0 && s != 0){
                    System.out.println("Restarting driver...");

                    driver.close();
                    driver.quit();

                    init();

                    System.out.println("Restarted driver successfully!");
                }

            }catch(Exception ex){
                ex.printStackTrace();
            }



        }

        System.out.println("Last site : " + ss + " Resource Links : " + urls.size());


        getVersionsofList();


        return urls;

    }



    private static HashMap<String, String> versions = new HashMap<>();

    public static void getVersionsofList(){

        File f = new File("C:\\Users\\root\\Desktop\\allResources.txt");
        String everything = "";
        try(
                FileInputStream inputStream = new FileInputStream(f)) {
                everything = IOUtils.toString(inputStream);
        }catch(Exception ex){

        }

        if(everything.equalsIgnoreCase("") || everything.isEmpty()){
            getLatestResourceIDs();
            return;
        }


        System.out.println(everything);

        List<String> urls;
        String[] splitUp = everything.split("\n");

        urls = Arrays.asList(splitUp);
        int iduplicated = 0;
        for(int i = 0; i < urls.size(); i++){

            driver.navigate().to(urls.get(i));
            WebElement element;

            try{
                element = driver.findElement(By.className("resourceInfo"));
            }catch(Exception ex){
                continue;
            }

            element = element.findElement(By.xpath("//span[@class='muted']"));


            List<String> informations = new ArrayList<>();


            informations.add(element.getText());
            String uri = urls.get(i);
            String[] split = uri.split("/");
            String nameAndVer = split[4];

            String[] splitVer = nameAndVer.split("\\.");

            String version = splitVer[1];


            informations.add(version);

            resources.put(urls.get(i), informations);



            iduplicated = iduplicated+1;
            if(iduplicated < 27){
                sql.updateResource(iduplicated, uri, "", informations.get(0));
            }

            sql.updateAll(informations.get(1), uri, "", informations.get(0));

            System.out.println("Ver - " + informations.get(0) + "       PluginID - " + informations.get(1));


            if(i % 12 == 0 && i != 0){
                init();
            }


        }
        saveVersionstoFile();

        driver.quit();


        end = System.currentTimeMillis();
    }

    public static void saveVersionstoFile() {
        Iterator it = versions.entrySet().iterator();
        String valueToWrite = "";
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());

            if (valueToWrite.equalsIgnoreCase("")) {
                valueToWrite = "" + pair.getKey() + " +~+ " + pair.getValue() + "\n";
            } else {
                valueToWrite = valueToWrite + pair.getKey() + " +~+ " + pair.getValue() + "\n";
            }

        }

        try {
            System.out.println("Starting to write to file...");

            FileWriter writer = new FileWriter("C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\allVersions.txt");

            writer.write("");
            writer.write(valueToWrite);

            writer.close();

            System.out.println("Finished writing!");
        } catch (Exception ex) {

        }

    }


    public static void init(){

        System.setProperty("webdriver.opera.driver", "C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\driver\\od.exe");

        if(driver != null){
            driver.quit();
        }

        System.out.println("Starting driver...");
        driver = new OperaDriver();
        System.out.println("Started driver.");

    }

}
