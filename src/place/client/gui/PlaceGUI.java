package place.client.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import place.PlaceBoardObservable;
import place.PlaceColor;
import place.PlaceException;
import place.PlaceTile;
import place.network.NetworkClient;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

//kminu
/**
 * PlaceGui class is the class that converts what we already have done to the PTUI
 * into a full fledged accessible UI based version of the same game.
 * The users of this application will be able to select a color based on mouse input
 * and then click onto a spot on the grid that pops up to draw a color based on
 * personal preference.
 * @author Miguel Rosario
 * @author Joey Saltalamacchia
 * @author Shrif Rai
 */
public class PlaceGUI extends Application implements Observer/*Observer<ClientModel, PlaceTile>*/ {

    /**the size of one side of a tile*/
    private final int TILE_SIZE = 64;


    /**Grabs the username of the player*/
    private String username;

    /**Grabs an observable board that can be updated.*/
    private PlaceBoardObservable model;

    /**The stage for the gui*/
    private Stage window;

    /**The grid being used to hold all of the panes/tiles*/
    private GridPane grid;

    /**creates a network client that is used to connect to a server*/
    private NetworkClient serverConn;

    /**Creates a boolean value, running, true/false*/
    private boolean running;

    /**Variable that holds the information of the color that's currently selected*/
    private PlaceColor currentColor;

    /***
     * Basic method to initialize the gui
     * @param primaryStage
     */
    @Override
    public synchronized void start(Stage primaryStage) throws Exception {
        this.model.addObserver(this);

        //set the stage
        this.window = primaryStage;

        //Display the username of the place and name of the game.
        this.window.setTitle("Place: "+username);

        //Create a borderpane to gold the grid
        BorderPane mainPane = new BorderPane();

        //set init color to black
        this.currentColor = PlaceColor.BLACK;

        //start a new grid
        this.grid = new GridPane();

        //creates a board with the proper amount of rows/cols
        this.setBoard();

        HBox bottom = this.setBottm();

        mainPane.setCenter(this.grid);
        mainPane.setBottom(bottom);
        Scene scene = new Scene(mainPane);
        this.window.setScene(scene);


        primaryStage.show();
    }

    /**
     * Creates the HBox that makes up the line of selectable colors for the place game
     * @return the bottom pane for the user's display
     */
    private synchronized HBox setBottm()
    {
        //Bottom gui element that allows for color selection
        HBox bottom = new HBox();

        StackPane black = this.createBottomSpace(PlaceColor.BLACK);
        StackPane grey = this.createBottomSpace(PlaceColor.GRAY);
        StackPane silver = this.createBottomSpace(PlaceColor.SILVER);
        StackPane white = this.createBottomSpace(PlaceColor.WHITE);
        StackPane maroon = this.createBottomSpace(PlaceColor.MAROON);
        StackPane red = this.createBottomSpace(PlaceColor.RED);
        StackPane olive = this.createBottomSpace(PlaceColor.OLIVE);
        StackPane yellow = this.createBottomSpace(PlaceColor.YELLOW);
        StackPane green = this.createBottomSpace(PlaceColor.GREEN);
        StackPane lime = this.createBottomSpace(PlaceColor.LIME);
        StackPane teal = this.createBottomSpace(PlaceColor.TEAL);
        StackPane aqua = this.createBottomSpace(PlaceColor.AQUA);
        StackPane navy = this.createBottomSpace(PlaceColor.NAVY);
        StackPane blue = this.createBottomSpace(PlaceColor.BLUE);
        StackPane purple = this.createBottomSpace(PlaceColor.PURPLE);
        StackPane fuchsia = this.createBottomSpace(PlaceColor.FUCHSIA);

        bottom.getChildren().addAll(black,grey,silver,white,maroon,
                red,olive,yellow,green,lime,teal,
                aqua,navy,blue,purple,fuchsia);

        //Returns the bottom gui element
        return(bottom);

     }

