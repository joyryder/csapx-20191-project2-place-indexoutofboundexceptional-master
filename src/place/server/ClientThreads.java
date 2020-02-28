package place.server;

import place.PlaceBoard;
import place.PlaceTile;
import place.network.PlaceRequest;

import java.io.*;
import java.net.Socket;

public class ClientThreads extends Thread implements Runnable, Closeable {
    private Socket clientSocket;
    private String username;
    private PlaceServer server;
    private ObjectOutputStream output;
    private boolean go = true;
    private ObjectInputStream input;
    private PlaceBoard board;
    private boolean goWhile = true;

    /**
     * creates a new client thread
     *
     * @param socket
     * @param username
     * @param server
     */
    public ClientThreads(Socket socket, String username, PlaceServer server) {
        this.clientSocket = socket;
        this.username = username;
        System.out.println("User " + username + " connected on socket " + clientSocket);
        this.board = server.getBoard();
        this.server = server;
        try {
            this.output = new ObjectOutputStream(clientSocket.getOutputStream());
            this.input = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
        }
    }


    /**
     * return the user's socket
     *
     * @return the user's socket
     */
    public Socket getClientSocket() {
        return clientSocket;
    }

    /**
     * returns the user's Username
     *
     * @return the user's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Close all of the threads/server connections
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        this.server.signOut(this.username);
        this.clientSocket.close();
        output.close();
        input.close();
    }

    /**
     * runs client thread
     */
    public void run() {
        try {
            PlaceRequest<?> request;
            while (go) {
                try {
                    while(goWhile) {
                        request = (PlaceRequest<?>) input.readObject();

                        checkRequest(request);
                    }

                } finally {
                    close();
                }
            }
        } catch (IOException | InterruptedException | ClassNotFoundException ignored) {
        }
    }

    protected ObjectOutputStream getOutput() {
        return output;
    }


    /**
     * a method w/a switch case to check client requests and execute them
     *
     * @param request
     */
    private void checkRequest(PlaceRequest<?> request) throws IOException, InterruptedException {

        System.out.println(request);
        PlaceRequest.RequestType type = request.getType();
        PlaceRequest<?> servercheck;
        System.out.println("top of check request");
        switch (type) {
            case LOGIN:
                username = (String) request.getData();
                if (server.login(username, this)) {
                    board = server.getBoard();
                    servercheck = new PlaceRequest<>(PlaceRequest.RequestType.BOARD, board);
                    output.writeObject(servercheck);
                    output.flush();
                    }
                else{
                    close();
                }

                break;
            case BOARD:
            case LOGIN_SUCCESS:
            case TILE_CHANGED:
                break;
            case CHANGE_TILE: {
                try {
                    server.tileUpdate((PlaceTile) request.getData());
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    break;
                }
                board = server.getBoard();
                Thread.sleep(500);
                break;
            }
            case ERROR: {
                this.goWhile=false;
                close();
                break;
            }
        }
    }
}