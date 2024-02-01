package com.nedap.go.client;

import com.nedap.go.gamelogic.Goban;
import com.nedap.go.gamelogic.IllegalMoveException;
import com.nedap.go.gamelogic.NotYourTurnException;
import com.nedap.go.gamelogic.Stone;
import com.nedap.go.server.GoProtocol;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class GoClient {

  private final Socket clientSocket;
  private final BufferedReader in;
  private final BufferedWriter out;
  private final GoClientTUI clientTUI;
  private String username;
  private String tempUsername;
  private int boardSize;
  private boolean gameStarted = false;
  private boolean currentTurn;
  private boolean queued;
  private Goban goban;
  private boolean gui = false;
  private GoClientListener listener;

  public GoClient(InetAddress address, int port, GoClientTUI client) throws IOException {
    clientSocket = new Socket(address, port);
    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    clientTUI = client;
    receiveInput();
  }

  public Stone getStone(String color) {
    if (color.equalsIgnoreCase("BLACK")) {
      return Stone.BLACK;
    } else {
      return Stone.WHITE;
    }
  }

  public boolean getTurn() {
    return currentTurn;
  }

  public boolean getGameStarted() {
    return gameStarted;
  }

  public Goban getGoban() {
    return goban;
  }

  public void setGUI() {
    if (!gameStarted) {
      gui = true;
    }
  }

  public void receiveInput() {
    new Thread(() -> {
      try {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
          handleInput(inputLine);
        }
      } catch (IOException e) {
        sendToTUI(e.getMessage());
      } finally {
        closeConnection();
      }
    }).start();
  }

  public void handleInput(String inputLine) {
    String[] parsedInput = inputLine.split(GoProtocol.SEPARATOR);
    try {
      switch (parsedInput[0].toUpperCase()) {

        case GoProtocol.ACCEPTED:
          if (parsedInput[1].equals(tempUsername)) {
            username = tempUsername;
          }
          break;

        case GoProtocol.QUEUED:
          queued = !queued;
          if (queued) {
            inputLine = GoProtocol.QUEUED + GoProtocol.SEPARATOR + "You are now in the queue.";
          } else {
            inputLine = GoProtocol.QUEUED + GoProtocol.SEPARATOR + "You left the queue.";
          }
          break;

        case GoProtocol.GAME_STARTED:
          boardSize = Integer.parseInt(parsedInput[2]);
          handleGameStart();
          break;

        case GoProtocol.MOVE:
          currentTurn = false;
          int position = Integer.parseInt(parsedInput[1]);
          Stone move = getStone(parsedInput[2]);
          handleMove(position, move);
          break;

        case GoProtocol.MAKE_MOVE:
          handleTurn();
          break;

        case GoProtocol.GAME_OVER:
          currentTurn = false;
          gameStarted = false;
          goban.scoreGoban();
          inputLine += "\n" + goban.getScores();
          clearGUI();
          goban = null;
          break;
      }
    } catch (IndexOutOfBoundsException | NumberFormatException e) {
      sendMessage(GoProtocol.ERROR + GoProtocol.SEPARATOR + e.getMessage());
    }
    sendToTUI(inputLine);
  }

  public void handleGameStart() {
    goban = new Goban(boardSize);
    gameStarted = true;
    queued = false;
    if (gui && listener == null) {
      startGUI();
    }
  }

  public void handleMove(int position, Stone move) {
    try {
      goban.makeMove(position, move);
    } catch (IllegalMoveException e) {
      sendToTUI(e.getMessage());
    }
    if (!gui) {
      sendToTUI(goban.toString());
    }
    updateGUI();
  }

  public void handleTurn() {
    currentTurn = true;
  }

  public void handleOutput(String outputLine)
      throws IllegalMoveException, NotYourTurnException,
      NumberFormatException, IndexOutOfBoundsException {
    String[] parsedOutput = outputLine.split(GoProtocol.SEPARATOR);
    switch (parsedOutput[0].toUpperCase()) {
      case GoProtocol.LOGIN:
        if (username == null && !parsedOutput[1].trim().isEmpty()) {
          sendMessage(outputLine);
          tempUsername = parsedOutput[1];
        }
        break;

      case GoProtocol.QUEUE:
        if (!getGameStarted()) {
          sendMessage(outputLine);
        }
        break;

      case GoProtocol.MOVE:
      case GoProtocol.PASS:
      case GoProtocol.RESIGN:
        if (getGameStarted()) {
          if (!getTurn()) {
            throw new NotYourTurnException("It's not your turn!");
          }
          if (parsedOutput[0].equalsIgnoreCase(GoProtocol.MOVE)) {
            int position;
            String[] parsedPosition = parsedOutput[1].split(",");
            if (parsedPosition.length > 1) {
              position = Integer.parseInt(parsedPosition[1]) * boardSize + Integer.parseInt(
                  parsedPosition[0]);
            } else {
              position = Integer.parseInt(parsedPosition[0]);
            }
            if (!goban.isValidMove(position)) {
              throw new IllegalMoveException("Invalid move! Try again.");
            }
          }
          sendMessage(outputLine);
        }
        break;
    }
  }

  public void startGUI() {
    listener = new GoClientListener(boardSize, this);
  }

  public void updateGUI() {
    if (listener != null) {
      listener.updateGUI();
    }
  }

  public void clearGUI() {
    if (listener != null) {
      listener.clearGUI();
    }
  }

  public void sendMessage(String outputLine) {
    try {
      out.write(outputLine);
      out.newLine();
      out.flush();
    } catch (IOException e) {
      sendToTUI(e.getMessage());
      closeConnection();
    }
  }

  public void sendToTUI(String message) {
    clientTUI.receiveInput(message);
  }

  public void closeConnection() {
    try {
      clientSocket.close();
    } catch (IOException e) {
      sendToTUI(e.getMessage());
    }
  }
}
