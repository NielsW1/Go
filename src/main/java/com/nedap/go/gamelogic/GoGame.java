package com.nedap.go.gamelogic;

import com.nedap.go.Go;
import com.nedap.go.client.GoClient;
import com.nedap.go.server.GoClientHandler;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GoGame extends Thread {

  private int boardSize;
  private int passCounter;
  private Goban goban;
  private GoPlayer currentTurn;
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
    player1.setStone(Stone.BLACK);
    player2.setStone(Stone.WHITE);
    currentTurn = player1;
  }

  /**
   * Second constructor for local testing purposes.
   * @param boardSize
   */
  public GoGame(int boardSize, GoPlayer player1, GoPlayer player2) {
    this.boardSize = boardSize;
    goban = new Goban(boardSize);
    this.player1 = player1;
    this.player2 = player2;
    this.player1.setStone(Stone.BLACK);
    this.player2.setStone(Stone.WHITE);
    currentTurn = player1;
  }

  public GoPlayer getTurn() {
    return currentTurn;
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

  public synchronized void pass(GoPlayer player) throws NotYourTurnException{
    if (player.equals(getTurn())) {
      passCounter += 1;
      if (!isGameOver()) {
        setTurn();
      }
    } else {
      throw new NotYourTurnException();
    }
  }

  public synchronized void makeMove(int row, int col, GoPlayer player) throws IllegalMoveException, NotYourTurnException {
    makeMove(row * boardSize + col, player);
  }

  public synchronized void makeMove(int linearPosition, GoPlayer player) throws IllegalMoveException, NotYourTurnException {
    if (player.equals(getTurn()) && !isGameOver()) {
      goban.makeMove(linearPosition, player);
      passCounter = 0;
      setTurn();
    } else {
      throw new NotYourTurnException();
    }
  }

  public void makeBulkMoves(int[] moves, GoPlayer player) {
    goban.makeBulkMoves(moves, player);
  }

  public boolean isGameOver() {
    return passCounter > 1;
  }

  public GoPlayer endGame() {
    goban.scoreGoban();
    for (int position = 0; position < boardSize * boardSize; position++) {
      if (goban.getStone(position) == player1.getStone()) {
        player1.incrementScore();
      } else if (goban.getStone(position) == player2.getStone()) {
        player2.incrementScore();
      }
    }
    return getWinner();
  }

  public GoPlayer getWinner() {
    if (player1.getScore() > player2.getScore()) {
      return player1;
    } else if (player2.getScore() > player1.getScore()) {
      return player2;
    } else {
      return null;
    }
  }

  public String getScores() {
    return "\nScores:\n" + player1.getUsername() + ": " + player1.getScore() + "\n" +
        player2.getUsername() + ": " + player2.getScore();
  }

  public Goban getGoban() {
    return goban;
  }

  public Goban getGobanCopy() {
    return goban.gobanCopy();
  }

  public String toString() {
     return goban.toString();
  }
}
