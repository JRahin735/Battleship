import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Server{

    int count = 1;
    private static final int PORT = 5555;
    ArrayList<ClientThread> allOnlineClients = new ArrayList<ClientThread>();
    ArrayList<Match> ongoingMatches = new ArrayList<Match>();

    private class Match {

        int gameMode = -1; // -1 not decided, 0 vs computer, 1 online play
        ClientThread player1 = null;
        ClientThread player2 = new ClientThread(new Socket(), -1);
        BattleshipAI computer = null;
        Board gameBoard1 = new Board(7);
        Board gameBoard2 = new Board(7);
        List<Integer> shipSizes = new ArrayList<>();

        public Match () {
            shipSizes.add(2);
            shipSizes.add(3);
            shipSizes.add(4);
            shipSizes.add(5);
        }
        public List<Integer> getShipSizes() {
            return shipSizes;
        }
    }

    TheServer server;
    private Consumer<Serializable> callback;

    Server(Consumer<Serializable> call){

        callback = call;
        server = new TheServer();
        server.start();
    }

    public class TheServer extends Thread{

        public void run() {

            try(ServerSocket mysocket = new ServerSocket(PORT);){

                System.out.println("Server started on port: " + PORT);

                while(true) {

                    ClientThread c = new ClientThread(mysocket.accept(), count);
                    callback.accept("client has connected to server: " + "client #" + count);
                    allOnlineClients.add(c);
                    c.start();

                    count++;
                }
            }

            catch(Exception e) {

                callback.accept("Server socket did not launch");
            }
        }
    }

    class ClientThread extends Thread{

        String clientName = "placeholderName";
        Socket connection;
        int count;
        ObjectInputStream in;
        ObjectOutputStream out;

        ClientThread(Socket s, int count){

            this.connection = s;
            this.count = count;
        }

        String getClientName () {return clientName;}

        void setClientName (String clientName) throws IOException {

            System.out.println("Setting Client Name");

            for (ClientThread client: allOnlineClients) {
                if (client.getClientName().equals(clientName)) {

                    // Username already exists
                    System.out.println("Username already existed");
                    Message toSend = new Message("JOIN", "SERVER", -1, "USERNAME ALREADY EXISTS");

                    for (int i = 0; i < allOnlineClients.size(); i++) {
                        if(allOnlineClients.get(i).getClientName().equals(clientName)){
                            ClientThread t = allOnlineClients.get(i);
                            t.out.writeObject(toSend);
                            break;
                        }
                    }
                    return;
                }
            }
            this.clientName = clientName;

            Message toSend = new Message("JOIN", "SERVER", -1, clientName);
            for (int i = 0; i < allOnlineClients.size(); i++) {
                if(allOnlineClients.get(i).getClientName().equals(clientName)){
                    ClientThread t = allOnlineClients.get(i);
                    t.out.writeObject(toSend);
                    break;
                }
            }

            System.out.println("Client name set");
        }

        private Match computerMatchInit (String playerName) {

            Match game = new Match();
            game.gameMode = 0;

            for (ClientThread t: allOnlineClients) {
                if (t.getClientName().equals(playerName)) {
                    game.player1 = t;
                    break;
                }
            }
            return game;
        }

        void declareWinner (String clientName, String winnerName) throws IOException {

            Message toSend = new Message("DECLARE_WINNER", "SERVER", -1, winnerName);
            for (int i = 0; i < allOnlineClients.size(); i++) {
                if(allOnlineClients.get(i).getClientName().equals(clientName)){
                    ClientThread t = allOnlineClients.get(i);
                    t.out.writeObject(toSend);
                    break;
                }
            }
        }

        void sendBoard (String clientName, String boardOwner, Board boardToSend, int x, int y, int gameMode) throws IOException {

            String decision = "";
            if (boardToSend.getCell(x, y).isHit() && boardToSend.getCell(x, y).hasShip()) {
                decision = "HIT";
            }
            else {
                decision = "MISS";
            }


            Message toSend = new Message("SEND_BOARD", boardOwner, gameMode, x, y, decision);
            for (int i = 0; i < allOnlineClients.size(); i++) {
                if(allOnlineClients.get(i).getClientName().equals(clientName)){
                    ClientThread t = allOnlineClients.get(i);
                    t.out.writeObject(toSend);
                    break;
                }
            }
        }

        void notifyTermination (String clientName) throws IOException {

            Message toSend = new Message("TERMINATE_GAME", "SERVER", -1, "GAME ENDED");
            for (int i = 0; i < allOnlineClients.size(); i++) {
                if(allOnlineClients.get(i).getClientName().equals(clientName)){
                    ClientThread t = allOnlineClients.get(i);
                    t.out.writeObject(toSend);
                    break;
                }
            }
        }

        void notifyWaitOver (String clientName, String opponentName) throws IOException {

            Message toSend = new Message("WAIT_OVER", "SERVER", 1, opponentName);
            for (int i = 0; i < allOnlineClients.size(); i++) {
                if(allOnlineClients.get(i).getClientName().equals(clientName)){
                    ClientThread t = allOnlineClients.get(i);
                    t.out.writeObject(toSend);
                    break;
                }
            }
        }

        void sendMessage (String clientName, String opponentName, String content) throws IOException {

            Message toSend = new Message("MESSAGING", clientName, 1, content);
            for (int i = 0; i < allOnlineClients.size(); i++) {
                if(allOnlineClients.get(i).getClientName().equals(opponentName)){
                    ClientThread t = allOnlineClients.get(i);
                    t.out.writeObject(toSend);
                    break;
                }
            }
        }

        public void run(){

            try {
                in = new ObjectInputStream(connection.getInputStream());
                out = new ObjectOutputStream(connection.getOutputStream());
                connection.setTcpNoDelay(true);
            }
            catch(Exception e) {
                System.out.println("Streams not open");
            }


            while(true) {
                try {

                    Message data = (Message) in.readObject();

                    if (data.getType().equals("JOIN")) {

                        // New user joined
                        setClientName(data.getMessage());
                        if (!clientName.equals("placeholderName")) {
                            callback.accept("client: " + count + " named themselves: " + data.getMessage());
                        }
                        else {
                            callback.accept("client: " + count + "needs to name themselves again");
                        }
                    }
                    else if (data.getType().equals("GAME_CREATION")) {

                        // COMPUTER GAME
                        if (data.getGameMode() == 0) {
                            callback.accept("player: " + data.getSender() + "chose comp game");
                            ongoingMatches.add(computerMatchInit(data.getSender()));
                        }
                        // PvP GAME
                        else if (data.getGameMode() == 1) {
                            callback.accept("player: " + data.getSender() + "chose pvp game");

                            int gamefound = 0;
                            for (Match m: ongoingMatches) {

                                if (m.gameMode == 0) {continue;}

                                // PvP match exists with one player waiting
                                if (m.player1 != null && m.gameMode == 1) {

                                    gamefound = 1;

                                    for (ClientThread t: allOnlineClients) {
                                        if (t.getClientName().equals(data.getSender())) {
                                            m.player2 = t;

                                            notifyWaitOver(m.player1.getClientName(), data.getSender());
                                            notifyWaitOver(data.getSender(), m.player1.getClientName());
                                            break;
                                        }
                                    }
                                }
                            }

                            // creating new match
                            if (gamefound == 0) {

                                Match match = new Match();

                                for (ClientThread t : allOnlineClients) {
                                    if (t.getClientName().equals(data.getSender())){
                                        match.player1 = t;
                                        break;
                                    }
                                }

                                match.gameMode = 1;
                                ongoingMatches.add(match);
                            }
                        }
                        // SMTH WRONG
                        else {
                            callback.accept("player: " + data.getSender() + "sent gamemode: " + data.getGameMode() + " which is not valid.");
                        }
                    }
                    else if (data.getType().equals("BOARD_SETUP")) {
                        callback.accept("player: " + data.getSender() + " is setting up their board. On mode: " + data.getGameMode());

                        // fetch match played by this player
                        for (Match m: ongoingMatches) {
                            if (m.player1.getClientName().equals(data.getSender()) || m.player2.getClientName().equals(data.getSender())) {

                                // COMPUTER GAME
                                if (data.getGameMode() == 0) {

                                    m.computer = new BattleshipAI(m.gameBoard2, m.gameBoard1);
                                    m.computer.placeShipsRandomly(m.getShipSizes());
                                    m.gameBoard2 = m.computer.getMyBoard();
                                }
                                // PvP GAME
                                else if (data.getGameMode() == 1) {

                                    if (m.player1.getClientName().equals(data.getSender())) {
                                        m.gameBoard1 = data.getBoard();
                                        m.gameBoard1.printBoard();

                                    }
                                    else if (m.player2.getClientName().equals(data.getSender())) {
                                        m.gameBoard2 = data.getBoard();
                                        m.gameBoard2.printBoard();

                                    }
                                }
                                // SMTH WRONG
                                else {
                                    callback.accept("player: " + data.getSender() + "sent gamemode: " + data.getGameMode() + " which is not valid.");
                                }
                                break;
                            }
                        }
                    }
                    else if (data.getType().equals("PLAYER_MOVE")) {
                        callback.accept("player: " + data.getSender() + " is hitting cell: " + data.getX()+data.getY());

                        // fetch match played by this player
                        for (Match m: ongoingMatches) {
                            if (m.player1.getClientName().equals(data.getSender()) || m.player2.getClientName().equals(data.getSender())) {
                                    System.out.println("PlayerMoveCalledBy:"+m.player1.getClientName());
                                // COMPUTER GAME
                                if (data.getGameMode() == 0) {

                                    // player's turn
                                    m.computer.setOpponentBoard(data.getBoard());
                                    m.gameBoard1 = data.getBoard();

                                    m.gameBoard2 = m.computer.getMyBoard();
                                    m.gameBoard2.shootAt(data.getX(), data.getY());
                                    m.computer.setMyBoard(m.gameBoard2);

                                    if (m.gameBoard2.allShipsSunk()) {
                                        declareWinner(data.getSender(), data.getSender());
                                    }

                                    // computer's turn
                                    int[] move= m.computer.chooseTarget();
                                    System.out.println(move[0] + move[1] + "comp target");
                                    m.computer.shootAt(move[0], move[1]);

                                    m.gameBoard1 = m.computer.getOpponentBoard();
                                    m.gameBoard2 = m.computer.getMyBoard();

                                    if (m.gameBoard1.allShipsSunk()) {
                                        declareWinner(data.getSender(), "COMPUTER");
                                    }

                                    sendBoard(data.getSender(), data.getSender(), m.gameBoard1, move[0], move[1], 0);
                                    sendBoard(data.getSender(), "Computer", m.gameBoard2, data.getX(), data.getY(), 0);
                                }
                                // PvP GAME
                                else if (data.getGameMode() == 1) {
                                    System.out.println("In PVP mode");
                                    // for player 1
                                    if (m.player1.getClientName().equals(data.getSender())) {
                                        System.out.println("Shot by"+ m.player1.getClientName()+"at:"+data.getX()+data.getY());
                                        m.gameBoard1 = data.getBoard();
                                        m.gameBoard2.shootAt(data.getX(), data.getY());
                                        sendBoard(m.player1.getClientName(), m.player2.getClientName(), m.gameBoard2, data.getX(), data.getY(), 1);
                                        sendBoard(m.player2.getClientName(), m.player2.getClientName(), m.gameBoard2, data.getX(), data.getY(), 1);
                                    }
                                    // for player 2
                                    else if (m.player2.getClientName().equals(data.getSender())) {
                                        System.out.println("Shot by"+ m.player2.getClientName()+"at:"+data.getX()+data.getY());
                                        m.gameBoard2 = data.getBoard();
                                        m.gameBoard1.shootAt(data.getX(), data.getY());
                                        sendBoard(m.player1.getClientName(), m.player1.getClientName(), m.gameBoard1, data.getX(), data.getY(), 1);
                                        sendBoard(m.player2.getClientName(), m.player1.getClientName(), m.gameBoard1, data.getX(), data.getY(), 1);
                                    }

                                    // when someone wins
                                    if (m.gameBoard1.allShipsSunk()) {
                                        declareWinner(m.player1.getClientName(), m.player2.getClientName());
                                        declareWinner(m.player2.getClientName(), m.player2.getClientName());
                                    }
                                    if (m.gameBoard2.allShipsSunk()) {
                                        declareWinner(m.player1.getClientName(), m.player1.getClientName());
                                        declareWinner(m.player2.getClientName(), m.player1.getClientName());
                                    }
                                }
                                // SMTH WRONG
                                else {
                                    callback.accept("player: " + data.getSender() + "sent gamemode: " + data.getGameMode() + " which is not valid.");
                                }
                                break;
                            }
                        }
                    }
                    else if (data.getType().equals("TERMINATE_GAME")) {

                        callback.accept("player: " + data.getSender() + " terminated the game");

                        // fetch match played by this player
                        int i = 0;
                        for (Match m: ongoingMatches) {
                            if (m.player1.getClientName().equals(data.getSender()) || m.player2.getClientName().equals(data.getSender())) {

                                ongoingMatches.remove(i);
                                notifyTermination(m.player1.getClientName());
                                notifyTermination(m.player2.getClientName());

                                break;
                            }
                            i++;
                        }

                    }
                    else if (data.getType().equals("MESSAGING")) {

                        for (Match m: ongoingMatches) {
                            if (m.player1.getClientName().equals(data.getSender())) {

                                sendMessage(data.getSender(), m.player2.getClientName(), data.getMessage());
                            }
                            else if (m.player2.getClientName().equals(data.getSender())) {
                                sendMessage(data.getSender(), m.player1.getClientName(), data.getMessage());
                            }
                        }

                    }
                }
                catch(Exception e) {
                    callback.accept("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");
                    e.printStackTrace();
                    allOnlineClients.remove(this);
                    break;
                }
            }
        }//end of run
    }//end of client thread
}
