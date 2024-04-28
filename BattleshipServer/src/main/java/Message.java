import java.io.Serializable;

public class Message implements Serializable {

    static final long serialVersionUID = 42L;

    private String type = "null"; // JOIN, GAME_CREATION, BOARD_SETUP, PLAYER_MOVE, SEND_BOARD, DECLARE_WINNER, TERMINATE_GAME, WAIT_OVER, MESSAGING
    private String sender = "null";
    private int gameMode = -1; // -1 not decided, 0 vs computer, 1 online play
    private String message = "null";
    private int x = -1;
    private int y = -1;
    private Board board = new Board(7);


    // for game creation and logistics
    public Message (String type, String sender, int gameMode, String message) {

        this.type = type;
        this.sender = sender;
        this.gameMode = gameMode;
        this.message = message;
    }

    // for game
    public Message (String type, String sender, int x, int y, Board board) {

        this.type = type;
        this.sender = sender;
        this.x = x;
        this.y = y;
        this.board = board;
    }

    public Message (String type, String sender, int gameMode, int x, int y, String message) {

        this.type = type;
        this.sender = sender;
        this.gameMode = gameMode;
        this.x = x;
        this.y = y;
        this.message = message;
    }

    // for sending board over the network
    public Message (String type, String sender, String message, Board board) {

        this.type = type;
        this.sender = sender;
        this.message = message;
        this.board = board;
    }

    public String getType() { return type; }
    public String getSender() { return sender; }
    public int getGameMode() {return gameMode;}
    public String getMessage() {return message;}

    public int getX() {return x;}
    public int getY() {return y;}

    public Board getBoard() {return board;}
}