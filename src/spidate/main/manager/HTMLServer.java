package spidate.main.manager;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import spidate.main.main.MySQL;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by root on 05.08.2017.
 */
public class HTMLServer {


    private HttpServer server;

    private static MySQL sql;

    public HTMLServer(MySQL sql) throws Exception {
        this.sql = sql;
        startServer();
    }

    public void startServer(){


        if(server != null){
            System.out.println("Server already running.");

            return;
        }
        try{
            server = HttpServer.create(new InetSocketAddress(80), 0);
            server.createContext("/", new MyHandler());
            server.start();
        }catch(Exception ex){
            ex.printStackTrace();
        }


        System.out.println("Started Server.");

    }

    public void stopServer(){

        if(server != null){
            server.stop(1);
        }

        server = null;

        System.out.println("Server stopped.");


    }

    public void restartServer(){


        if(server != null){
            server.stop(1);
        }

        startServer();
    }

    public String requestVersion(String id){
        try{
            String url = "http://localhost/?=" + id;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestProperty("User-Agent", "SPIDATE-USER");
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.flush();
            wr.close();


            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            String yourVersion = response.toString();

            return yourVersion;

            /*
            Thanks for using our Service.
            This was and will be a spare time open-source project.
            We're aiming to improve our skills at every corner of programming.
            If you want a reliable way of getting your plugin version, try https://spiget.org
            This project will most likely be abandoned in some month.
            Everybody can host it by himself, you just need some dependencies.
            Look that up on github.com/simplec0de/sos
             */


        }catch(Exception ex){
            ex.printStackTrace();
            return "not available";
        }

    }



    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
        try{
            String response;

            if(!t.getRequestHeaders().get("User-Agent").toString().equalsIgnoreCase("[SPIDATE-USER]")){
                response = "WRONG HTTPAGENT - " + t.getRequestHeaders().get("User-Agent").toString();
                t.sendResponseHeaders(200, response.length());
                //System.out.println("Sending response: " + response);
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            String[] requestUri = t.getRequestURI().toString().split("=");

            String pluginId = requestUri[1];
            pluginId = pluginId.replace("?", "");

            String version = sql.getVersion(pluginId);

            if(version.equalsIgnoreCase("") || version.isEmpty()){
                response = "404 \n" +
                        "NOT IN DATABASE";
            }else{
                response = version;
            }

            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }catch(Exception ex){
             ex.printStackTrace();
        }


        }
    }

}
