package place.client.ptui;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * A class to do console-based user interaction in a manner similar to
 * how JavaFX does window-based interaction.
 * This class is to be inherited by any console application.
 *
 * (THIS is pulled from the reversi client and then edited to fit the needs
 * of the program for Place.
 *
 * @author James Heliotis
 * @author Miguel Rosario
 * @author Joey Saltalamacchia
 * @author Shrif Rai
 */
public abstract class Run {

    private String[] cmdLineArgs;

    private Thread eventThread;

    /**
     * Run a console application.
     * @param ptuiClass the class object that refers to the class to
     *             be instantiated
     */
    public static void launch(
            Class< ? extends Run > ptuiClass
    ) {
        launch( ptuiClass, new String[ 0 ] );
    }

    /**
     * Run a console application, with command line arguments.
     * @param ptuiClass the class object that refers to the class to
     *             be instantiated
     * @param args the array of strings from the command line
     */
    public static void launch(
            Class< ? extends Run > ptuiClass,
            String[] args
    ) {
        try {
            Run ptuiApp = ptuiClass.newInstance();
            ptuiApp.cmdLineArgs = Arrays.copyOf( args, args.length );

            try {
                ptuiApp.init();
                ptuiApp.eventThread = new Thread( new Runner( ptuiApp ) );
                ptuiApp.eventThread.start();
                ptuiApp.eventThread.join();
            }
            catch( Exception ie ) {
                System.err.println( "Console event thread interrupted" );
            }
            finally {
                ptuiApp.stop();
            }
        }
        catch( InstantiationException ie ) {
            System.err.println( "Can't instantiate Console App:" );
            System.err.println( ie.getMessage() );
        }
        catch(IllegalAccessException | IOException iae ) {
            System.err.println( iae.getMessage() );
        }
    }

    private static class Runner implements Runnable {
        private final Run ptuiApp;

        public Runner( Run ptuiApp ) { this.ptuiApp = ptuiApp; }

        public void run() {
            try ( Scanner in = new Scanner( System.in ) ) {
                try {
                    ptuiApp.start( in );
                }
                catch( Exception e ) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Fetch the application's command line arguments
     * @return the string array that was passed to launch, if any, or else
     *         an empty array
     */
    public List< String > getArguments() {
        return Arrays.asList( this.cmdLineArgs );
    }

    /**
     * A do-nothing setup method that can be overwritten by subclasses
     * when necessary
     */
    public void init() throws Exception {}

    /**
     * The method that is expected to run the main guts of our program.
     *
     * @param in The scanner used for user input of moves.
     */
    public abstract void start( Scanner in ) throws InterruptedException;

    /**
     * A do-nothing teardown method that can be overwritten by subclasses
     * when necessary.
     */
    public void stop() throws IOException {}

}

