package com.nedap.go.server;

import com.nedap.go.gamelogic.GoGame;
import com.nedap.go.gamelogic.GoPlayer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class GoServer {

  private boolean runServer = true;
  private final ServerSocket serverSocket;
  public List<GoClientHandler> handlers;
  private final Queue<GoClientHandler> playerQueue;
  private List<GoGame> gamesList;

  public GoServer(int port) throws IOException {
    serverSocket = new ServerSocket(port);
    handlers = new ArrayList<>();
    playerQueue = new LinkedList<>();
    gamesList = new ArrayList<>();
  }

  public int getServerPort() {
    return serverSocket.getLocalPort();
  }

  public synchronized List<GoClientHandler> getHandlers() {
    return handlers;
  }

  public Queue<GoClientHandler> getQueue() {
    synchronized (playerQueue) {
      return playerQueue;
    }
  }

  public void addToQueue(GoClientHandler clientHandler) {
    synchronized (playerQueue) {
      playerQueue.add(clientHandler);
      if (playerQueue.size() > 1) {
        startNewGame(playerQueue.poll(), playerQueue.poll());
      }
    }
  }

  public void removeFromQueue(GoClientHandler clientHandler) {
    synchronized (playerQueue) {
      playerQueue.remove(clientHandler);
    }
  }

  public synchronized boolean userAlreadyLoggedIn(GoClientHandler clientHandler, String username) {
    for (GoClientHandler handler : handlers) {
      if (!handler.equals(clientHandler)) {
        try {
          if (handler.getUsername().equals(username)) {
            return true;
          }
        } catch (NullPointerException ignored) {
        }
      }
    }
    return false;
  }

  public void startNewGame(GoClientHandler player1, GoClientHandler player2) {
    waitOneSecond();
    GoGame newGame = new GoGame(9, player1, player2);
    player1.setGame(newGame);
    player2.setGame(newGame);
    gamesList.add(newGame);
    broadCastMessage(GoProtocol.GAME_STARTED + GoProtocol.SEPARATOR +
        "Player 1: " + player1.getUsername() + " Player 2: " + player2.getUsername());
  }

  public void acceptConnections() throws IOException {
    while (!serverSocket.isClosed()) {
      try {
        Socket socket = serverSocket.accept();
        handleConnection(socket);
      } catch (SocketException ignored) {
      }
    }
  }

  public void handleConnection(Socket socket) {
    try {
      GoClientHandler clientHandler = new GoClientHandler(socket, this);
      handlers.add(clientHandler);
      Thread thread = new Thread(clientHandler);
      thread.start();
    } catch (IOException e) {
      System.out.println("Something went wrong initializing the connection.");
    }
  }

  public void startServer() {
    while (runServer) {
      try {
        acceptConnections();
      } catch (IOException e) {
        System.out.println("Something went wrong, closing the server");
        runServer = false;
        closeServer();
      }
    }
  }

  public void broadCastMessage(String inputLine) {
    System.out.println(inputLine);
    for (GoClientHandler handler : handlers) {
      handler.handleOutput(inputLine);
    }
  }

  public void handleDisconnect(GoClientHandler clientHandler) {
    handlers.remove(clientHandler);
    String disconnectMessage = clientHandler.getUsername() != null ?
        clientHandler.getUsername() : "<Unknown user>";
    disconnectMessage += " has disconnected from the server.";
    broadCastMessage(disconnectMessage);
  }

  public synchronized void closeServer() {
    try {
      if (!serverSocket.isClosed()) {
        serverSocket.close();
      }
    } catch (IOException ignored) {
    }
  }

  public void waitOneSecond() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ignored) {
    }
  }

  public static void main(String[] args) {
    boolean validPort = false;
    int port = -1;
    Scanner input = new Scanner(System.in);
    System.out.println("Enter a port to start listening for connections:");
    while (!validPort) {
      if (input.hasNextLine()) {
        try {
          port = Integer.parseInt(input.nextLine());
          if (port < 0 || port > 65535) {
            System.out.println("Valid port numbers range between 0 and 65535.");
            continue;
          }
          validPort = true;
        } catch (NumberFormatException e) {
          System.out.println("Not a valid port.");
        }
      }
    }
    try {
      GoServer server = new GoServer(port);
      System.out.println("Starting server on port " + server.getServerPort());
      server.startServer();
    } catch (IOException e) {
      System.out.println("Unable to start server!");
    }
  }

}
