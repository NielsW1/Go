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
  private final List<GoClientHandler> handlers = new ArrayList<>();
  private final Queue<GoClientHandler> playerQueue = new LinkedList<>();
  private GoServerTUI serverTUI;

  public GoServer(int port, GoServerTUI serverTUI) throws IOException {
    this(port);
    this.serverTUI = serverTUI;
  }

  public GoServer(int port) throws IOException {
    serverSocket = new ServerSocket(port);
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
      printToServer(
          protocolMessage(GoProtocol.QUEUED, clientHandler.getUsername()) + " joined the queue");
      if (playerQueue.size() > 1) {
        startNewGame(playerQueue.poll(), playerQueue.poll());
      }
    }
  }

  public void removeFromQueue(GoClientHandler clientHandler) {
    synchronized (playerQueue) {
      printToServer(
          protocolMessage(GoProtocol.QUEUED, clientHandler.getUsername()) + " left the queue");
      playerQueue.remove(clientHandler);
    }
  }

  /**
   * Checks if someone with username is already logged in to the server.
   * @return true if username already logged in
   */
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

  /**
   * Starts a new game with the first two players in the waiting queue.
   */
  public synchronized void startNewGame(GoClientHandler player1, GoClientHandler player2) {
    GoGame newGame = new GoGame(9, player1, player2);
    player1.setGame(newGame);
    player2.setGame(newGame);
    wait(500);
    broadCastToPlayers(newGame, protocolMessage(GoProtocol.GAME_STARTED,
        player1.getUsername() + "," + player2.getUsername()) + GoProtocol.SEPARATOR
        + newGame.getBoardSize());
    player1.sendMessage(GoProtocol.MAKE_MOVE);
    player1.startTimeout();
  }

  /**
   * Ends the game. If a player resigns or disconnects, the other player automatically wins.
   * The winner is then broadcasted to both players and the players are reset.
   */
  public synchronized void endGame(GoGame game, GoClientHandler clientHandler, boolean resign) {
    List<GoClientHandler> gamePlayers = game.getHandlers();
    game.scoreGame();
    String gameOverMessage;
    GoPlayer winner = null;

    if (resign) {
      for (GoClientHandler handler : gamePlayers) {
        if (!clientHandler.equals(handler)) {
          winner = handler.getPlayer();
        }
      }
    } else {
      winner = game.getWinner();
    }
    if (winner == null) {
      gameOverMessage =
          protocolMessage(GoProtocol.GAME_OVER, "Draw");
    } else {
      gameOverMessage = protocolMessage(GoProtocol.GAME_OVER,
          "Winner" + GoProtocol.SEPARATOR + winner.getUsername());
    }
    wait(500);
    broadCastToPlayers(game, gameOverMessage);

    for (GoClientHandler handler : gamePlayers) {
      handler.cancelTimeout();
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
        printToServer(e.getMessage());
        closeServer();
      }
    }
  }

  public void acceptConnections() throws IOException {
    while (!serverSocket.isClosed()) {
      try {
        Socket socket = serverSocket.accept();
        handleConnection(socket);
      } catch (SocketException e) {
        printToServer(e.getMessage());
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
      printToServer(e.getMessage());
    }
  }

  /**
   * Broadcasts message to clients in game.
   */
  public synchronized void broadCastToPlayers(GoGame game, String outputLine) {
    printToServer(outputLine);
    if (game != null) {
      for (GoClientHandler handler : game.getHandlers()) {
        handler.sendMessage(outputLine);
      }
    }
  }

  /**
   * Handles a disconnect on the server side. If the player was in a game, the game ends
   * and the other player is declared winner.

   */

  public void handleDisconnect(GoClientHandler clientHandler, GoGame game) {
    synchronized (handlers) {
      if (game != null) {
        endGame(game, clientHandler, true);
      }
      printToServer(protocolMessage(GoProtocol.DISCONNECTED, clientHandler.getUsername()));
      handlers.remove(clientHandler);
    }
  }

  /**
   * Translates a message to adhere to the protocol.
   */
  public String protocolMessage(String type, String message) {
    return type + GoProtocol.SEPARATOR + message;
  }

  /**
   * Prints a message to the serverTUI.
   */
  public void printToServer(String message) {
    if (serverTUI != null) {
      serverTUI.receiveMessage(GoProtocol.LOG + message);
    }
  }

  public void wait(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ignored) {
    }
  }

  public void closeServer() {
    try {
      if (!serverSocket.isClosed()) {
        serverSocket.close();
      }
    } catch (IOException ignored) {
    }
  }
}
