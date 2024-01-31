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
import java.util.Timer;
import java.util.TimerTask;

public class GoClientHandler implements Runnable {

  private final GoServer server;
  private final Socket socket;
  private final BufferedReader in;
  private final BufferedWriter out;
  private String username;
  private GoPlayer player;
  private GoGame game;
  private Timer timer;

  public GoClientHandler(Socket socket, GoServer server) throws IOException {
    this.socket = socket;
    this.server = server;
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
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
      cancelTimeout();
      server.broadCastToPlayers(game, protocolMessage(GoProtocol.MOVE,
          pos + GoProtocol.SEPARATOR + player.getColor()));
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
      cancelTimeout();
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
    game = null;
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

  public void opponentTurn() {
    for (GoClientHandler handler : game.getHandlers()) {
      if (!handler.equals(this)) {
        handler.sendMessage(GoProtocol.MAKE_MOVE);
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
      closeConnection();
    }
  }

  public String protocolMessage(String type, String message) {
    return type + GoProtocol.SEPARATOR + message;
  }

  public void startTimeout() {
    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        server.broadCastToPlayers(game,
            protocolMessage(GoProtocol.ERROR, username + ", move timed out!"));
        handleResign();
      }
    }, 60000);
  }

  public void cancelTimeout() {
    if (timer != null) {
      timer.cancel();
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
      cancelTimeout();
      server.handleDisconnect(this, game);
      socket.close();
    } catch (IOException | NullPointerException ignored) {
    }
  }

}
