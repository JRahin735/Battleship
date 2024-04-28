import java.io.Serializable;

public class BoardCell implements Serializable {

    static final long serialVersionUID = 42L;
    private boolean hasShip;
    private boolean isHit;

    public BoardCell() {
        this.hasShip = false;
        this.isHit = false;
    }

    public boolean hasShip() {
        return hasShip;
    }

    public void setShip(boolean hasShip) {
        this.hasShip = hasShip;
    }

    public boolean isHit() {
        return isHit;
    }

    public void setHit(boolean hit) {
        isHit = hit;
    }

}