    /**
     * A helper method for filling the bottom pane of the place board, returning
     * a tile used to change the user's color to that of the chosen tile
     *
     * @param color the color of this tile
     * @return a StackPane that is the color that it will change you user's color to
     */
    private synchronized StackPane createBottomSpace(PlaceColor color)
    {
        int displayNumber = color.getNumber();
        String textHolder = String.valueOf(color.getNumber());

        //set the bottom buttons to be the relative to the size of the window
        //we use 17 because it's the number of colors plus one, when using the number of colors
        //it occasionally stretches the window
        double bottomSize = TILE_SIZE * this.grid.getColumnCount() / 17;

        //establish the physical proportions of the rectangle
        Rectangle rectangle = new Rectangle(bottomSize, bottomSize);

        rectangle.setArcHeight(7.0d);
        rectangle.setArcWidth(7.0d);
        //establish the color gradient
        Stop[] stops = new Stop[] { new Stop(.35, Color.rgb(color.getRed(),color.getGreen(),color.getBlue())),
                                    new Stop(1, Color.BLACK)};
        LinearGradient lg1 = new LinearGradient(0, 0, 2, 0, true, CycleMethod.NO_CYCLE, stops);

        rectangle.setFill(lg1);

        //chnages the higher numbers to the letter characters the project wanted
        if(displayNumber > 9)
        {
            switch(displayNumber) {
                case 10:
                    textHolder = "a";
                    break;
                case 11:
                    textHolder = "b";
                    break;
                case 12:
                    textHolder = "c";
                    break;
                case 13:
                    textHolder = "d";
                    break;
                case 14:
                    textHolder = "e";
                    break;
                case 15:
                    textHolder = "f";
                    break;
                default:
                    System.out.println("What? how did you get here?");
            }
        }
        Text text = new Text(textHolder);

        //certain colors need to have certain color test in order for it to be legible
        if(color == PlaceColor.GRAY || color == PlaceColor.SILVER ||
                color == PlaceColor.WHITE || color == PlaceColor.AQUA ||
                color == PlaceColor.YELLOW || color == PlaceColor.LIME)
        {
            text.setFill(Color.BLACK);
        }
        else {
            text.setFill(Color.WHITE);
        }
        text.setFont(Font.font("Verdana", FontWeight.BOLD, 12));

        //the stack pane is used to put the text on the tile
        StackPane stack = new StackPane();
        stack.getChildren().addAll(rectangle,text);

        //change the user's color when the rectangle is clicked
        stack.setOnMouseClicked(mouseEvent -> {
            System.out.println(color);
            setCurrentColor(color);
        });


        return stack;
    }

    /**
     * thread that's held to set a current color.
     * @param newColor color
     */
    private synchronized void setCurrentColor(PlaceColor newColor)
    {
        this.currentColor = newColor;
    }

    /**
     * A method to convert PlaceTile's getTime() to Standard Date and Time format
     * @param milliseconds sec
     * @return month day year hour minute second
     */
    private synchronized String fromatTime(long milliseconds)
    {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR)+12;
        int minute =calendar.get(Calendar.MINUTE);
        int second =calendar.get(Calendar.SECOND);

