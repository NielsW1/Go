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

public class GoServer implements Runnable {

  private final ServerSocket serverSocket;
  private final List<GoClientHandler> handlers;
  private final Queue<GoClientHandler> playerQueue;
  private final GoServerTUI serverTUI;

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

  public synchronized void startNewGame(GoClientHandler player1, GoClientHandler player2) {
    GoGame newGame = new GoGame(9, player1, player2);
    player1.setGame(newGame);
    player2.setGame(newGame);
    broadCastToPlayers(newGame, protocolMessage(GoProtocol.GAME_STARTED,
        player1.getUsername() + "," + player2.getUsername()) + GoProtocol.SEPARATOR
        + newGame.getBoardSize());
    wait(500);
    player1.handleOutput(GoProtocol.MAKE_MOVE);
    player1.startTimeOut();
  }

  public synchronized void endGame(GoGame game, GoClientHandler clientHandler, boolean resign) {
    List<GoClientHandler> gamePlayers = game.getHandlers();
    game.scoreGame();
    String gameOverMessage;
    GoPlayer winner = null;

    if (resign) {
      for (GoClientHandler player : gamePlayers) {
        if (!clientHandler.equals(player)) {
          winner = player.getPlayer();
          break;
        }
      }
    } else {
      winner = game.getWinner();
    }
    if (winner == null) {
      gameOverMessage =
          protocolMessage(GoProtocol.GAME_OVER, "Draw!");
    } else {
      gameOverMessage = protocolMessage(GoProtocol.GAME_OVER,
          "Winner" + GoProtocol.SEPARATOR + winner.getUsername());
    }
    wait(500);
    broadCastToPlayers(game, gameOverMessage);

    for (GoClientHandler handler : gamePlayers) {
      handler.getTimer().cancel();
      handler.getPlayer().resetPlayer();
      handler.setGame(null);
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
      printToServer("Connected to server");

    } catch (IOException e) {
      System.out.println("Something went wrong initializing the connection.");
    }
  }

  /**
   * Broadcasts message to clients in specific game.
   */
  public synchronized void broadCastToPlayers(GoGame game, String outputLine) {
    printToServer(outputLine);
    for (GoClientHandler handler : game.getHandlers()) {
      handler.handleOutput(outputLine);
    }
  }

  public void handleDisconnect(GoClientHandler clientHandler) {
    synchronized (handlers) {
      printToServer(protocolMessage(GoProtocol.DISCONNECTED, clientHandler.getUsername()));
      handlers.remove(clientHandler);
    }
  }

  public String protocolMessage(String type, String message) {
    return type + GoProtocol.SEPARATOR + message;
  }

  public void printToServer(String message) {
    serverTUI.receiveMessage(GoProtocol.LOG + message);
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
