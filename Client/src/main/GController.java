package main;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import java.io.IOException;

public class GController {
    public ObservableList<GField> gamesTableFields = FXCollections.observableArrayList();
    public int selectIndex;

    @FXML
    private TableView<GField> gamesTable;

    @FXML
    private TableColumn<GField, String> clientsLobbyColumn;

    @FXML
    private TableColumn<GField, String> clientsPlayerColumn;

    @FXML
    public void initialize() {
        clientsLobbyColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getCount()));
        clientsPlayerColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getNames()));
        gamesTable.setItems(gamesTableFields);
    }

    @FXML
    public void createGame() throws IOException {
        GField newGame = new GField("1/3", Client.clientName);
        gamesTableFields.add(newGame);
        initialize();
        TableView.TableViewSelectionModel<GField> selectionModel = gamesTable.getSelectionModel();
        selectionModel.select(newGame);
        gamesTable.setSelectionModel(selectionModel);
        openGameWindow(true);
    }

    @FXML
    public void joinGame() throws IOException {
        selectIndex = gamesTable.getSelectionModel().getSelectedIndex();
        if (selectIndex != -1) {
            if (!gamesTableFields.get(selectIndex).count.equals("3/3")) {
                if (gamesTableFields.get(selectIndex).count.equals("1/3")) {
                    gamesTableFields.get(selectIndex).count = "2/3";
                } else {
                    if (gamesTableFields.get(selectIndex).count.equals("2/3")) {
                        gamesTableFields.get(selectIndex).count = "3/3";
                    }
                }
                gamesTableFields.get(selectIndex).names += " " + Client.clientName;
                openGameWindow(false);
            }
        }
    }

    public void deleteEmptyGame() throws IOException {
        for (int i = 0; i < gamesTableFields.size(); i++) {
            if (gamesTableFields.get(i).players.size() == 0) {
                gamesTableFields.remove(i);
                sendMsg("gameremove");
            }
        }
    }

    private void openGameWindow(boolean isFirst) throws IOException {
        selectIndex = gamesTable.getSelectionModel().getSelectedIndex();
        Player player = new Player(Client.clientName);
        gamesTableFields.get(selectIndex).players.add(player);
        sendMsg("gameupd");

        Stage tempStage = (Stage) gamesTable.getScene().getWindow();
        tempStage.hide();
        Stage primaryStage = new Stage();

        BorderPane root = new BorderPane();
        DropShadow color = new DropShadow();
        color.setColor(Color.PURPLE);
        root.setEffect(color);

        primaryStage.setTitle("Game");
        Scene scene = new Scene(root, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                try {
                    showMenu(tempStage,primaryStage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        primaryStage.show();
        sendMsg("gameupd");
    }

    private void showMenu(Stage menuStage, Stage gameStage) throws IOException {
        menuStage.show();
        int tempIndex = 0;
        for (int i = 0; i < gamesTableFields.get(selectIndex).players.size(); i++){
            if (gamesTableFields.get(selectIndex).players.get(i).name.equals(Client.clientName)){
                tempIndex = i;
            }
        }
        gamesTableFields.get(selectIndex).players.remove(tempIndex);
        sendMsg("gameupd");
        gameStage.close();
    }

    private void sendMsg(String messageStr) throws IOException {
        Message message = new Message(messageStr, gamesTableFields);
        Client.outMessage.writeObject(message);
        Client.outMessage.flush();
    }
}