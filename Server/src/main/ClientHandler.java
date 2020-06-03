package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private Server server;
    private ObjectOutputStream outMessage;
    private BufferedReader bufferedReader;
    private ObjectInputStream inMessage;

    private boolean isClose = false;

    private Socket clientSocket = null;

    private static int clients_count = 0;


    public ClientHandler(Socket socket, Server server) {
        try {
            clients_count++;
            this.server = server;
            this.clientSocket = socket;
            this.outMessage = new ObjectOutputStream(socket.getOutputStream());
            this.inMessage = new ObjectInputStream(socket.getInputStream());
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException ex) {
            System.out.println("Couldn't connect to server.");
        }
    }

    @Override
    public void run() {
        try {
            if (!isClose && !clientSocket.isClosed()) {
                while (true) {
                    server.sendMessageToAllClients("New player entered.", server.gamesArray);
                    server.sendMessageToAllClients("lobbies", server.gamesArray);
                    break;
                }
            }

            while (true) {
                if (!isClose && inMessage != null && outMessage != null && !clientSocket.isClosed()) {
                    Message clientMessage = (Message) inMessage.readObject();
                    if (clientMessage.msg.equals("end")) {
                        System.out.println("Exit");
                        isClose = true;
                        clientSocket.close();
                        inMessage.close();
                        inMessage = null;
                        outMessage.close();
                        break;
                    }
                    System.out.println(clientMessage.msg);
                    if (clientMessage.msg.regionMatches(0,"login",0,5))
                        this.sendMsg("start", null);
                    if (clientMessage.msg.regionMatches(0,"game",0,4)) {
                        List<GField> list = (List<GField>) clientMessage.gamesArray;
                        server.gamesArray = FXCollections.observableList(list);

                        server.sendMessageToAllClients("lobbies", server.gamesArray);
                    }
                }
                Thread.sleep(100);
            }
        }
        catch (InterruptedException | IOException | ClassNotFoundException ex) {
            System.out.println("Client lost connection with server.");
        }
        finally {
            this.close();
        }
    }

    public void sendMsg(String msg, ObservableList<GField> gamesList) {
        try {
            if (!clientSocket.isClosed()) {
                Message message = new Message(msg, gamesList);
                outMessage.writeObject(message);
                outMessage.flush();
            }
        } catch (Exception ex) {
            System.out.println("Client lost connection with server.");
        }
    }

    public void close() {
        server.removeClient(this);
        clients_count--;
        if (clients_count != 0)
            server.sendMessageToAllClients("Players in game = " + clients_count, null);
    }
}
