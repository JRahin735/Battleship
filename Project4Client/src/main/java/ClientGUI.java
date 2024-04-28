import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Random;
import java.util.random.*;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientGUI extends Application {

    Client clientConnection;

    String playerName = "PLAYER";
    String opponentName = "opponent";
    private int y;
    int[]moves = {-1,-1};
    String getPlayerName () {return playerName;}
    String getOpponentName () {return opponentName;}

    Board myBoard = new Board(7);
    Board opponentBoard = new Board(7);
    Button[][] buttons = new Button[7][7]; // Buttons for the board
    Button[][] myBoardButtons = new Button[7][7];
    Button[][] opponentBoardButtons = new Button[7][7];

    Button[][] buttons1 = new Button[7][7]; // Buttons for the board
    Button[][] myBoardButtons1 = new Button[7][7];
    Button[][] opponentBoardButtons1 = new Button[7][7];
    boolean[] shipPlaced = {false, false, false, false};

    Text PlayersPVP2, Players, anotherHeading;
    TextField textBox;
    GridPane gridPane = new GridPane(); //compgame init
    GridPane myBoardGrid = new GridPane();//compgame sc2
    GridPane opponentBoardGrid = new GridPane(); //compgame sc2

    GridPane myBoardGridPVP = new GridPane();//pvp sc2
    GridPane opponentBoardGridPVP = new GridPane(); //pvp sc2

     Random random = new Random();
    private void revealRow(GridPane grid)
    {
        //reveals a random row of the grid
        System.out.println("Entered funct row\n");
        int row = random.nextInt(6);
        for (int i = 0; i < 7; i++)
        {
            System.out.println("Entered funct hit row: " +row+" col: " + i);
            Message toSend = new Message("PLAYER_MOVE", getPlayerName(), 1, row, i, myBoard);
            clientConnection.send(toSend);


        }
    }

    //function that reveals one column of the grid

    private void revealColumn(GridPane grid)
    {
        System.out.println("Entered funct col\n");
        //reveals a random column of the grid
        int col = random.nextInt(6);
        for (int i = 0; i < 7; i++)
        {
            System.out.println("Entered funct hit column: " +col + " row: "+ i);
            Message toSend = new Message("PLAYER_MOVE", getPlayerName(), 1, i, col, myBoard);
            clientConnection.send(toSend);
        }
    }

    private void handleButtonAction(int x, int y, int shipSize) {

        BoardCell cell = myBoard.getCell(x, y);
        int valid = 0;
        System.out.println("Ship size:"+shipSize);
        if (cell != null && x+shipSize <= 7) {
            System.out.println("Entered here 1"+shipSize);
                for(int i = 0; i< shipSize; i++) {
                    BoardCell currCell = myBoard.getCell(x+i,y);
                    if(currCell.hasShip() == false) {
                        valid++;
                    }
                }

            if(valid == shipSize & shipPlaced[shipSize-2] == false)
            {
                System.out.println("Entered here 2"+shipSize);
                for(int i = 0; i < shipSize; i++)
                {
                    BoardCell currCell = myBoard.getCell(x,y);
                    currCell.setShip(true); // Assume we are setting ships here; modify logic as needed
                    buttons1[x][y].setStyle("-fx-background-color: #C0C0C0;");
                    buttons1[x][y].setDisable(true);
                    myBoardButtons1[x][y].setStyle("-fx-background-color: #C0C0C0;");
                    myBoardButtons1[x][y].setDisable(true);
                    x++;
                }

                shipPlaced[shipSize-2] = true;
            }
        }
    }


    private void handleButtonActionPVP(int x, int y, int shipSize) {

        BoardCell cell = myBoard.getCell(x, y);
        int valid = 0;
        System.out.println("Ship size:"+shipSize);
        if (cell != null && x+shipSize <= 7) {
            System.out.println("Entered here 1"+shipSize);
            for(int i = 0; i< shipSize; i++) {
                BoardCell currCell = myBoard.getCell(x+i,y);
                if(currCell.hasShip() == false) {
                    valid++;
                }
            }

            if(valid == shipSize & shipPlaced[shipSize-2] == false)
            {
                System.out.println("Entered here 2"+shipSize);
                for(int i = 0; i < shipSize; i++)
                {
                    BoardCell currCell = myBoard.getCell(x,y);
                    currCell.setShip(true); // Assume we are setting ships here; modify logic as needed
                    buttons[x][y].setStyle("-fx-background-color: #C0C0C0;");
                    buttons[x][y].setDisable(true);
                    myBoardButtons[x][y].setStyle("-fx-background-color: #C0C0C0;");
                    myBoardButtons[x][y].setDisable(true);
                    x++;
                }

                shipPlaced[shipSize-2] = true;
            }
        }
    }


    private void replaceButton(GridPane grid, int row, int col, Button button) {
        Node node = getNodeByRowColumnIndex(row, col, grid);
        if (node != null) {
            grid.getChildren().remove(node);
        }
        grid.add(button, col, row);
    }

    private Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) == row &&
                    GridPane.getColumnIndex(node) != null && GridPane.getColumnIndex(node) == column) {
                return node;
            }
        }
        return null;
    }

    int p1HitCount = 0;
    int p2HitCount = 0;


    Scene homeScreenScene, howToPlayScene, pvpGameScreen1,pvpGameScreen2, compGameScreen1,compGameScreen2, waitingScreen;

    public static void main(String[] args) {launch(args);}

    @Override
    public void start(Stage primaryStage) throws Exception {

        clientConnection = new Client(data -> {
            Platform.runLater(() -> {

                Message messageReceived = (Message) data;

                if (messageReceived.getType().equals("JOIN")) {

                    if (messageReceived.getMessage().equals("USERNAME ALREADY EXISTS")) {
                        showAlert("error", "Username already exists, pick a new one");
                    }
                    else {
                        playerName = messageReceived.getMessage();
                        System.out.println(getPlayerName() + " : " + messageReceived.getMessage());
                        showAlert("congrats", "Username set to " +getPlayerName());
                        Text anotherHeading = new Text(getPlayerName() + "     vs     Computer");
                    }
                }
                else if (messageReceived.getType().equals("DECLARE_WINNER")) {

                    // PLAYER won
                    if (messageReceived.getMessage().equals(getPlayerName())) {
                        showAlert("congrats", "You won the match!!");
                        primaryStage.setScene(homeScreenScene);
                    }
                    // PLAYER lost
                    else {
                        showAlert("Better luck next time", "You lost this match");
                        primaryStage.setScene(homeScreenScene);
                    }
                }
                else if (messageReceived.getType().equals("SEND_BOARD")) {

                    System.out.println("Entered msg" + messageReceived.getType());
                    // this is player's board
                    if (messageReceived.getSender().equals(getPlayerName()))
                    {

                        if(messageReceived.getGameMode() == 0) {
                            System.out.println("Entered hit");

                            if (messageReceived.getMessage().equals("HIT")) {
                                Button button = new Button();
                                button.setStyle("-fx-background-color:#C70039 ;");
                                button.setDisable(true);
                                button.setMinWidth(50);
                                button.setMinHeight(50);
                                System.out.println("Moves"+moves[0]+moves[1]);
                                int x = messageReceived.getX();
                                int y = messageReceived.getY();
                                myBoardButtons1[x][y] = button;
                                replaceButton(myBoardGrid, x,y, myBoardButtons1[x][y]);

                            }
                            else if (messageReceived.getMessage().equals("MISS")) {
                                Button button = new Button();
                                button.setStyle("-fx-background-color: #008000;");
                                button.setDisable(true);
                                button.setMinWidth(50);
                                button.setMinHeight(50);
                                int x = messageReceived.getX();
                                int y = messageReceived.getY();
                                myBoardButtons1[x][y] = button;
                                replaceButton(myBoardGrid, x,y, myBoardButtons1[x][y]);
                            }
                        }
                        else if(messageReceived.getGameMode() == 1)
                        {
                            System.out.println("Response recvd by opponent" + messageReceived.getMessage());

                            if(messageReceived.getMessage().equals("HIT"))
                            {
                                Button button = new Button();
                                button.setStyle("-fx-background-color: #C70039;");
                                button.setDisable(true);
                                button.setMinWidth(50);
                                button.setMinHeight(50);
                                int x = messageReceived.getX();
                                int y = messageReceived.getY();
                                myBoardButtons[x][y] = button;
                                replaceButton(myBoardGridPVP, x,y, myBoardButtons[x][y]);

                            } else if (messageReceived.getMessage().equals("MISS")) {
                                Button button = new Button();
                                button.setStyle("-fx-background-color: #008000;");
                                button.setDisable(true);
                                button.setMinWidth(50);
                                button.setMinHeight(50);
                                int x = messageReceived.getX();
                                int y = messageReceived.getY();
                                myBoardButtons[x][y] = button;
                                replaceButton(myBoardGridPVP, x,y, myBoardButtons[x][y]);

                            }

                        }


                    }
                    // this is opponent's board
                    else {
                        if(messageReceived.getGameMode() == 0) {
                            System.out.println("Entered hit");

                            if (messageReceived.getMessage().equals("HIT")) {
                                Button button = new Button();
                                button.setStyle("-fx-background-color: #C0C0C0;");
                                button.setDisable(true);
                                button.setMinWidth(50);
                                button.setMinHeight(50);
                                System.out.println("Moves"+moves[0]+moves[1]);
                                int x = moves[0];
                                int y = moves[1];
                                opponentBoardButtons1[x][y] = button;
                                replaceButton(opponentBoardGrid, moves[0], moves[1], opponentBoardButtons1[moves[0]][moves[1]]);

                            }
                            else if (messageReceived.getMessage().equals("MISS")) {
                                Button button = new Button();
                                button.setStyle("-fx-background-color: #C70039;");
                                button.setDisable(true);
                                button.setMinWidth(50);
                                button.setMinHeight(50);
                                int x = moves[0];
                                int y = moves[1];
                                opponentBoardButtons1[x][y] = button;
                                replaceButton(opponentBoardGrid, moves[0], moves[1], opponentBoardButtons1[moves[0]][moves[1]]);
                            }
                        }
                        else if(messageReceived.getGameMode() == 1)
                        {
                            System.out.println("Response recvd by opponent" + messageReceived.getMessage());
                            if(messageReceived.getMessage().equals("HIT"))
                            {
                                Button button = new Button();
                                button.setStyle("-fx-background-color: #C0C0C0;");
                                button.setDisable(true);
                                button.setMinWidth(50);
                                button.setMinHeight(50);
                                int x = moves[0];
                                int y = moves[1];
                                opponentBoardButtons[x][y] = button;
                                replaceButton(opponentBoardGridPVP, moves[0], moves[1], opponentBoardButtons[moves[0]][moves[1]]);



                            } else if (messageReceived.getMessage().equals("MISS")) {
                                Button button = new Button();
                                button.setStyle("-fx-background-color: #C70039;");
                                button.setDisable(true);
                                button.setMinWidth(50);
                                button.setMinHeight(50);
                                int x = moves[0];
                                int y = moves[1];
                                opponentBoardButtons[x][y] = button;
                                replaceButton(opponentBoardGridPVP, moves[0], moves[1], opponentBoardButtons[moves[0]][moves[1]]);

                            }

                        }

                    }
                }
                else if (messageReceived.getType().equals("WAIT_OVER")) {
                    opponentName = messageReceived.getMessage();
                    primaryStage.setScene(pvpGameScreen1);

                     PlayersPVP2 = new Text(getPlayerName() + "vs " + getOpponentName());
                    Players = new Text(getPlayerName() + "vs " + getOpponentName());


                }
                else if (messageReceived.getType().equals("TERMINATE_GAME")) {
                    primaryStage.setScene(homeScreenScene);
                }
                else if (messageReceived.getType().equals("MESSAGING")) {
                    textBox.setText(messageReceived.getMessage());
                }

            });
        });

        clientConnection.start();
        primaryStage.setTitle("Chat Client");

        // -------------------------- HOME SCREEN

        Label header = new Label("BATTLESHIP");
        Button vsCompButton = new Button("vs Comp");
        Button vsPlayerButton = new Button("vs Player");
        Button howToPlayButton = new Button("How to Play");
        HBox buttonBox = new HBox(20, vsCompButton, vsPlayerButton, howToPlayButton);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        Button submitButton = new Button("Submit");
        HBox nameBox = new HBox(20, usernameField, submitButton);
        VBox layout = new VBox(20, header, buttonBox, nameBox);

        homeScreenScene = new Scene(layout, 900, 700);

        vsCompButton.setOnAction(e -> {

            Message toSend = new Message("GAME_CREATION", getPlayerName(), 0, null);
            clientConnection.send(toSend);

            primaryStage.setScene(compGameScreen1);
        });

        vsPlayerButton.setOnAction(e -> {

            if (getPlayerName().equals("PLAYER")) {
                showAlert("error", "Pick a username before playing!");
            }
            else {

                Message toSend = new Message("GAME_CREATION", getPlayerName(), 1, null);
                clientConnection.send(toSend);
                primaryStage.setScene(waitingScreen);
            }
        });

        howToPlayButton.setOnAction(e -> primaryStage.setScene(howToPlayScene));

        submitButton.setOnAction(e -> {
            if (usernameField.getText().isEmpty()) {
                showAlert("Error", "Username cannot be empty!");
            } else {
                clientConnection.giveUsername(getPlayerName(), usernameField.getText());
            }
        });

        header.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #1C2833; -fx-text-alignment: center;");
        vsCompButton.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        vsPlayerButton.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        howToPlayButton.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        submitButton.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        usernameField.setStyle("-fx-control-inner-background: #D6DBDF; -fx-text-fill: #1C2833;");
        buttonBox.setAlignment(Pos.CENTER);
        nameBox.setAlignment(Pos.CENTER);
        layout.setAlignment(Pos.CENTER);


        // -------------------------- HOW TO PLAY SCREEN

        Label header2 = new Label("HOW TO PLAY");
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> primaryStage.setScene(homeScreenScene));

        Image image = new Image("GameMaster.jpg");
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(200);  // Set image height
        imageView.setPreserveRatio(true);  // Preserve image ratio

        Label gameMasterLabel = new Label("GAME MASTER");
        gameMasterLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-alignment: center; -fx-text-fill: #FF5733; -fx-letter-spacing: 0.5px;");

        Text instructionsText = new Text("Learn how to play Battleship:" +
                "\n- Each player places their ships on their grid, hidden from the enemy's view." +
                "\n- Players take turns guessing grid coordinates to attack enemy ships." +
                "\n- If a ship occupies the coordinates, it's a hit; otherwise, it's a miss." +
                "\n- Mark hits and misses on both your grid (for your shots) and an enemy grid (to track your guesses)." +
                "\n- The game continues until one player sinks all the enemy's ships." +
                "\n- The first player to sink all opposing ships wins the game." +
                "\nIf you hit an enemy's ship, the block turns silver and turns red if you miss." +
                "\nIf the opponent hits your ship, it turns red, and if they miss, the block turns green for you.");

        VBox layout2 = new VBox(10); // VBox for vertical layout
        layout2.getChildren().addAll(header2, instructionsText, imageView, gameMasterLabel, backButton);
        layout2.setAlignment(Pos.CENTER);

        header2.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 24px; -fx-font-weight: bold; -fx-text-alignment: center; -fx-text-fill: #D6DBDF; -fx-letter-spacing: 0.5px;");
        instructionsText.setStyle("-fx-font-family: 'Verdana'; -fx-fill: #D6DBDF; -fx-font-size: 16px; -fx-letter-spacing: 0.2px;");
        backButton.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        layout2.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: #1C2833;");

        howToPlayScene = new Scene(layout2, 900, 700);
        howToPlayScene.setFill(Color.valueOf("#1C2833"));



        // -------------------------- COMPUTER GAME

        // SCREEN 1

        Text title = new Text("Battleship!");
        Text anotherTitle = new Text(getPlayerName() + "vs Computer");
        Text instructions = new Text("Place your ships");

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("2", "3", "4", "5");
        comboBox.setValue("2");

        // Grid for placing ships
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                Button btn = new Button();
                btn.setMinWidth(50);
                btn.setMinHeight(50);
                btn.setStyle("-fx-background-color: #00FFFF;");

                Button tempbutton = new Button();
                tempbutton.setMinWidth(50);
                tempbutton.setMinHeight(50);
                tempbutton.setStyle("-fx-background-color: #00FFFF;");

                int finalI = i;
                int finalJ = j;

                AtomicInteger size = new AtomicInteger(Integer.parseInt(comboBox.getValue()));
                comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal.equals(oldVal)) {
                       // System.out.println("Selected: " + newVal);
                        size.set(Integer.parseInt(newVal));
                        // Perform any action you need based on the new value
                    }
                });

                btn.setOnAction(e -> handleButtonAction(finalI, finalJ, size.get()));
                tempbutton.setOnAction(e -> handleButtonAction(finalI, finalJ, size.get()));

                gridPane.add(btn, j, i);

                buttons1[i][j] = btn;

                myBoardButtons1[i][j] = tempbutton;
            }
        }

        gridPane.setAlignment(Pos.CENTER);

        Button quitButton = new Button("Quit");
        Button beginButton = new Button("Begin");
        HBox controlButtons = new HBox(quitButton, beginButton);
        controlButtons.setAlignment(Pos.CENTER);
        controlButtons.setSpacing(70);
        quitButton.setMinWidth(100);
        beginButton.setMinWidth(100);


        Image image1 = new Image("ship1.png");
        ImageView additionalImageView = new ImageView(image1);
        additionalImageView.setFitHeight(200);  // Set image height
        additionalImageView.setPreserveRatio(true);  // Preserve image ratio

        Image image2 = new Image("ship2.png");
        ImageView additionalImageView2 = new ImageView(image2);
        additionalImageView2.setFitHeight(200);  // Set image height
        additionalImageView2.setPreserveRatio(true);  // Preserve image ratio

        Image image3 = new Image("ship3.png");
        ImageView additionalImageView3 = new ImageView(image3);
        additionalImageView3.setFitHeight(200);  // Set image height
        additionalImageView3.setPreserveRatio(true);  // Preserve image ratio

        Image image4 = new Image("ship4.png");
        ImageView additionalImageView4 = new ImageView(image4);
        additionalImageView4.setFitHeight(200);  // Set image height
        additionalImageView4.setPreserveRatio(true);  // Preserve image ratio


        HBox images = new HBox(20, additionalImageView, additionalImageView2, additionalImageView3, additionalImageView4);

        quitButton.setOnAction(e ->  {
            Message toSend = new Message("TERMINATE_GAME", getPlayerName(), -1, "ending game");
            clientConnection.send(toSend);
            primaryStage.setScene(homeScreenScene);
        });

        beginButton.setOnAction(e -> {
            int valid = 0;
            for(int i = 0; i < 4 ; i++) {
                if(shipPlaced[i] == true) {
                    valid++;
                }
                else {
                    showAlert("Error","Ship "+(i+2)+" has not been placed");
                    break;
                }
            }
            if(valid == 4) {
                Message toSend = new Message("BOARD_SETUP", getPlayerName(), 0, "just board setup");
                clientConnection.send(toSend);
                primaryStage.setScene(compGameScreen2);
            }
        });


        VBox layout3 = new VBox(10, title, anotherTitle, instructions, comboBox, gridPane, images, controlButtons);

        layout3.setAlignment(Pos.TOP_CENTER);
        title.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 30px; -fx-font-weight: bold; -fx-fill: #D6DBDF;");
        anotherTitle.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #D6DBDF;");
        instructions.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-fill: #D6DBDF;");
        comboBox.setStyle("-fx-font-family: 'Arial'; -fx-background-color: #FF5733; -fx-text-fill: #1C2833;");
        quitButton.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        beginButton.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        layout3.setStyle("-fx-background-color: #1C2833; -fx-padding: 20;");

        compGameScreen1 = new Scene(layout3, 900, 800);

        // SCREEN 2

        Text Heading = new Text("BATTLESHIP");
        anotherHeading = new Text(getPlayerName() + "     vs     Computer");
        VBox headings = new VBox(Heading, anotherHeading);

        for (int i = 0; i < 7; i++)
        {
            for (int j = 0; j < 7; j++)
            {
                myBoardGrid.add(myBoardButtons1[i][j],j,i);
            }
        }

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                Button btn1 = new Button();
                btn1.setMinWidth(50);
                btn1.setMinHeight(50);
                btn1.setStyle("-fx-background-color: #00FFFF;");

                int finalI = i;
                int finalJ = j;

                btn1.setOnAction(e->{
                    moves[0] = finalI;
                    moves[1] = finalJ;
                    Message toSend = new Message("PLAYER_MOVE", getPlayerName(), 0, finalI, finalJ, myBoard);
                    clientConnection.send(toSend);
                    System.out.println("player move / hit" + moves[0]+ moves[1]);
                });
                opponentBoardGrid.add(btn1, j, i);
                opponentBoardButtons1[i][j] = btn1;
            }
        }

        HBox gridBox = new HBox(20, myBoardGrid, opponentBoardGrid);
        Button quitButton2 = new Button("Quit");



        quitButton2.setOnAction(e-> {

            Message toSend = new Message("TERMINATE_GAME", getPlayerName(), -1, "quitted");
            clientConnection.send(toSend);
            primaryStage.setScene(homeScreenScene);
        });

        VBox layout99 = new VBox(30, headings, gridBox, quitButton2);

        Heading.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 36px; -fx-font-weight: bold; -fx-fill: #D6DBDF;");
        anotherHeading.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 24px; -fx-fill: #D6DBDF;");
        headings.setAlignment(Pos.CENTER);
        gridBox.setAlignment(Pos.CENTER);
        layout99.setAlignment(Pos.CENTER);
        quitButton2.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        layout99.setStyle("-fx-background-color: #1C2833; -fx-padding: 20;");

        compGameScreen2 = new Scene(layout99, 900, 700);

        // -------------------------- WAITING SCREEN

        Label waitingLabel = new Label("Waiting for Opponent");

        // Images
        Image image221 = new Image("popeyes.png");
        Image image222 = new Image("sailor.jpeg");
        ImageView imageView1 = new ImageView(image221);
        ImageView imageView2 = new ImageView(image222);

        imageView1.setFitHeight(100);
        imageView1.setFitWidth(100);
        imageView2.setFitHeight(100);
        imageView2.setFitWidth(100);

        // Layout
        HBox imageBox = new HBox(imageView1, imageView2);
        imageBox.setSpacing(700); // Adjust spacing to place images in the bottom corners

        VBox layout4 = new VBox(20, waitingLabel, imageBox);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        waitingLabel.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #D6DBDF; -fx-text-alignment: center; -fx-padding: 20;");
        imageBox.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: #1C2833;");
        layout4.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: #1C2833;");

        waitingScreen = new Scene(layout4, 900, 500);

        waitingScreen.setFill(Color.valueOf("#1C2833"));

        // -------------------------- PvP GAME

        GridPane gridPanePVP = new GridPane(); //PVPgame init
        // SCREEN 1
        Text PVPTitle = new Text("Battleship!"); //title
        Text PlayerInfo = new Text(getPlayerName() + "vs PLAYER"); //anotherTitle
        Text Details = new Text("Place your ships"); //instructions

        ComboBox<String> DropBox = new ComboBox<>(); //comboBox
        DropBox.getItems().addAll("2", "3", "4", "5");
        DropBox.setValue("2");
        for(int i = 0; i < 7; i++) {
            for(int j = 0; j < 7; j++) {
                Button btn = new Button();
                btn.setMinWidth(50);
                btn.setMinHeight(50);
                btn.setStyle("-fx-background-color: #00FFFF;");

                Button tempbutton = new Button();
                tempbutton.setMinWidth(50);
                tempbutton.setMinHeight(50);
                tempbutton.setStyle("-fx-background-color: #00FFFF;");

                int finalI = i;
                int finalJ = j;

                AtomicInteger size = new AtomicInteger(Integer.parseInt(DropBox.getValue()));
                DropBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal.equals(oldVal)) {
                        size.set(Integer.parseInt(newVal));
                    }
                });

                btn.setOnAction(e -> handleButtonActionPVP(finalI, finalJ, size.get()));
                tempbutton.setOnAction(e -> handleButtonActionPVP(finalI, finalJ, size.get()));

                gridPanePVP.add(btn, j, i);

                buttons[i][j] = btn;

                myBoardButtons[i][j] = tempbutton;
            }
        }

        gridPanePVP.setAlignment(Pos.CENTER);

        Button quitButtonPVP = new Button("Quit");
        Button beginButtonPVP = new Button("Begin");
        HBox controlButtonsPVP = new HBox(quitButtonPVP, beginButtonPVP);
        controlButtonsPVP.setAlignment(Pos.CENTER);
        controlButtonsPVP.setSpacing(70);
        quitButtonPVP.setMinWidth(100);
        beginButtonPVP.setMinWidth(100);

        quitButtonPVP.setOnAction(e ->  {
            Message toSend = new Message("TERMINATE_GAME", getPlayerName(), -1, "ending game");
            clientConnection.send(toSend);
            primaryStage.setScene(homeScreenScene);
        });

        Image image1pvp = new Image("ship1.png");
        ImageView additionalImageViewpvp = new ImageView(image1pvp);
        additionalImageViewpvp.setFitHeight(200);  // Set image height
        additionalImageViewpvp.setPreserveRatio(true);  // Preserve image ratio

        Image image2pvp = new Image("ship2.png");
        ImageView additionalImageView2pvp = new ImageView(image2pvp);
        additionalImageView2pvp.setFitHeight(200);  // Set image height
        additionalImageView2pvp.setPreserveRatio(true);  // Preserve image ratio

        Image image3pvp = new Image("ship3.png");
        ImageView additionalImageView3pvp = new ImageView(image3pvp);
        additionalImageView3pvp.setFitHeight(200);  // Set image height
        additionalImageView3pvp.setPreserveRatio(true);  // Preserve image ratio

        Image image4pvp = new Image("ship4.png");
        ImageView additionalImageView4pvp = new ImageView(image4pvp);
        additionalImageView4pvp.setFitHeight(200);  // Set image height
        additionalImageView4pvp.setPreserveRatio(true);  // Preserve image ratio


        HBox imagespvp = new HBox(20, additionalImageViewpvp, additionalImageView2pvp, additionalImageView3pvp, additionalImageView4pvp);


        beginButtonPVP.setOnAction(e -> {
            int valid = 0;
            for(int i = 0; i < 4 ; i++) {
                if(shipPlaced[i] == true) {
                    valid++;
                }
                else {
                    showAlert("Error","Ship "+(i+2)+" has not been placed");
                    break;
                }
            }
            if(valid == 4) {
                Message toSend = new Message("BOARD_SETUP", getPlayerName(), 1,-1,-1, myBoard);
                clientConnection.send(toSend);
                primaryStage.setScene(pvpGameScreen2);
            }
        });

        VBox layoutPVP = new VBox(10, PVPTitle, PlayerInfo, Details, DropBox, gridPanePVP, imagespvp, controlButtonsPVP);
        PVPTitle.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 30px; -fx-font-weight: bold; -fx-fill: #D6DBDF;");
        PlayerInfo.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #D6DBDF;");
        Details.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-fill: #D6DBDF;");
        DropBox.setStyle("-fx-font-family: 'Arial'; -fx-background-color: #FF5733; -fx-text-fill: #1C2833;");
        quitButtonPVP.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        beginButtonPVP.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        layoutPVP.setStyle("-fx-background-color: #1C2833; -fx-padding: 20;");

        pvpGameScreen1 = new Scene(layoutPVP, 900, 700);

        //SCREEN 2
        Text HeadingPVP = new Text("BATTLESHIP");
        anotherHeading = new Text(getPlayerName() + "     vs     Player");
        VBox headingsPVP = new VBox(HeadingPVP, anotherHeading);

        for (int i = 0; i < 7; i++)
        {
            for (int j = 0; j < 7; j++)
            {
                myBoardGridPVP.add(myBoardButtons[i][j],j,i);
            }
        }

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                Button btn1 = new Button();
                btn1.setMinWidth(50);
                btn1.setMinHeight(50);
                btn1.setStyle("-fx-background-color: #00FFFF;");

                int finalI = i;
                int finalJ = j;

                btn1.setOnAction(e->{
                    moves[0] = finalI;
                    moves[1] = finalJ;
                    Message toSend = new Message("PLAYER_MOVE", getPlayerName(), 1, finalI, finalJ, myBoard);
                    clientConnection.send(toSend);
                    System.out.println("player move / hit" + moves[0]+ moves[1]);
                });
                opponentBoardGridPVP.add(btn1, j, i);
                opponentBoardButtons[i][j] = btn1;
            }
        }

        HBox gridBoxPVP = new HBox(20, myBoardGridPVP, opponentBoardGridPVP);
        Button quitButton2PVP = new Button("Quit");
        textBox = new TextField("...");
        textBox.setEditable(false);
        TextField messagingArea = new TextField("Talk to your opponent");
        
        Button send = new Button("Send");
        HBox messeages = new HBox(20, messagingArea, send);

        Button revealRow = new Button("Reveal Row");
        Button revealColumn = new Button("Reveal Column");
        revealRow.setOnAction(e -> {
            revealRow(myBoardGridPVP);
            revealRow.setDisable(true);
        });
        revealColumn.setOnAction(e -> {
            revealColumn(myBoardGridPVP);
            revealColumn.setDisable(true);
        });

        HBox powerups = new HBox(20, revealRow, revealColumn);

        send.setOnAction(e-> {

            if (messagingArea.getText().isEmpty()) {
                showAlert("Error", "Write something before messaging");
            }
            Message toSend = new Message("MESSAGING", getPlayerName(), 1, messagingArea.getText());
            clientConnection.send(toSend);

        });

        quitButton2PVP.setOnAction(e-> {

            Message toSend = new Message("TERMINATE_GAME", getPlayerName(), -1, "quitted");
            clientConnection.send(toSend);
            primaryStage.setScene(homeScreenScene);
        });

        VBox layout99PVP = new VBox(30, headingsPVP, gridBoxPVP, powerups,textBox, messeages, quitButton2PVP);

        HeadingPVP.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 36px; -fx-font-weight: bold; -fx-fill: #D6DBDF;");
        anotherHeading.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 24px; -fx-fill: #D6DBDF;");
        headingsPVP.setAlignment(Pos.CENTER);
        gridBoxPVP.setAlignment(Pos.CENTER);
        layout99PVP.setAlignment(Pos.CENTER);
        powerups.setAlignment(Pos.CENTER);
        revealRow.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        revealColumn.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        quitButton2PVP.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        send.setStyle("-fx-background-color: #FF5733; -fx-text-fill: #1C2833; -fx-font-family: 'Helvetica'; -fx-font-weight: bold;");
        layout99PVP.setStyle("-fx-background-color: #1C2833; -fx-padding: 20;");

        pvpGameScreen2 = new Scene(layout99PVP, 900, 700);


        // -------------------------- FINAL CMDS
        primaryStage.setScene(homeScreenScene);
        primaryStage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
