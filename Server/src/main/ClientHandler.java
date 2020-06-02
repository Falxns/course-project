package main;

import java.io.*;
import java.net.Socket;

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
            System.out.println("Не удалось установить соединение!");
        }
    }

    @Override
    public void run() {
        try {
            if (!isClose && !clientSocket.isClosed()) {
                while (true) {
                    server.sendMessageToAllClients("Новый участник вошёл в игру!");
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
                }
                Thread.sleep(100);
            }
        }
        catch (InterruptedException | IOException | ClassNotFoundException ex) {
            System.out.println("Клиент потерял соединение с сервером!");
        }
        finally {
            this.close();
        }
    }

    public void sendMsg(String msg) {
        try {
            if (!clientSocket.isClosed()) {
                Message message = new Message(msg);
                outMessage.writeObject(message);
                outMessage.flush();
            }
        } catch (Exception ex) {
            System.out.println("Клиент потерял соединение с сервером!");
        }
    }

    public void close() {
        server.removeClient(this);
        clients_count--;
        if (clients_count != 0)
            server.sendMessageToAllClients("Клиентов в игре = " + clients_count);
    }
}
