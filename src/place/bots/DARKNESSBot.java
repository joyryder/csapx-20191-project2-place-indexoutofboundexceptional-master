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
                                _,.-------.,_
                            ,;~'             '~;,
                          ,;                     ;,
                         ;                         ;
                        ,'                         ',
                       ,;                           ;,
                       ; ;      .           .      ; ;
                       | ;   ______       ______   ; |
                       |  `/~"     ~" . "~     "~\'  |
                       |  ~  ,-~~~^~, | ,~^~~~-,  ~  |
                        |   |        }:{        |   |
                        |   l       / | \       !   |
                        .~  (__,.--" .^. "--.,__)  ~.
                        |     ---;' / | \ `;---     |
                         \__.       \/^\/       .__/
                          V| \                 / |V
       __                  | |T~\___!___!___/~T| |                  _____
    .-~  ~"-.              | |`IIII_I_I_I_IIII'| |               .-~     "-.
   /         \             |  \,III I I I III,/  |              /           Y
  Y          ;              \   `~~~~~~~~~~'    /               i           |
  `.   _     `._              \   .       .   /               __)         .'
    )=~         `-.._           \.    ^    ./           _..-'~         ~"<_
 .-~                 ~`-.._       ^~~~^~~~^       _..-'~                   ~.
/                          ~`-.._           _..-'~                           Y
{        .~"-._                  ~`-.._ .-'~                  _..-~;         ;
 `._   _,'     ~`-.._                  ~`-.._           _..-'~     `._    _.-
    ~~"              ~`-.._                  ~`-.._ .-'~              ~~"~
  .----.            _..-'  ~`-.._                  ~`-.._          .-~~~~-.
 /      `.    _..-'~             ~`-.._                  ~`-.._   (        ".
Y        `=--~                  _..-'  ~`-.._                  ~`-'         |
|                         _..-'~             ~`-.._                         ;
`._                 _..-'~                         ~`-.._            -._ _.'
   "-.="      _..-'~                                     ~`-.._        ~`.
    /        `.                                                ;          Y
   Y           Y                                              Y           |
   |           ;                                              `.          /
   `.       _.'                                                 "-.____.-'
     ~-----"

 */
/**
 * A rehash of the EraseBot that just makes everything you input black.
 * @author Miguel Rosario
 */
class DARKNESSBot extends Thread{

    private ObjectOutputStream out;
    private String name;

    /**
     * Constructor for the EraseBot. Can have a theoretical infinite amount of names.
     * @param o objectoutputstream
     * @param n string
     */
    private DARKNESSBot(ObjectOutputStream o, String n) {
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
        //write the changes to the board  (row/col/name/BLACK)
        sendToBoard(out,new PlaceRequest<>(CHANGE_TILE,new PlaceTile(row,col,name,PlaceColor.BLACK)));
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
            System.out.println("usage $ java EraseBot host port");
            System.exit(1);
        }
        System.out.println("Connection success to place.");
        try(Socket con = new Socket(args[0],Integer.parseInt(args[1]));
            ObjectOutputStream out = new ObjectOutputStream(con.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(con.getInputStream())){

            String name = "DARKNESS"+(Math.random()*100);
            sendToBoard(out,new PlaceRequest<>(LOGIN,name));

            DARKNESSBot bot = new DARKNESSBot(out,name);
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