        return month+"\\"+day+"\\"+year+"\n"+hour+":"+minute+":"+second;
    }

    /**
     * A helper method for creating the initial board state whe a player first logs onto
     * a Place Server
     */
    private synchronized void setBoard()
    {
        //Starts the creation of the board by getting the dimensions of the model
        //and then creating rows/cols
        for(int row = 0; row < model.getDIM(); row++)
            for(int col = 0; col < model.getDIM(); col++) {
                PlaceTile tempTile = model.getTile(row,col);
                PlaceColor tempColor = tempTile.getColor();

                Rectangle rectangle = createTile(tempColor);

                //adds the tool tip
                Tooltip tileInfo = new Tooltip("("+tempTile.getRow()+","+tempTile.getCol()+")\n"+tempTile.getOwner()+
                        "\n"+fromatTime(tempTile.getTime()));
                Tooltip.install(rectangle, tileInfo);

                this.grid.add(rectangle, row, col);
            }
    }

    /**
     * A helper method for generating the rectangles used in the main grid of the
     * Place game.
     *
     * @param color the Color of the rectangle being made
     * @return a rectangle of the correct size and color that can change collors when clicked
     */
    private synchronized Rectangle createTile(PlaceColor color)
    {
        Rectangle rectangle = new Rectangle(TILE_SIZE, TILE_SIZE);

        rectangle.setFill(setTileColor(color));

        //when the rectangle is clicked it will attempt to make a move in that rectangle's location
        rectangle.setOnMouseClicked(new EventHandler<MouseEvent>() {

            // added new eventhandler for mouse event (gib point plz)
            @Override
            public void handle(MouseEvent mouseEvent) {
                int row = grid.getRowIndex((Rectangle) mouseEvent.getSource());
                int col = grid.getColumnIndex((Rectangle) mouseEvent.getSource());
                try {
                    serverConn.sendTile(new PlaceTile(col, row, username, currentColor, System.currentTimeMillis()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return rectangle;
    }

    /**
     * Update the gui for the user.
     */
    private synchronized void updateOutput()
    {
        this.window.show();
    }

    /**
     * smoothly ends the game
     */
    private synchronized void endGame()
    {
        this.notify();
    }

    /**
     * closes the NetworClient
     */
    public synchronized void stop() throws IOException {
        this.endGame();
        this.serverConn.close();
    }

    /**
     * updates the display with the changed tile
     * @param tile the tile being changed
     */
    private synchronized  void refresh(PlaceTile tile)
    {
        Rectangle rectangle = createTile(tile.getColor());

        grid.add(rectangle, tile.getRow(), tile.getCol());

        PlaceTile tempTile = tile;

        Tooltip tileInfo = new Tooltip("("+tempTile.getRow()+","+tempTile.getCol()+")\n"+tempTile.getOwner()+
                "\n"+fromatTime(tempTile.getTime()));
        Tooltip.install(rectangle, tileInfo);

        this.updateOutput();
    }

    /**
     * a helper function to establish the correct color for a tile
     * @param inColor the PlaceColor value of a color
     * @return the Color-class color needed
     */
    private synchronized Color setTileColor(PlaceColor inColor)
    {
        return(Color.rgb(inColor.getRed(),inColor.getGreen(),inColor.getBlue()));
    }


    /**
     * The Function that established the connection to te server
     *
     * @throws PlaceException when the player cannot connect, or looses connection with the server
     * @throws IOException if the serverCon thread fails to close properly
     */
    @Override
    public synchronized  void init() throws IOException {

        //Create a list of strings that holds the args passed in.
        List<String> parameters = getParameters().getRaw();

        //Grabbing the hostname the port number and the username
        String host = parameters.get(0);
        int port = Integer.parseInt(parameters.get(1));
        this.username = parameters.get(2);

        //Create a new observable board
        this.model = new PlaceBoardObservable();

        try {

            //Creates a new NetWorkClient that gets passed a host name
            // a port number, the username of the player, the classname and the model.
            this.serverConn = new NetworkClient(
                    host,
                    port,
                    this.username,
                    getClass().getSimpleName(),
                    this.model);
        }
        //catches a PlaceException if there's an error and then closes the server.
        catch(PlaceException e) {
            System.exit(0);
            this.serverConn.close();
        }

        //If you get here then the server starts
        this.serverConn.start();

        //Add model to observer and update the board when possible.
        this.model.addObserver( this);

        //The game is now running therefore the boolean is true.
        //Otherwise default false.
        this.running = true;

    }


    /**
     * The main function of the Place GUI class, ensures the correct number of arguments
     * are passed in and then launches the application
     *
     * @param args an array of arguments including the Server address, the port number
     *             and  the username
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PlaceGUI host port username");
            System.exit(-1);
        } else {
            Application.launch(args);
        }

    }

    /**
     *Updates the view for the user
     * @param o the observable object
     * @param arg the view
     */
    @Override
    public synchronized void update(Observable o, Object arg) {
        Platform.runLater(()->
                {
                    if(arg instanceof PlaceTile)
                        this.refresh((PlaceTile) arg);
                }
        );
    }
}
