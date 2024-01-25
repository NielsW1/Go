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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoClientHandler implements Runnable{

  private GoServer server;
  private Socket socket;
  private BufferedReader in;
  private BufferedWriter out;
  private String username;
  private GoPlayer player;
  private GoGame game;

  public GoClientHandler(Socket socket, GoServer server) throws IOException {
    this.socket = socket;
    this.server = server;
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new BufferedWriter((new OutputStreamWriter(socket.getOutputStream())));
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
      server.handleDisconnect(this);
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
    player.setGame(game);
  }

  public void handleHandshake() {
    waitOneSecond();
    handleOutput(GoProtocol.HELLO + GoProtocol.SEPARATOR +
        "Welcome to the GO server! use LOGIN~<username> to log in.");
  }

  public void handleInput(String inputLine) {
    String[] parsedInput = inputLine.split(GoProtocol.SEPARATOR);

    switch (parsedInput[0]) {
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
    }
  }

  public void handleLogin(String username) {
    if (this.username == null) {
      if (!server.userAlreadyLoggedIn(this, username)) {
        this.username = username;
        player = new GoPlayer(username);
        server.broadCastMessage(
            GoProtocol.ACCEPTED + GoProtocol.SEPARATOR + username + " has joined the server.");
      } else {
        handleOutput(GoProtocol.REJECTED + GoProtocol.SEPARATOR + "User by that name already logged in!");
      }
    } else {
      handleOutput(GoProtocol.ERROR + GoProtocol.SEPARATOR +
          "You are already logged in as " + username + "!");
    }
  }

  public void handleQueue() {
    if (username != null) {
      if (server.getQueue().contains(this)) {
        server.removeFromQueue(this);
        server.broadCastMessage(GoProtocol.QUEUED + GoProtocol.SEPARATOR + username + " has left the queue.");
      } else {
        server.broadCastMessage(GoProtocol.QUEUED + GoProtocol.SEPARATOR + username + " has joined the queue.");
        server.addToQueue(this);
      }
    }
  }

  public void handleMove(String position) {
    Pattern pat = Pattern.compile("^\\((\\d+),(\\d+)\\)$");
    Matcher matcher = pat.matcher(position);
    try {
      if (matcher.find()) {
        game.makeMove(Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2)), player);
      } else {
        game.makeMove(Integer.parseInt(position), player);
      }
      broadCastToPlayers(
          GoProtocol.MOVE + GoProtocol.SEPARATOR + position + GoProtocol.SEPARATOR + username
              + "\n" + game.toString());
      broadCastToPlayers(
          GoProtocol.MAKE_MOVE + GoProtocol.SEPARATOR + game.getTurn().getUsername());
    } catch (NumberFormatException | IllegalMoveException e) {
      handleOutput(GoProtocol.ERROR + GoProtocol.SEPARATOR + "Invalid move, try again!");
    } catch (NotYourTurnException e) {
      handleOutput(GoProtocol.ERROR + GoProtocol.SEPARATOR + "It is not your turn!");
    }
  }

  public void handlePass() {
    try {
      game.pass(player);
      if (game.isGameOver()) {
        GoPlayer winner = game.endGame();
        server.endGame(game);
        broadCastToPlayers(
            GoProtocol.GAME_OVER + GoProtocol.SEPARATOR + winner.getUsername() + game.getScores());
      } else {
        broadCastToPlayers(GoProtocol.PASS + GoProtocol.SEPARATOR + username);
      }
    } catch (NotYourTurnException e) {
      handleOutput(GoProtocol.ERROR + GoProtocol.SEPARATOR + "It is not your turn!");
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

  public void closeConnection() {
    try {
      socket.close();
    } catch (IOException ignored) {
    }
  }

  public void waitOneSecond() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ignored) {
    }
  }
}
