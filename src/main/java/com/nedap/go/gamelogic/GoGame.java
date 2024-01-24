package com.nedap.go.gamelogic;

import com.nedap.go.Go;
import com.nedap.go.client.GoClient;
import com.nedap.go.server.GoClientHandler;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GoGame {

  private int boardSize;
  private int passCounter;
  private Goban goban;
  public GoPlayer currentTurn;
  private final GoPlayer player1;
  private final GoPlayer player2;
  private List<GoClientHandler> handlers;
  public boolean fixTurn = false;

  public GoGame(int boardSize, GoClientHandler handler1, GoClientHandler handler2) {
    this.boardSize = boardSize;
    handlers = new ArrayList<>();
    passCounter = 0;
    goban = new Goban(boardSize);

    handlers.add(handler1);
    handlers.add(handler2);
    player1 = handler1.getPlayer();
    player2 = handler2.getPlayer();
    currentTurn = player1;
  }

  public GoGame(int boardSize) {
    this.boardSize = boardSize;
    goban = new Goban(boardSize);
    player1 = new GoPlayer("Henk");
    player2 = new GoPlayer("Piet");
  }

  public GoPlayer getTurn() {
    return currentTurn;
  }

  public GoPlayer getOpponent() {
    if (currentTurn.equals(player1)) {
      return player2;
    } else {
      return player1;
    }
  }

  public List<GoClientHandler> getHandlers() {
    return handlers;
  }

  public void setTurn() {
    if (!fixTurn) {
      if (currentTurn.equals(player1)) {
        currentTurn = player2;
      } else {
        currentTurn = player1;
      }
    }
  }

  public void pass() {
    passCounter += 1;
    if (isGameOver()) {
      endGame();
    } else {
      setTurn();
    }
  }

  public void makeMove(int row, int col, GoPlayer player) throws IllegalMoveException, NotYourTurnException {
    makeMove(row * boardSize + col, player);
  }

  public void makeMove(int linearPosition, GoPlayer player) throws IllegalMoveException, NotYourTurnException {
    if (player.equals(getTurn()) && !isGameOver()) {
      goban.makeMove(linearPosition, player);
      passCounter = 0;
      setTurn();
    } else {
      throw new NotYourTurnException();
    }
  }

  public boolean isGameOver() {
    return passCounter > 1;
  }

  public void endGame() {
    goban.scoreGoban();
    for (int position = 0; position < boardSize * boardSize; position++) {
      if (goban.getStone(position) == player1.getStone()) {
        player1.incrementScore();
      } else if (goban.getStone(position) == player2.getStone()) {
        player2.incrementScore();
      }
    }
  }

  public String toString() {
    StringBuilder boardString = new StringBuilder();
    for (int position = 0; position < boardSize * boardSize; position++) {
      boardString.append(goban.getStone(position)).append("  ");
    }
    boardString.append("\n");
    return boardString.toString();
  }
}
