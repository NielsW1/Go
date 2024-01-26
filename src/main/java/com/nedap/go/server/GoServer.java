package com.nedap.go.server;

import com.nedap.go.gamelogic.GoGame;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GoServer implements Runnable {

  private final ServerSocket serverSocket;
  private List<GoClientHandler> handlers;
  private final Queue<GoClientHandler> playerQueue;
  private GoServerTUI serverTUI;

  public GoServer(int port, GoServerTUI serverTUI) throws IOException {
    serverSocket = new ServerSocket(port);
    handlers = new ArrayList<>();
    playerQueue = new LinkedList<>();
    this.serverTUI = serverTUI;
  }

  public int getServerPort() {
    return serverSocket.getLocalPort();
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
        if (handler.getUsername().equals(username)) {
          return true;
        }
      }
    }
    return false;
  }

  public synchronized void startNewGame(GoClientHandler player1, GoClientHandler player2) {
    GoGame newGame = new GoGame(9, player1, player2);
    player1.setGame(newGame);
    player2.setGame(newGame);
    broadCastMessage(protocolMessage(GoProtocol.GAME_STARTED,
        "Player 1: " + player1.getUsername() + "; Player 2: " + player2.getUsername()));
    wait(500);
    player1.broadCastToPlayers(protocolMessage(
        GoProtocol.MAKE_MOVE, newGame.getTurn().getUsername()));
  }

  public synchronized void endGame(GoGame game) {
    for (GoClientHandler handler : game.getHandlers()) {
      handler.getPlayer().resetPlayer();
      handler.setGame(null);
    }
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
      serverTUI.receiveMessage(clientHandler + " connected to server");
    } catch (IOException e) {
      System.out.println("Something went wrong initializing the connection.");
    }
  }

  @Override
  public void run() {
    while (!serverSocket.isClosed()) {
      try {
        acceptConnections();
      } catch (IOException e) {
        closeServer();
      }
    }
  }

  public synchronized void broadCastMessage(String inputLine) {
    serverTUI.receiveMessage(inputLine);
    for (GoClientHandler handler : handlers) {
      handler.handleOutput(inputLine);
    }
  }

  public synchronized void handleDisconnect(GoClientHandler clientHandler) {
    handlers.remove(clientHandler);

    try {
      endGame(clientHandler.getGame());
    } catch (NullPointerException ignored) {
    }
  }

  public String protocolMessage(String type, String message) {
    return type + GoProtocol.SEPARATOR + message;
  }

  public void closeServer() {
    try {
      if (!serverSocket.isClosed()) {
        serverSocket.close();
      }
    } catch (IOException ignored) {
    }
  }

  public void wait(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ignored) {
    }
  }
}
