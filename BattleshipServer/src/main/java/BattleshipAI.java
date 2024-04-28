import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattleshipAI {

    private Board myBoard = new Board(7);
    private Board opponentBoard = new Board(7);
    private List<int[]> targetList = new ArrayList<>(); // Used to store potential targets after a hit
    private boolean hunting = false; // Indicates if the AI is in hunting mode

    public BattleshipAI(Board myBoard, Board opponentBoard) {
        this.myBoard = myBoard;
        this.opponentBoard = opponentBoard;
        this.targetList = new ArrayList<>();
        this.hunting = false;
    }

    public void setMyBoard(Board myBoard) {this.myBoard = myBoard;}
    public void setOpponentBoard(Board opponentBoard) {this.opponentBoard = opponentBoard;}
    public Board getMyBoard() {return myBoard;}
    public Board getOpponentBoard() {return opponentBoard;}

    public void placeShipsRandomly(List<Integer> shipSizes) {

        Random random = new Random();
        for (Integer size: shipSizes) {
            boolean placed = false;
            while (!placed) {
                int x = random.nextInt(opponentBoard.getSize());
                int y = random.nextInt(opponentBoard.getSize());
                placed = myBoard.placeShip(x, y, size, random.nextBoolean());
            }
        }

        myBoard.printBoard();
    }

    public int[] chooseTarget() {

        Random random = new Random();
        int x, y;

        if (hunting && !targetList.isEmpty()) {

            int[] target = targetList.remove(0);
            x = target[0];
            y = target[1];
        }
        else {

            do {
                x = random.nextInt(6);
                y = random.nextInt(6);
            } while (opponentBoard.getCell(x, y).isHit()); // Ensure we don't hit the same spot twice
        }

        System.out.println("x" +x + "y" + y + "in AI");
        return new int[]{x, y};
    }

    public void shootAt(int x, int y) {

        if (opponentBoard.shootAt(x, y) && !hunting) {

            hunting = true;
            addSurroundingCells(x, y);
        }
        else if (!opponentBoard.getCell(x, y).hasShip()) {

            hunting = false; // Stop hunting if it was a miss
        }
    }

    private void addSurroundingCells(int x, int y) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];
            if (newX >= 0 && newX < opponentBoard.getSize() && newY >= 0 && newY < opponentBoard.getSize()) {
                targetList.add(new int[]{newX, newY});
            }
        }
    }

}
