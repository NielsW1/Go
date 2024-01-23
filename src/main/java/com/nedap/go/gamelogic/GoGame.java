package com.nedap.go.gamelogic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GoGame {

  private int boardSize;
  private int passCounter;
  private Move[][] board;
  private Move previousMove;
  public GoPlayer currentTurn;
  private final GoPlayer player1;
  private final GoPlayer player2;
  public boolean fixTurn = false;

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
    if (!fixTurn) {
      if (currentTurn.equals(player1)) {
        currentTurn = player2;
      } else {
        currentTurn = player1;
      }
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

  public void pass() {
    passCounter += 1;
    if (isGameOver()) {

    } else {
      setTurn();
    }
  }

  public void makeMove(Move move) {
    if (previousMove != null && !previousMove.equals(move)) {
      previousMove = null;
    }
    move.setPlayer(currentTurn);
    passCounter = 0;
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (board[i][j].getPlayer() != null && board[i][j].getPlayer().equals(getOpponent())) {
          getStoneChain(board[i][j], getOpponent());
        }
      }
    }
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (board[i][j].getPlayer() != null && board[i][j].getPlayer().equals(getTurn())) {
          getStoneChain(board[i][j], getTurn());
        }
      }
    }
    setTurn();
  }

  public void getStoneChain(Move move, GoPlayer playerTurn) {
    List<Move> stoneChain = new ArrayList<>();
    List<Move> reachedByChain = new ArrayList<>();
    Queue<Move> frontOfChain = new LinkedList<>();
    frontOfChain.add(move);
    while (!frontOfChain.isEmpty()) {
      Move currentMove = frontOfChain.remove();
      stoneChain.add(currentMove);
      for (Move neighbour : getNeighbours(currentMove.getRow(), currentMove.getCol())) {
        if (playerTurn.equals(neighbour.getPlayer()) && !stoneChain.contains(neighbour)) {
          frontOfChain.add(neighbour);
        } else {
          reachedByChain.add(neighbour);
        }
      }
    }
    if (captureCheck(reachedByChain)) {
      capture(stoneChain, move);
    }
  }

  public boolean captureCheck(List<Move> reachedByChain) {
    for (Move move : reachedByChain) {
      if (move.getPlayer() == null) {
        return false;
      }
    }
    return true;
  }

  public void capture(List<Move> stoneChain, Move move) {
    if (stoneChain.size() == 1) {
      previousMove = move;
    }
    for (Move m : stoneChain) {
      m.setPlayer(null);
    }
  }

  public List<Move> getNeighbours(int row, int col) {
    List<Move> neighbourList = new ArrayList<>();
    if (row > 0 && row < boardSize) {
      neighbourList.add(board[row - 1][col]);
      neighbourList.add(board[row + 1][col]);
    } else if (row == 0) {
      neighbourList.add(board[row + 1][col]);
    } else {
      neighbourList.add(board[row - 1][col]);
    }
    if (col > 0 && col < boardSize) {
      neighbourList.add(board[row][col - 1]);
      neighbourList.add(board[row][col + 1]);
    } else if (col == 0) {
      neighbourList.add(board[row][col + 1]);
    } else {
      neighbourList.add(board[row][col - 1]);
    }
    return neighbourList;
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
