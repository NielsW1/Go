package com.nedap.go.server;

import com.nedap.go.gamelogic.GoGame;
import com.nedap.go.gamelogic.GoPlayer;
import com.nedap.go.gamelogic.IllegalMoveException;
import com.nedap.go.gamelogic.NotYourTurnException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GoClientHandler implements Runnable {

  private final GoServer server;
  private final Socket socket;
  private final BufferedReader in;
  private final BufferedWriter out;
  private String username;
  private GoPlayer player;
  private GoGame game;
  private ScheduledExecutorService timerExecutor;

  public GoClientHandler(Socket socket, GoServer server) throws IOException {
    this.socket = socket;
    this.server = server;
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
  }

  @Override
  public void run() {
    handleHandshake();
    try {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        handleInput(inputLine);
      }
    } catch (IOException e) {
      server.printToServer(e.getMessage());
    } finally {
      closeConnection();
    }
  }

  public String getUsername() {
    return username;
  }

  public GoPlayer getPlayer() {
    return player;
  }

  public void setGame(GoGame game) {
    this.game = game;
  }

  public void handleHandshake() {
    wait(500);
    sendMessage(protocolMessage(GoProtocol.HELLO,
        "Welcome to Niels' GO server! use LOGIN~<username> to log in."));
  }

  public void handleInput(String inputLine) {
    String[] parsedInput = inputLine.split(GoProtocol.SEPARATOR);
    try {
      switch (parsedInput[0].toUpperCase()) {
        case GoProtocol.LOGIN:
          handleLogin(parsedInput[1]);
          break;

        case GoProtocol.QUEUE:
          handleQueue();
          break;

        case GoProtocol.MOVE:
          handleMove(parsedInput[1]);
          break;

        case GoProtocol.PASS:
          handlePass();
          break;

        case GoProtocol.RESIGN:
          handleResign();
          break;

        case GoProtocol.ERROR:
          server.printToServer(inputLine);
          break;
      }
    } catch (IndexOutOfBoundsException e) {
      server.printToServer(e.getMessage());
    } catch (NullPointerException e) {
      sendMessage(protocolMessage(GoProtocol.ERROR, "You are not in a game!"));
    }
  }

  /**
   * Logs the user in if the username is valid and no one with the same name is logged in
   * at the moment.
   */
  public void handleLogin(String name) {
    String outMessage;
    if (username == null && !name.trim().isEmpty()) {
      if (!server.userAlreadyLoggedIn(this, name)) {
        username = name;
        player = new GoPlayer(username);
        outMessage = protocolMessage(GoProtocol.ACCEPTED, username);
      } else {
        outMessage = protocolMessage(GoProtocol.REJECTED, "User by that name already logged in!");
      }
    } else {
      outMessage = protocolMessage(GoProtocol.REJECTED, "Invalid username!");
    }
    sendMessage(outMessage);
    server.printToServer(outMessage);
  }

  /**
   * Adds the player to the waiting queue. If the player is already in the queue,
   * removes them instead.
   */
  public void handleQueue() {
    if (username != null && game == null) {
      if (server.getQueue().contains(this)) {
        server.removeFromQueue(this);
        sendMessage(GoProtocol.QUEUED);
      } else {
        sendMessage(GoProtocol.QUEUED);
        server.addToQueue(this);
      }
    }
  }

  public void handleMove(String position) {
    int pos;
    String[] parsedPosition = position.split(",");
    try {
      if (parsedPosition.length > 1) {
        pos = Integer.parseInt(parsedPosition[1]) * game.getBoardSize() +
            Integer.parseInt(parsedPosition[0]);
      } else {
        pos = Integer.parseInt(position);
      }
      game.makeMove(pos, player);
      server.broadCastToPlayers(game, protocolMessage(GoProtocol.MOVE,
          pos + GoProtocol.SEPARATOR + player.getColor()));
      wait(100);
      opponentTurn();

    } catch (NumberFormatException | IllegalMoveException | NotYourTurnException e) {
      String errorMessage = protocolMessage(GoProtocol.ERROR, e.getMessage());
      sendMessage(errorMessage);
      server.printToServer(errorMessage);
    }
  }

  public void handlePass() {
    try {
      game.pass(player);
      server.broadCastToPlayers(game, protocolMessage(GoProtocol.PASS, player.getColor()));
      if (game.isGameOver()) {
        server.endGame(game, this, false);
      } else {
        opponentTurn();
      }
    } catch (NotYourTurnException e) {
      sendMessage(protocolMessage(GoProtocol.ERROR, "It is not your turn!"));
    }
  }

  public void handleResign() {
    server.endGame(game, this, true);
    player.resetPlayer();
  }

  /**
   * Cancels this player's timeout timer, then sends the MAKE MOVE message to the
   * other player and starts the other player's timeout timer.
   */
  public void opponentTurn() {
    cancelTimeout();
    for (GoClientHandler handler : game.getHandlers()) {
      if (!handler.equals(this)) {
        handler.sendMessage(GoProtocol.MAKE_MOVE);
        server.printToServer(GoProtocol.MAKE_MOVE + GoProtocol.SEPARATOR + handler.getUsername());
        handler.startTimeout();
      }
    }
  }

  public void sendMessage(String outputLine) {
    try {
      out.write(outputLine);
      out.newLine();
      out.flush();
    } catch (IOException e) {
      server.printToServer(e.getMessage());
      closeConnection();
    }
  }

  public String protocolMessage(String type, String message) {
    return type + GoProtocol.SEPARATOR + message;
  }

  /**
   * Starts a 60-second timer for this player. If the timer expires, the player
   * automatically resigns.
   */
  public void startTimeout() {
    timerExecutor = Executors.newScheduledThreadPool(1);
    timerExecutor.schedule(() -> {
      server.broadCastToPlayers(game,
          protocolMessage(GoProtocol.ERROR, username + ", move timed out!"));
      handleResign();
    }, 60, TimeUnit.SECONDS);
  }

  /**
   * Cancels the timer for this player. This method is called whenever this player
   * makes a move.
   */
  public void cancelTimeout() {
    if (timerExecutor != null) {
      timerExecutor.shutdownNow();
    }
  }

  public void wait(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ignored) {
    }
  }

  public void closeConnection() {
    try {
      server.handleDisconnect(this, game);
      socket.close();
    } catch (IOException e) {
      server.printToServer(e.getMessage());
    }
  }

}
