package spidate.main.main;

import java.sql.*;

/**
 * Created by root on 04.08.2017.
 */
public class MySQL {

    private Connection conn;
    private  String hostname;
    private  String user;
    private  String password;
    private  String database;
    private  int port;

    public MySQL()
    {

        hostname = "";
        port = 3306;
        database = "";
        user = "";
        password = "";
        openConnection();

        for(int i = 1; i < 41; i++){
            queryUpdate("CREATE TABLE IF NOT EXISTS resource_" + i + " (LINK varchar(255), AUTHOR varchar(255), VERSION varchar(255), NAME varchar(255))");
        }

        queryUpdate("CREATE TABLE IF NOT EXISTS allResources (LINK varchar(255), AUTHOR varchar(255), VERSION varchar(255), ID varchar(32));");
        queryUpdate("CREATE TABLE IF NOT EXISTS allConns (CONNECTIONS long);");

        queryUpdate("CREATE TABLE IF NOT EXISTS allMembers (ID varchar(255), LINK varchar(255), NAME varchar(255))");

        long outCome = getConnections();

        if(outCome < 0){
            queryUpdate("INSERT INTO allConns (CONNECTIONS) VALUES (0);");
        }

    }

    public  Connection openConnection()
    {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://"+hostname + ":" + port + "/" + database + "?user=" + user + "&password=" + password + "&useUnicode=true&characterEncoding=UTF-8");
            conn = con;

            return conn;
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return conn;
    }


    public long getConnections(){

        try{
            if(!getConnection().isValid(2000)){
                openConnection();
            }

            Connection conn = getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT CONNECTIONS FROM allConns");
            ResultSet rs = st.executeQuery();

            while(rs.next()){
                return rs.getLong("CONNECTIONS");
            }

            return -1;


        }catch(Exception ex){
            ex.printStackTrace();
            return -1;
        }

    }

    public void updateConnections(){
        try{

            if(!getConnection().isValid(2000)){
                openConnection();
            }

            long start = getConnections();
            if(start < 0){
                return;
            }

            queryUpdate("UPDATE allConns SET CONNECTIONS = CONNECTIONS + 1;");

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }



    public  Connection getConnection()
    {
        return conn;
    }
    public  boolean hasConnection()
    {
        try {
            return conn != null || conn.isValid(1);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    public  void closeRessources(ResultSet rs, PreparedStatement st)
    {
        if(rs != null)
        {
            try {
                rs.close();
            } catch (SQLException e) {

            }
        }
        if(st != null)
        {
            try {
                st.close();
            } catch (SQLException e) {

            }
        }
    }


    public  void closeConnection()
    {
        try {
            conn.close();
        } catch (SQLException e) {

            e.printStackTrace();
        }finally
        {
            conn = null;
        }

    }
    public Thread queryUpdate(final String query)
    {

        Thread t = new Thread(){
            public void run(){
                try {
                    if(!getConnection().isValid(2000))
                    {
                        openConnection();
                    }

                    PreparedStatement st = null;

                    Connection conn = getConnection();
                    try {
                        st = conn.prepareStatement(query);
                        st.executeUpdate();
                    } catch (SQLException e) {
                        System.err.println("Failed to send update '" + query + "'.");
                        e.printStackTrace();
                    }finally
                    {
                        closeRessources(null, st);
                        this.stop();
                    }



                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        };

        t.start();

        return t;

    }

    public Thread truncateId(int id){
        try{

            Thread t = queryUpdate("TRUNCATE resource_" + id);

            return t;
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public void updateResource(int id, String link, String author, String ver, String name){



                try{



                    Thread worker = truncateId(id);

                    if(worker != null){
                        worker.join();
                    }

                    queryUpdate("INSERT INTO resource_" + id + "(LINK, AUTHOR, VERSION, NAME) VALUES('" + link + "', '" + author + "', '" + ver + "', '" + name + "');");



                }catch(Exception ex){
                    ex.printStackTrace();
                }



    }

    public void updateAll(String pluginId, String link, String author, String ver){
        //LINK varchar(255), AUTHOR varchar(255), VERSION varchar(255), ID varchar(32)

        Thread t = new Thread(){
            public void run(){

                try{

                    boolean b = false;


                    Connection conn = getConnection();
                    PreparedStatement st = conn.prepareStatement("SELECT * FROM allResources WHERE LINK = '" + link + "';");

                    ResultSet rs = st.executeQuery();

                    b = rs.next();

                   // queryUpdate("SELECT * FROM allResources WHERE LINK = '" + link + "';");




                    if(!b){
                        queryUpdate("INSERT INTO allResources(LINK, AUTHOR, VERSION, ID) VALUES('" + link + "', '" + author + "', '" + ver + "', '" + pluginId + "');");
                    }else{
                        queryUpdate("UPDATE allResources SET VERSION = '" + ver + "' WHERE LINK = '" + link + "';");
                    }


                }catch(Exception ex){
                    ex.printStackTrace();
                }finally {
                    this.stop();
                }

            }
        };

        t.start();

    }


    public String getVersion(String pluginId){
        try{
            String respond = "";

            if(!getConnection().isValid(2000)){
                openConnection();
            }

            Connection conn = getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT VERSION FROM allResources WHERE ID = '" + pluginId + "';");
            ResultSet rs = st.executeQuery();

            while(rs.next()){
                respond = rs.getString("VERSION");
            }

            return respond;

        }catch(Exception ex){
            ex.printStackTrace();
            return "";
        }
    }

    public void addUser(String id, String link, String name){
        try{
            if(!getConnection().isValid(2000)){
                openConnection();
            }
            boolean b;


            Connection conn = getConnection();
            PreparedStatement st = conn.prepareStatement("SELECT * FROM allMembers WHERE ID = '" + id + "';");

            ResultSet rs = st.executeQuery();

            b = rs.next();

            // queryUpdate("SELECT * FROM allResources WHERE LINK = '" + link + "';");




            if(!b){
                queryUpdate("INSERT INTO allMembers (ID, LINK, NAME) VALUES ('" + id + "', '" + link + "', '" + name + "');");
            }


        }catch(Exception ex){
            ex.printStackTrace();
        }
    }





}
