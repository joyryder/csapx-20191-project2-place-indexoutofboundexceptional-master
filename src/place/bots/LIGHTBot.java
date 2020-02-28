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
_\/_                 |                _\/_
/o\\             \       /            //o\
 |                 .---.                |
_|_______     --  /     \  --     ______|__
         `~^~^~^~^~^~^~^~^~^~^~^~`
 */
/**
 * A bot class that attempts to "erase" (set to white) a spot that has currently been colored/tile.
 * @author Miguel Rosario
 */
class LIGHTBot extends Thread{

    private ObjectOutputStream out;
    private String name;

    /**
     * Constructor for the LIGHTBot. Can have a theoretical infinite amount of names.
     * @param o objectoutputstream
     * @param n string
     */
    private LIGHTBot(ObjectOutputStream o, String n) {
        out = o;
        //Appending a number to the end of the name so almost 0 conflict when creating multiple
        name = n+(Math.random()*100);
    }

    /**
     * Adds the move to the board after waiting .5s
     * @param row row
     * @param col col
     */
    private void addToBoard(int row, int col) {
        //wait .5s and then
        try {
            sleep(500);
        } catch(InterruptedException ignored) {

        }
        //write the changes to the board  (row/col/name/WHITE)
        sendToBoard(out,new PlaceRequest<>(CHANGE_TILE,new PlaceTile(row,col,name,PlaceColor.WHITE)));
    }

    /**
     * Method the sends the information to the board so it can be placed and then flushed
     * @param out objectoutputstream
     * @param placeRequest placeRequest
     */
    private static void sendToBoard(ObjectOutputStream out, PlaceRequest<java.io.Serializable> placeRequest) {
        try {
            out.writeUnshared(placeRequest);
            out.flush();
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This main method runs the bot until closed.
     * @param args
     */
    public static void main(String[] args) {
        if(args.length != 2) {
            System.err.println("usage $ java LIGHTBot host port");
            System.exit(1);
        }
        System.out.println("Connection success to place.");
        try(Socket con = new Socket(args[0],Integer.parseInt(args[1]));
            ObjectOutputStream out = new ObjectOutputStream(con.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(con.getInputStream())){

            String name = "ERASE"+System.currentTimeMillis();
            sendToBoard(out,new PlaceRequest<>(LOGIN,name));

            LIGHTBot bot = new LIGHTBot(out,name);
            while(true) {
                if(((PlaceRequest)in.readUnshared()).getType().equals(TILE_CHANGED)) {
                    PlaceTile tile = (PlaceTile)((PlaceRequest)in.readUnshared()).getData();
                    int col = tile.getCol();
                    int row = tile.getRow();
                    bot.addToBoard(row,col);
                }
            }
        } catch (IOException | ClassNotFoundException ignored) {
        }
    }

}