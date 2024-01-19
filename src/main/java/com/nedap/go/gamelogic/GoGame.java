package com.nedap.go.gamelogic;

import com.nedap.go.client.GoPlayer;

public class GoGame {

  private int boardSize;
  private int passCounter;
  private Move[][] board;
  private Move previousMove;
  private GoPlayer currentTurn;
  private final GoPlayer player1;
  private final GoPlayer player2;

  public GoGame(int boardSize, GoPlayer player1, GoPlayer player2) {
    this.boardSize = boardSize;
    passCounter = 0;
    board = new Move[boardSize][boardSize];

    for (int row = 0; row < boardSize; row++) {
      for (int col = 0; col < boardSize; col++) {
        board[row][col] = new Move(row, col, boardSize);
      }
    }

    this.player1 = player1;
    this.player2 = player2;
    player1.setSymbol("●");
    player2.setSymbol("○");
    currentTurn = player1;

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

  public void setTurn() {
    if (currentTurn.equals(player1)) {
      currentTurn = player2;
    } else {
      currentTurn = player1;
    }
  }

  public Move isValidMove(int row, int col) {
    if (row >= 0 && row < boardSize &&
        col >= 0 && col < boardSize &&
        board[row][col].getPlayer() == null &&
        !board[row][col].equals(previousMove)) {
        return board[row][col];
    }
    return null;
  }

  public Move isValidMove(int linearPosition) {
    return isValidMove(linearPosition / boardSize, linearPosition % boardSize);
  }

  public void makeMove() {
    passCounter += 1;
    setTurn();
  }

  public void makeMove(Move move) {
    move.setPlayer(currentTurn);
    passCounter = 0;
    setTurn();
  }

  public void capture(Move move) {

  }

  public boolean isGameOver() {
    return passCounter >= 2;
  }

  public String toString() {
    StringBuilder boardString = new StringBuilder();
    for (int row = 0; row < boardSize; row++) {
      for (int col = 0; col < boardSize; col++) {
        boardString.append(board[row][col].getSymbol()).append("  ");
      }
      boardString.append("\n");
    }
    return boardString.toString();
  }
}
