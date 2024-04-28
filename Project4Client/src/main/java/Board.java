import javafx.scene.control.Cell;

import java.io.Serializable;

public class Board implements Serializable {

    static final long serialVersionUID = 42L;

    private final int size;
    private BoardCell[][] grid;

    public Board(int size) {
        this.size = size;
        grid = new BoardCell[size][size];
        initializeBoard();
    }

    public int getSize () {return size;}

    public BoardCell getCell (int x, int y) {
        if (x > size || y > size) {
            return null;
        }
        else {
            return grid[x][y];
        }
    }

    private void initializeBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                grid[i][j] = new BoardCell();
            }
        }
    }

    public boolean placeShip(int x, int y, int length, boolean horizontal) {
        if (horizontal) {
            if (x + length > size) return false; // Check bounds
            for (int i = 0; i < length; i++) {
                if (grid[x + i][y].hasShip()) return false; // Check overlap
            }
            for (int i = 0; i < length; i++) {
                grid[x + i][y].setShip(true);
            }
        } else {
            if (y + length > size) return false; // Check bounds
            for (int i = 0; i < length; i++) {
                if (grid[x][y + i].hasShip()) return false; // Check overlap
            }
            for (int i = 0; i < length; i++) {
                grid[x][y + i].setShip(true);
            }
        }
        return true;
    }

    public boolean shootAt(int x, int y) {

        if (!grid[x][y].isHit()) {
            grid[x][y].setHit(true);
            if (grid[x][y].hasShip()) {
                System.out.println("Hit!");
                return true;
            }
            System.out.println("Miss!");
        }
        return false;
    }

    public boolean allShipsSunk() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (grid[i][j].hasShip() && !grid[i][j].isHit()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void printBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.print(grid[i][j].isHit() ? (grid[i][j].hasShip() ? "X " : "- ") : "O ");
            }
            System.out.println();
        }
    }

}
