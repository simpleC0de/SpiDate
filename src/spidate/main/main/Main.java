package spidate.main.main;

import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaDriverService;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Created by root on 02.08.2017.
 */
public class Main {

    private static WebDriver driver;


    private static HashMap<String, List<String>> resources = new HashMap<>();



    private static MySQL sql;

    public static void main(String[] args) {
        sql = new MySQL();

        init();
        // getAllMembers();


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
            try{
                driver.navigate().to("https://google.de");
            }catch(Exception ex){
                init();
            }
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
                String perc = format.format(((double)urls.size() / 28000) * 100);
                System.out.println("Number of Links : " + perc + "%");
            }
            System.out.println("Current page : " + s);

            try{

                if(s % 6 == 0){
                    System.out.println("Starting to write to file...");

                    FileWriter writer = new FileWriter("C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\allResources.txt");

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

                    // driver.quit();

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

    public static void getAllMembers(){
        Thread t = new Thread(){
            public void run(){
                Main.init();
                String url = "https://www.spigotmc.org/members/";
                for(int i = 1; i < 12500; i++){






                    if(i % 24 == 0){
                        System.setProperty("webdriver.opera.driver", "C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\driver\\od.exe");

                        if(driver != null){
                            Main.init();
                        }


                    }

                    driver.navigate().to(url + i);
                    WebElement userName;
                    try{
                        userName = driver.findElement(By.xpath("//meta[@property='og:title']"));
                    }catch(Exception ex){
                        continue;
                    }

                    System.out.println("Username - " + userName.getAttribute("content"));

                    String uri = driver.getCurrentUrl();

                    sql.addUser("" + i, uri, userName.getAttribute("content"));





                }


            }
        };

        t.start();
    }




    private static HashMap<String, String> versions = new HashMap<>();

    public static void getVersionsofList(){

        //init();

        File f = new File("C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\allResources.txt");
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

            WebElement verlement = element.findElement(By.xpath("//span[@class='muted']"));

            WebElement name = element.findElement(By.tagName("h1"));

            String textName = name.getText();



            List<String> informations = new ArrayList<>();


            informations.add(verlement.getText());


            String uri = urls.get(i);
            String[] split = uri.split("/");
            String nameAndVer = split[4];

            String[] splitVer = nameAndVer.split("\\.");

            String version = splitVer[1];


            informations.add(version);

            resources.put(urls.get(i), informations);


            WebElement authElement = driver.findElement(By.className("author"));

            //authElement = authElement.findElement(By.cssSelector(".dd"));


            String author = authElement.getText();

            String[] splitAuth = author.split(":");



            splitAuth[1] = splitAuth[1].replace("\n", "");


            textName = textName.replace(informations.get(0), "");

            sql.updateAll(informations.get(1), uri, splitAuth[1], informations.get(0));
            iduplicated = i+1;
            try{
                System.out.println("duplic - " + iduplicated);
                sql.updateResource(iduplicated, uri, splitAuth[1], informations.get(0), textName);

            }catch(Exception ex){

            }



            System.out.println("Ver - " + informations.get(0) + "       PluginID - " + informations.get(1) + "     Author - " + splitAuth[1] + "     Resource - " + textName);

            if(i % 12 == 0 && i != 0){
                init();
            }


        }
        saveVersionstoFile();
        emptyLinkFile();

        //driver.quit();

    }

    public static void emptyLinkFile(){
        try{

            File f = new File("C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\allResources.txt");
            FileWriter fileWriter = new FileWriter(f);
            fileWriter.write("");
            fileWriter.flush();
            fileWriter.close();
        }catch(Exception ex){
            ex.printStackTrace();
        }
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

        ConsoleOutputCapturer cs = new ConsoleOutputCapturer();
        cs.start();
        if(driver != null){
            try{





                // driver.quit();
                /*
                Use driver.quit(); on Windows 7 and higher
                 */
                String childWindow = "";
                String homeWindow = driver.getWindowHandle();
                Set<String> allWindows = driver.getWindowHandles();

                //Use Iterator to iterate over windows
                Iterator<String> windowIterator =  allWindows.iterator();

                //Verify next window is available
                while(windowIterator.hasNext())
                {
                    //Store the Recruiter window id
                    childWindow = windowIterator.next();
                }

                //Here we will compare if parent window is not equal to child window
                if (homeWindow.equals(childWindow))
                {
                    driver.switchTo().window(childWindow);
                    Robot robot = new Robot();
                    robot.keyPress(KeyEvent.VK_ALT);
                    robot.keyPress(KeyEvent.VK_F4);
                }





            }catch(Exception ex){
                ex.printStackTrace();
            }
        }



        System.setProperty("webdriver.opera.logfile", "C:\\Users\\" + System.getProperty("user.name") + "\\Desktop\\server.log");
        System.out.println("Starting driver...");
        OperaOptions op = new OperaOptions();
        op.setBinary("C:\\Program Files\\Opera\\46.0.2597.57\\opera.exe");
        DesiredCapabilities cap = new DesiredCapabilities();
        OperaDriverService service = null;

        driver = new OperaDriver();
        System.out.println("Started driver.");



    }

    private static int i = 1;

}
