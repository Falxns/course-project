package main;

import javafx.animation.FadeTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class GController {
    public ObservableList<GField> gamesTableFields = FXCollections.observableArrayList();
    public int selectIndex;
    public Label firstPlayer = null;
    public Label secondPlayer = null;
    public Label infoLabel;
    public Label moveLabel;

    private ImageView retreatImage;
    private int playerIndex;
    private FlowPane handPane;
    private Button newCardButton = new Button("Take card");
    private Button passButton = new Button("Skip");
    private boolean isStop = false;

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
        Random random = new Random(System.currentTimeMillis());
        ArrayList<Integer> deck = new ArrayList<Integer>();
        int i = 1;
        while (i <= 36){
            int number = 1 + random.nextInt(36);
            if (deck.indexOf(number) == -1){
                deck.add(number);
                i++;
            }
        }
        GField newGame = new GField("1/3", Client.clientName, deck);
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
                sendMsg();
            }
        }
    }

    private void endMessage(){
        Stage gameStage = (Stage) firstPlayer.getScene().getWindow();
        Stage menuStage = (Stage) gamesTable.getScene().getWindow();
        Stage stage = new Stage();
        BorderPane pane = new BorderPane();
        Player tempPlayer = null;
        for (int i = 0; i < gamesTableFields.get(selectIndex).players.size(); i++){
            if (gamesTableFields.get(selectIndex).players.get(i).hand.size() == 0){
                tempPlayer = gamesTableFields.get(selectIndex).players.get(i);
            }
        }
        Label mess = null;
        if (tempPlayer != null) {
            mess = new Label("Winner: " + tempPlayer.name);
            Button button = new Button("OK");
            pane.setTop(mess);
            pane.setCenter(button);
            Scene scene = null;
            scene = new Scene(pane, 100,100);
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    try {
                        showMenu(menuStage,gameStage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    stage.close();
                }
            });
            stage.setScene(scene);
            stage.show();
        }
    }

    public void fillLabel() throws IOException {
        if (gamesTableFields.size() != 0) {
            if (firstPlayer != null) {
                if (secondPlayer.getText().equals("Nekoplayer 2")) {
                    for (int i = 0; i < gamesTableFields.get(selectIndex).players.size(); i++) {
                        if (!gamesTableFields.get(selectIndex).players.get(i).name.equals(Client.clientName)) {
                            firstPlayer.setText(gamesTableFields.get(selectIndex).players.get(i).name);
                        }
                    }
                }
            }
            if (secondPlayer != null) {
                for (int i = 0; i < gamesTableFields.get(selectIndex).players.size(); i++) {
                    if (firstPlayer != null && !gamesTableFields.get(selectIndex).players.get(i).name.equals(Client.clientName) && !gamesTableFields.get(selectIndex).players.get(i).name.equals(firstPlayer.getText()))
                        secondPlayer.setText(gamesTableFields.get(selectIndex).players.get(i).name);
                }
            }
            if (gamesTableFields.get(selectIndex).players.size() == 3) {
                boolean isEnd = false;
                for (int i = 0; i < gamesTableFields.get(selectIndex).players.size(); i++) {
                    if (gamesTableFields.get(selectIndex).players.get(i).hand.size() == 0) {
                        isEnd = true;
                        break;
                    }
                }
                if (isEnd) {
                    if (!isStop) {
                        endMessage();
                        isStop = true;
                        sendMsg();
                    }
                } else {
                    play();
                }
            } else {
                if (gamesTableFields.get(selectIndex).players.size() > 0) {
                    int amount = gamesTableFields.get(selectIndex).players.size();
                    gamesTableFields.get(selectIndex).count = amount + "/3";
                    gamesTableFields.get(selectIndex).names = "";
                    for (int i = 0; i < gamesTableFields.get(selectIndex).players.size(); i++) {
                        gamesTableFields.get(selectIndex).names += gamesTableFields.get(selectIndex).players.get(i).name + " ";
                    }
                    gamesTableFields.get(selectIndex).deck.addAll(gamesTableFields.get(selectIndex).retreat);
                    gamesTableFields.get(selectIndex).retreat.removeAll(gamesTableFields.get(selectIndex).retreat);
                }
            }
        }
    }

    private void play() throws FileNotFoundException {
        int i = 0;
        if (gamesTableFields.get(selectIndex).retreat.size() == 0) {
            while (gamesTableFields.get(selectIndex).deck.get(i).value != 6 && gamesTableFields.get(selectIndex).deck.get(i).value != 9 && gamesTableFields.get(selectIndex).deck.get(i).value != 12) {
                i++;
            }
            int imgNum = (gamesTableFields.get(selectIndex).deck.get(i).value - 5) + (gamesTableFields.get(selectIndex).deck.get(i).lear - 1) * 9;
            FileInputStream input = new FileInputStream("Client/src/cards/" + imgNum + ".png");
            Image image = new Image(input, 100, 170, false, true);
            retreatImage.setImage(image);
            gamesTableFields.get(selectIndex).retreat.add(gamesTableFields.get(selectIndex).deck.get(i));
            gamesTableFields.get(selectIndex).deck.remove(i);
        } else {
            int imgNum = (gamesTableFields.get(selectIndex).retreat.get(gamesTableFields.get(selectIndex).retreat.size() - 1).value - 5) + (gamesTableFields.get(selectIndex).retreat.get(gamesTableFields.get(selectIndex).retreat.size() - 1).lear - 1) * 9;
            FileInputStream input = new FileInputStream("Client/src/cards/" + imgNum + ".png");
            Image image = new Image(input, 100, 170, false, true);
            retreatImage.setImage(image);
        }
        String tempName = "";
        for (int k = 0; k < gamesTableFields.get(selectIndex).players.size(); k++){
            if (gamesTableFields.get(selectIndex).players.get(k).isTurn){
                tempName = gamesTableFields.get(selectIndex).players.get(k).name;
            }
        }
        moveLabel.setText(gamesTableFields.get(selectIndex).action);
        infoLabel.setText(tempName + " turn");
        handPane.getChildren().removeAll(handPane.getChildren());
        Player player = gamesTableFields.get(selectIndex).players.get(playerIndex);
        for (int j = 0; j < player.hand.size(); j++) {
            int imgNum = (player.hand.get(j).value - 5) + (player.hand.get(j).lear - 1) * 9;
            FileInputStream input = new FileInputStream("Client/src/cards/"+ imgNum + ".png");
            Image image = new Image(input, 100, 170, false, true);
            ImageView imageView = new ImageView(image);
            imageView.setOpacity(0.5);
            imageView.getStyleClass().add("card");
            Card tempCard = player.hand.get(j);
            imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, (event)-> {
                try {
                    if (gamesTableFields.get(selectIndex).players.get(playerIndex).isTurn) {
                        if (move(tempCard)) {
                            passButton.setDisable(true);
                            newCardButton.setDisable(false);
                            handPane.getChildren().remove(imageView);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            imageView.addEventHandler(MouseEvent.MOUSE_ENTERED, (event)-> {
                final FadeTransition fadeIn = new FadeTransition(Duration.millis(100));
                fadeIn.setNode(imageView);
                fadeIn.setToValue(1);
                fadeIn.playFromStart();
            });
            imageView.addEventHandler(MouseEvent.MOUSE_EXITED, (event)-> {
                final FadeTransition fadeOut= new FadeTransition(Duration.millis(100));
                fadeOut.setNode(imageView);
                fadeOut.setToValue(0.5);
                fadeOut.playFromStart();
            });
            handPane.getChildren().add(imageView);
        }
    }

    private void sevenAction(){
        int nextPlayerIndex = (playerIndex + 1) % 3;
        getNewCard(gamesTableFields.get(selectIndex).players.get(nextPlayerIndex));
        getNewCard(gamesTableFields.get(selectIndex).players.get(nextPlayerIndex));
        gamesTableFields.get(selectIndex).action = "Nekoplayer " + gamesTableFields.get(selectIndex).players.get(nextPlayerIndex).name + " takes 2 cards";
    }

    private void eightAction(){
        Stage stage = new Stage();
        FlowPane flowPane = new FlowPane();
        ObservableList<String> list = FXCollections.observableArrayList("6","7","8","9","10","J","Q","K","A");
        ComboBox<String> comboBox = new ComboBox<>(list);
        comboBox.setValue("6");
        flowPane.getChildren().add(comboBox);
        Button okButton = new Button("Go!");
        okButton.setOnAction(actionEvent -> {
            int eightValue = 0;
            switch (comboBox.getValue()) {
                case "6": eightValue = 6; break;
                case "7": eightValue = 7; break;
                case "8": eightValue = 8; break;
                case "9": eightValue = 9; break;
                case "10": eightValue = 10; break;
                case "J": eightValue = 11; break;
                case "Q": eightValue = 12; break;
                case "K": eightValue = 13; break;
                case "A": eightValue = 14; break;
            }
            gamesTableFields.get(selectIndex).action = "Chosen card: " + comboBox.getValue();
            gamesTableFields.get(selectIndex).eightVal = eightValue;
            changeTurn();
            try {
                sendMsg();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage.close();
        });
        flowPane.getChildren().add(okButton);
        Scene scene = new Scene(flowPane, 200, 50);
        stage.setScene(scene);
        stage.setTitle("Choose value");
        stage.show();
    }

    private void tenAction(){
        int nextPlayerIndex = (playerIndex + 1) % 3;
        Random random = new Random(System.currentTimeMillis());
        int number = 0;
        if (gamesTableFields.get(selectIndex).players.get(playerIndex).hand.size() != 1) {
            number = random.nextInt(gamesTableFields.get(selectIndex).players.get(playerIndex).hand.size());
        }
        gamesTableFields.get(selectIndex).players.get(nextPlayerIndex).hand.add(gamesTableFields.get(selectIndex).players.get(playerIndex).hand.get(number));
        gamesTableFields.get(selectIndex).players.get(playerIndex).hand.remove(number);
        gamesTableFields.get(selectIndex).action = "Nekoplayer " + gamesTableFields.get(selectIndex).players.get(nextPlayerIndex).name + " drawn a card from nekoplayer " + gamesTableFields.get(selectIndex).players.get(playerIndex).name;
    }

    private void jackAction(){
        Stage stage = new Stage();
        FlowPane flowPane = new FlowPane();
        ObservableList<String> list = FXCollections.observableArrayList("Diamond","Club","Hearts","Spade");
        ComboBox<String> comboBox = new ComboBox<String>(list);
        comboBox.setValue("Diamond");
        flowPane.getChildren().add(comboBox);
        Button okButton = new Button("Go!");
        okButton.setOnAction(actionEvent -> {
            int jackValue = 0;
            switch (comboBox.getValue()) {
                case "Diamond": jackValue = 1; break;
                case "Club": jackValue = 2; break;
                case "Hearts": jackValue = 3; break;
                case "Spade": jackValue = 4; break;
            }
            gamesTableFields.get(selectIndex).action = "Chosen by jack lear: " + comboBox.getValue();
            gamesTableFields.get(selectIndex).jackVal = jackValue;
            changeTurn();
            try {
                sendMsg();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage.close();
        });
        flowPane.getChildren().add(okButton);
        Scene scene = new Scene(flowPane, 200, 50);
        stage.setScene(scene);
        stage.setTitle("Choose lear ^-^");
        stage.show();
    }

    private void kingAction(){
        ArrayList<Card> buffer = gamesTableFields.get(selectIndex).players.get(0).hand;
        gamesTableFields.get(selectIndex).players.get(0).hand = gamesTableFields.get(selectIndex).players.get(2).hand;
        gamesTableFields.get(selectIndex).players.get(2).hand = gamesTableFields.get(selectIndex).players.get(1).hand;
        gamesTableFields.get(selectIndex).players.get(1).hand = buffer;
        gamesTableFields.get(selectIndex).action = "Nekoplayers changed hands! UwU";
    }

    private void aceAction(){
        int nextPlayerIndex = (playerIndex + 2) % 3;
        int index = (playerIndex + 1) % 3;
        gamesTableFields.get(selectIndex).players.get(playerIndex).isTurn = false;
        gamesTableFields.get(selectIndex).players.get(nextPlayerIndex).isTurn = true;
        gamesTableFields.get(selectIndex).action = "Nekoplayer "  + gamesTableFields.get(selectIndex).players.get(index).name + " skips a turn. :c";
    }

    private void cardAction(Card card) throws IOException {
        Player player;
        gamesTableFields.get(selectIndex).retreat.add(card);
        player = gamesTableFields.get(selectIndex).players.get(playerIndex);
        player.hand.remove(card);
        switch (card.value) {
            case 6:
            case 9:
            case 12:
                gamesTableFields.get(selectIndex).action = "";
                changeTurn();
                break;
            case 7:
                changeTurn();
                sevenAction();
                break;
            case 8:
                if (player.hand.size() != 0) {
                    eightAction();
                }
                break;
            case 10:
                changeTurn();
                tenAction();
                break;
            case 11:
                if (player.hand.size() != 0) {
                    jackAction();
                }
                break;
            case 13:
                changeTurn();
                kingAction();
                break;
            case 14:
                aceAction();
                break;
        }
        sendMsg();
    }

    private void changeTurn(){
        int nextPlayerIndex = (playerIndex + 1) % 3;
        gamesTableFields.get(selectIndex).players.get(playerIndex).isTurn = false;
        gamesTableFields.get(selectIndex).players.get(nextPlayerIndex).isTurn = true;
    }

    private boolean move(Card card) throws IOException {
        if (card.value == 11){
            gamesTableFields.get(selectIndex).eightVal = 0;
            cardAction(card);
            return true;
        }
        if (gamesTableFields.get(selectIndex).jackVal == 0) {
            if (gamesTableFields.get(selectIndex).eightVal == 0) {
                if (gamesTableFields.get(selectIndex).retreat.get(gamesTableFields.get(selectIndex).retreat.size() - 1).value == card.value
                        || gamesTableFields.get(selectIndex).retreat.get(gamesTableFields.get(selectIndex).retreat.size() - 1).lear == card.lear) {
                    cardAction(card);
                    return true;
                } else {
                    return false;
                }
            } else {
                if (card.value == gamesTableFields.get(selectIndex).eightVal) {
                    gamesTableFields.get(selectIndex).eightVal = 0;
                    cardAction(card);
                    return true;
                } else
                    return false;
            }
        } else {
            if (card.lear == gamesTableFields.get(selectIndex).jackVal){
                gamesTableFields.get(selectIndex).jackVal = 0;
                cardAction(card);
                return true;
            } else {
                return false;
            }
        }
    }

    private void getNewCard(Player nextPlayer){
        if (gamesTableFields.get(selectIndex).deck.size() != 0) {
            nextPlayer.hand.add(gamesTableFields.get(selectIndex).deck.get(0));
            gamesTableFields.get(selectIndex).deck.remove(0);
        }
        if (gamesTableFields.get(selectIndex).deck.size() == 0) {
            int retreatSize = gamesTableFields.get(selectIndex).retreat.size();
            for (int i = 0; i < retreatSize - 1; i++){
                Random random = new Random(System.currentTimeMillis());
                int num = random.nextInt(gamesTableFields.get(selectIndex).retreat.size());
                gamesTableFields.get(selectIndex).deck.add(gamesTableFields.get(selectIndex).retreat.get(num));
                gamesTableFields.get(selectIndex).retreat.remove(num);
            }
        }
    }

    private void openGameWindow(boolean isFirst) throws IOException {
        selectIndex = gamesTable.getSelectionModel().getSelectedIndex();
        ArrayList<Card> hand = new ArrayList<Card>();
        for (int i = 0; i < 5; i++){
            hand.add(gamesTableFields.get(selectIndex).deck.get(0));
            gamesTableFields.get(selectIndex).deck.remove(0);
        }
        Player player = new Player(Client.clientName,hand,isFirst);
        gamesTableFields.get(selectIndex).players.add(player);
        playerIndex =  gamesTableFields.get(selectIndex).players.indexOf(player);
        sendMsg();

        Stage tempStage = (Stage) gamesTable.getScene().getWindow();
        tempStage.hide();
        Stage primaryStage = new Stage();
        BorderPane root = new BorderPane();
        Image img = new Image("/img/game.jpg");
        ImageView backgr = new ImageView();
        backgr.setImage(img);
        root.getChildren().add(backgr);
        Pane firstPane = new Pane();
        if (gamesTableFields.get(selectIndex).players.size() >= 2) {
            firstPlayer = new Label(gamesTableFields.get(selectIndex).players.get(0).name);
        } else {
            firstPlayer = new Label("Nekoplayer 1");
        }
        firstPlayer.setLayoutX(120.0);
        firstPlayer.setLayoutY(50.0);
        firstPane.getChildren().add(firstPlayer);

        Pane secondPane = new Pane();
        if (gamesTableFields.get(selectIndex).players.size() == 3) {
            secondPlayer = new Label(gamesTableFields.get(selectIndex).players.get(1).name);
        } else {
            secondPlayer = new Label("Nekoplayer 2");
        }
        secondPlayer.setLayoutX(-80.0);
        secondPlayer.setLayoutY(50.0);
        secondPane.getChildren().add(secondPlayer);
        infoLabel = new Label();
        infoLabel.setLayoutX(250.0);
        infoLabel.setLayoutY(30.0);
        firstPane.getChildren().add(infoLabel);
        moveLabel = new Label();
        moveLabel.setLayoutX(250.0);
        moveLabel.setLayoutY(60.0);
        firstPane.getChildren().add(moveLabel);
        root.setLeft(firstPane);
        root.setRight(secondPane);
        retreatImage = new ImageView();
        root.setCenter(retreatImage);

        handPane = new FlowPane();
        Pane buttonPane = new Pane();

        passButton.setDisable(true);
        newCardButton.setLayoutX(320);
        newCardButton.setOnAction(actionEvent -> {
            if (gamesTableFields.get(selectIndex).players.get(playerIndex).isTurn && gamesTableFields.get(selectIndex).players.size() == 3) {
                passButton.setDisable(false);
                newCardButton.setDisable(true);
                getNewCard(gamesTableFields.get(selectIndex).players.get(playerIndex));
                try {
                    sendMsg();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        buttonPane.getChildren().add(newCardButton);

        passButton.setLayoutX(450);
        passButton.setOnAction(actionEvent -> {
            passButton.setDisable(true);
            newCardButton.setDisable(false);
            changeTurn();
            try {
                sendMsg();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        buttonPane.getChildren().add(passButton);
        root.setTop(buttonPane);
        root.setBottom(handPane);

        primaryStage.setTitle("Meow-Meow");
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add("/styles/game.css");
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
        sendMsg();
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
        sendMsg();
        gameStage.close();
    }

    private void sendMsg() throws IOException {
        Message message = new Message("game_upd", gamesTableFields);
        Client.outMessage.writeObject(message);
        Client.outMessage.flush();
    }
}