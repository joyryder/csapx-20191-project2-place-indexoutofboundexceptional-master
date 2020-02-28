package place;

import java.util.Observable;

/**
 * PlaceBoardObservable is an easy way at grabbing information from the board as needed.
 * @author Miguel Rosario
 * @author Joey Saltalamacchia
 * @author Shrif Rai
 */
public class PlaceBoardObservable extends Observable {

    /**Creates a new Place Board*/
    private PlaceBoard board;

    /**Grabs the dimensions of the board*/
    private int DIM;

    /**Create a new board that has a board and a dimension*/
    public void createBoard(PlaceBoard board) {
        this.board = board;
        this.DIM = this.board.DIM;
    }

    /**
     * Grabs the dimensions of the board.
     * @return DIM dim
     */
    public int getDIM() {
        return this.DIM;
    }


    /**
     * Get the tile at the current spot at row / col positioning
     * @param row row
     * @param col column
     * @return the location of the tile
     */
    public PlaceTile getTile(int row, int col) {
        return this.board.getTile(row, col);
    }

    /**
     * grabs the entire board
     * @return board
     */
    public PlaceBoard getBoard() {
        return this.board;
    }

    /**
     * sets the tile and then shows the move from the observer
     * @param tile tile
     */
    public void moveDisplay(PlaceTile tile){
        this.board.setTile(tile);
        super.setChanged();
        super.notifyObservers(tile);
    }
}
