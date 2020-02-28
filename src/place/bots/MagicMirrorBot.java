package place.bots;

import place.PlaceColor;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static place.network.PlaceRequest.RequestType.*;
/*
        _________________________
       (, ______________________ )
       | |                      ||
       | |                      ||
       | |                      ||
       | |                      ||
       | |                      ||
       | |                      ||
       | |                      ||
       | |                      ||
       | |                      ||
       | |                      ||
       | |                      ||
       | |                      ||
       | |                      ||
       | |______________________||
   .---('________________________)--.
   |____          __________       _|
    |___|   -o-  |       |__|  -o- |
    |___|   -o-  |       |__|  -o- |
        |________|       |__|______|

 */
/**
 * A rehash of the light/darkness bots that instead of replacing the tile
 * you most recently placed, will instead mirror the move made with the
 * same color.
 *
 * @author Miguel Rosario
 */
class MagicMirrorBot extends Thread{

    private ObjectOutputStream out;
    private String name;

    /**
     * Constructor for the MagicMirrorBot that have an
     * objectoutputstream and a string for the name
     * @param o ObjOutS
     * @param n name
     */
    private MagicMirrorBot(ObjectOutputStream o, String n) {
        out = o;
        name = n+(Math.random()*100);
    }

    /**
     * Method that adds the tile to the board.
     * @param row row
     * @param col col
     * @param color tile color
     */
    private void addToBoard(int row, int col, PlaceColor color) {
        sendToBoard(out,new PlaceRequest<>(CHANGE_TILE,new PlaceTile(row,col,name,color)));
    }

    /**
     * Method that sends the information to the board so it can get written.
     * @param out ObjOutS
     * @param placeRequest Place
     */
    private static void sendToBoard(ObjectOutputStream out, PlaceRequest<java.io.Serializable> placeRequest) {
        try {
            out.writeUnshared(placeRequest);
            out.flush();
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    /**
     * This main is what runs the bot and processes the moves.
     * @param args host / port
     */
    public static void main(String[] args) {
        if(args.length != 2) {
            System.err.println("usage $ java MagicMirrorBot host port");
            System.exit(1);
        }
        try(Socket con = new Socket(args[0],Integer.parseInt(args[1]));
            ObjectOutputStream out = new ObjectOutputStream(con.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(con.getInputStream())) {

            String name = "OCEAN"+(Math.random()*100);
            sendToBoard(out,new PlaceRequest<>(LOGIN,name));

            MagicMirrorBot magicMirrorBot = new MagicMirrorBot(out,name);
            while(true) {
                //while running if the tile has been changed, grab its row and col
                if(((PlaceRequest)in.readUnshared()).getType().equals(TILE_CHANGED)) {
                    //grab the the tile
                    PlaceTile tile = (PlaceTile)((PlaceRequest)in.readUnshared()).getData();
                    int row, col;
                    row = tile.getCol();
                    col = tile.getRow();
                    //grab the color so it can match the input.
                    PlaceColor color = tile.getColor();
                    magicMirrorBot.addToBoard(row,col,color);
                }
            }
        } catch (IOException | ClassNotFoundException ignored) {
        }
    }
}
