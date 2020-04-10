package server;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Server {

    static ArrayList<String> userNames = new ArrayList<>();
    static ArrayList<ClientInfo> clients = new ArrayList<>();
    static ArrayList<PrintWriter> printWriters = new ArrayList<>();


    public static void main(String[] args) throws IOException {

        System.out.println("Waiting for clients ...");
        ServerSocket ss = new ServerSocket(9806);

        while (true) {
            Socket soc = ss.accept();
            System.out.println("Connection established");
            ConversationHandler conversationHandler = new ConversationHandler(soc);
            conversationHandler.start();
        }
    }
}


class ConversationHandler extends Thread {
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    String name;

    PrintWriter pw;
    static FileWriter fw;
    static BufferedWriter bw;

    ClientInfo client;


    public ConversationHandler(Socket socket) throws IOException {
        this.socket = socket;

        fw = new FileWriter("Server-Logs.txt", true);
        bw = new BufferedWriter(fw);
        pw = new PrintWriter(bw, true);
    }

    public void run() {

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            int count = 0;
            while (true) {
                if (count > 0) {
                    out.println("NAME_ALREADY_EXISTS");
                } else {
                    out.println("NAME_REQUIRED");
                }

                name = in.readLine().trim();

                if (name == null)
                    return;

                if (!Server.userNames.contains(name)) {
                    Server.userNames.add(name);
                    System.out.println("List Of Active Users : " + Server.userNames.toString());
                    broadcast("[Server]",name + " has just joined.");
                    break;
                }
                count++;
            }


            out.println("NAME_ACCEPTED:" + name );
            pw.println("-> NEW USER ADDED: " + socket.getRemoteSocketAddress() + ": " + name);

            String[] arr = socket.getRemoteSocketAddress().toString().replace("/", "").split(":");
            String remoteSocketIp = arr[0].trim();
            int remoteSocketPort = Integer.parseInt(arr[1].trim());
            client = new ClientInfo(name, remoteSocketIp, remoteSocketPort);
            Server.clients.add(client);
            Server.printWriters.add(out);

            while (true) {
                String message = in.readLine();
                if (message == null)
                    return;

                if (message.startsWith("LEAVING:")) {
                    performLeavingClientProcedures(message);
                    break;
                } else if (message.trim().toUpperCase().startsWith("WEATHER")) {
                    String city = message.trim().toUpperCase().substring("WEATHER".length()).trim();
                    city = city.length() == 0 ? "Kansas City": city;
                    System.out.println("city = " + city);
                    try {
                        String forecast5day = WeatherForecast.reportForecast(city);
                        pw.println(forecast5day);
                        System.out.println(forecast5day);
                        message = forecast5day;
                    } catch (Exception e) {
                        e.printStackTrace();
                        message = "";
                    }
                }
                else if (message.trim().toLowerCase().startsWith("exit")){
                    performLeavingClientProcedures(message);
                    pw.println("->>  Server is stopped by " + client.getName() + "  at "+ LocalDateTime.now());
                    System.out.println("->>  Server is stopped by " + client.getName()+ "  at "+ LocalDateTime.now());


                    System.exit(0);
                }

                // Logging
                pw.println(name + "-" + socket.getRemoteSocketAddress().toString() + " : " + message);

                // disseminating to all users
                broadcast(client.getName(), message);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void performLeavingClientProcedures(String message) throws IOException {
        System.out.println(client.getName() + " has left");
        pw.println("->>     USER LEFT :" + client.getName() + "  at "+ LocalDateTime.now() );

        Server.printWriters.remove(out);
        Server.userNames.remove(client.getName());
        Server.clients.remove(client);
        System.out.println("Number of remaining chatters : " + Server.userNames.size());
        socket.close();

    }


    private void broadcast(String name, String message) {
        for (PrintWriter writer : Server.printWriters) {
            writer.println(name + " >> " + message);
        }
    }

} //end of ConversationHandler

class ClientInfo {

    private String name;

    public ClientInfo(String name, String ipAddress, int port) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private String ipAddress;
    private int port;

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", port=" + port +
                '}';
    }
}