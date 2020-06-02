package main;

import java.io.*;
import java.net.Socket;

public class Client {

    private Socket clientSocket;
    public static ObjectInputStream inMessage;
    private BufferedReader bufferedReader;
    public static ObjectOutputStream outMessage;

    private static boolean isExit = false;
    public static boolean isClose = false;

    public static String clientName;

    public void clientWork() throws Exception{
        while (true) {
            if (isExit)
                break;
            if (bufferedReader.ready()) {
                Message answer = (Message) inMessage.readObject();
                System.out.println(answer.msg);
            }
            if (isClose){
                sendMsg("end");
                outMessage.close();
                inMessage.close();
                clientSocket.close();
                isExit = true;
                break;
            }
        }
    }

    public Client(){
        try {
            if (!isExit) {
                clientSocket = new Socket("localhost", 1488);

                inMessage = new ObjectInputStream(clientSocket.getInputStream());
                bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outMessage = new ObjectOutputStream(clientSocket.getOutputStream());
            }
        } catch (IOException e) {
            System.out.println("Не удалось установить подключение с сервером, проверьте подключение к сети!");
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientWork();
                } catch (Exception e) {
                    System.out.println("Клиент потерял соединение с сервером!");
                }
            }
        }).start();

    }

    public void sendMsg(String messageStr) throws IOException {
        Message message = new Message(messageStr);
        outMessage.writeObject(message);
        outMessage.flush();
    }
}