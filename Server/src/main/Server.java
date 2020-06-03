package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    static final int PORT = 1488;
    private ObservableList<ClientHandler> clients = FXCollections.observableArrayList();
    public ObservableList<GField> gamesArray;
    public Server() {
        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running.");
            while (true) {
                clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket, this);
                clients.add(client);
                new Thread(client).start();
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                System.out.println("Server stopped.");
                if (serverSocket != null) {
                    serverSocket.close();
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void sendMessageToAllClients(String msg, ObservableList<GField> gamesList) {
        for (ClientHandler client : clients) {
            client.sendMsg(msg, gamesList);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}
