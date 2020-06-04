package main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;


public class Client {

    private Socket clientSocket;
    public static ObjectInputStream inMessage;
    private BufferedReader bufferedReader;
    public static ObjectOutputStream outMessage;

    private static boolean isExit = false;

    public static boolean isClose = false;

    public static String clientName;

    private GController controller;
    private ObservableList<GField> gamesTableFields;

    @FXML
    private TextField login;


    @FXML
    public void logIn() throws IOException {
        String msg = "login:" + login.getText();
        clientName = login.getText();
        sendMsg(msg);
    }

    public void startGame() throws Exception{
        Stage tempStage = (Stage) login.getScene().getWindow();
        tempStage.hide();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/Lobby.fxml"));
        Parent root = (Parent) loader.load();
        controller = loader.getController();
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Lobby");
        primaryStage.setScene(new Scene(root, 400, 600));
        primaryStage.setOnCloseRequest(windowEvent -> {
            isClose = true;
            primaryStage.close();
        });
        controller.gamesTableFields = gamesTableFields;
        controller.initialize();
        primaryStage.show();
    }

    public void clientWork() throws Exception{
        while (true) {
            if (isExit)
                break;
            if (bufferedReader.ready()) {
                Message answer = (Message) inMessage.readObject();
                System.out.println(answer.msg);
                if (answer.msg.equals("start")) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                startGame();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                if (answer.msg.equals("lobbies")) {
                    List<GField> list = (List<GField>) answer.gamesArray;
                    if (list != null) {
                        if (controller != null) {
                            controller.gamesTableFields = FXCollections.observableArrayList(list);
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        controller.deleteEmptyGame();
                                        controller.initialize();
                                        controller.fillLabel();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }
                            });
                        } else {
                            gamesTableFields = FXCollections.observableArrayList(list);
                        }
                    }
                }
            }
            if (isClose){
                sendMsg("close");
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
            byte[] buf = new byte[4];
            if (!isExit) {
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, InetAddress.getByName("192.168.100.255"), 7777);
                DatagramSocket datagramSocket = new DatagramSocket();
                datagramSocket.send(datagramPacket);
                datagramSocket.receive(datagramPacket);
                datagramSocket.close();

                InetAddress address = datagramPacket.getAddress();
                ByteBuffer byteBuffer = ByteBuffer.wrap(datagramPacket.getData());
                int port = byteBuffer.getInt();

                clientSocket = new Socket(address, port);

                inMessage = new ObjectInputStream(clientSocket.getInputStream());
                bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                outMessage = new ObjectOutputStream(clientSocket.getOutputStream());
                controller = null;
                gamesTableFields = FXCollections.observableArrayList();
            }
        } catch (IOException e) {
            System.out.println("Can't connect to server, check network connection.");
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientWork();
                } catch (Exception e) {
                    System.out.println("Client lost the connection.");
                }
            }
        }).start();

    }

    private void sendMsg(String messageStr) throws IOException {
        Message message = new Message(messageStr, null);
        outMessage.writeObject(message);
        outMessage.flush();
    }
}