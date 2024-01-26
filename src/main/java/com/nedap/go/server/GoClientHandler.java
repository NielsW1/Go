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

public class GoClientHandler implements Runnable {

  private final GoServer server;
  private final Socket socket;
  private final BufferedReader in;
  private final BufferedWriter out;
  private String username;
  private GoPlayer player;
  private GoGame game;

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
    } catch (IOException ignored) {
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

  public GoGame getGame() {
    return game;
  }

  public void setGame(GoGame game) {
    this.game = game;
  }

  public void handleHandshake() {
    wait(500);
    handleOutput(protocolMessage(GoProtocol.HELLO,
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

        default:
          break;
      }
    } catch (IndexOutOfBoundsException ignored) {
    } catch (NullPointerException e) {
      handleOutput(protocolMessage(GoProtocol.ERROR, "You are not in a game!"));
    }
  }

  public void handleLogin(String username) {
    if (this.username == null) {

      if (!server.userAlreadyLoggedIn(this, username)) {
        this.username = username;
        player = new GoPlayer(this.username);
        server.broadCastMessage(
            protocolMessage(GoProtocol.ACCEPTED, this.username + " has joined the server."));

      } else {
        handleOutput(protocolMessage(GoProtocol.REJECTED, "User by that name already logged in!"));
      }

    } else {
      handleOutput(protocolMessage(GoProtocol.REJECTED,
          "You are already logged in as " + this.username + "!"));
    }
  }

  public void handleQueue() {
    if (username != null && game == null) {

      if (server.getQueue().contains(this)) {
        server.removeFromQueue(this);
        server.broadCastMessage(
            protocolMessage(GoProtocol.QUEUED, username + " has left the queue."));

      } else {
        server.broadCastMessage(
            protocolMessage(GoProtocol.QUEUED, username + " has joined the queue."));
        server.addToQueue(this);
      }
    }
  }

  public void handleMove(String position) {
    String[] parsedPosition = position.split(",");

    try {
      if (parsedPosition.length > 1) {
        game.makeMove(Integer.parseInt(parsedPosition[1]),
            Integer.parseInt(parsedPosition[0]), player);
      } else {
        game.makeMove(Integer.parseInt(position), player);
      }

      broadCastToPlayers(protocolMessage(
          GoProtocol.MOVE, position + GoProtocol.SEPARATOR + username + "\n" + game.toString()));
      broadCastToPlayers(protocolMessage(GoProtocol.MAKE_MOVE, game.getTurn().getUsername()));

    } catch (NumberFormatException | IllegalMoveException e) {
      handleOutput(protocolMessage(GoProtocol.ERROR, "Invalid move, try again!"));

    } catch (NotYourTurnException e) {
      handleOutput(protocolMessage(GoProtocol.ERROR, "It is not your turn!"));
    }
  }

  public void handlePass() {
    try {
      game.pass(player);
      broadCastToPlayers(protocolMessage(GoProtocol.PASS, username));

      if (game.isGameOver()) {
        GoPlayer winner = game.endGame();
        String scores = game.getScores();

        for (GoClientHandler handler : game.getHandlers()) {
          handler.handleOutput(protocolMessage(GoProtocol.GAME_OVER,
              winner.getUsername() + " is the winner!" + scores));
          handler.setGame(null);
        }
      }

    } catch (NotYourTurnException e) {
      handleOutput(protocolMessage(GoProtocol.ERROR, "It is not your turn!"));
    }
  }

  public void broadCastToPlayers(String message) {
    for (GoClientHandler handler : game.getHandlers()) {
      handler.handleOutput(message);
    }
  }

  public void handleOutput(String outputLine) {
    try {
      out.write(outputLine);
      out.newLine();
      out.flush();
    } catch (IOException e) {
      closeConnection();
    }
  }

  public String protocolMessage(String type, String message) {
    return type + GoProtocol.SEPARATOR + message;
  }

  public void closeConnection() {
    if (game != null) {
      game.setGameOver();
      game.getHandlers().remove(this);
    }
    try {
      server.handleDisconnect(this);
      server.broadCastMessage(protocolMessage(GoProtocol.DISCONNECTED, (username != null ?
          username : "<Unknown user>") + " has disconnected from the server."));
      socket.close();
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
