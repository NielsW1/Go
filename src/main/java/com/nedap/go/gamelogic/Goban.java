package com.nedap.go.gamelogic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Goban {

  private Stone[][] goban;
  private int boardSize;
  private int previousMove = -1;

  public Goban(int boardSize) {
    this.boardSize = boardSize;
    goban = new Stone[boardSize][boardSize];
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        goban[i][j] = Stone.EMPTY;
      }
    }
  }

  public boolean isValidMove(int linearPosition) {
    return linearPosition >= 0 && linearPosition < boardSize * boardSize &&
        getStone(linearPosition) == Stone.EMPTY &&
        previousMove != linearPosition;
  }

  public void makeMove(int linearPosition, GoPlayer player) throws IllegalMoveException {
    if (previousMove != -1 && linearPosition != previousMove) {
      previousMove = -1;
    }
    if (isValidMove(linearPosition)) {
      placeStone(linearPosition, player.getStone());

      List<Integer> playerStones = new ArrayList<>();
      List<Integer> opponentStones = new ArrayList<>();

      for (int neighbour : getNeighbours(linearPosition)) {
        if (getStone(neighbour) == player.getStone()) {
          playerStones.add(neighbour);
        } else if (getStone(neighbour) == player.getStone().other()) {
          opponentStones.add(neighbour);
        }
      }

      for (int position : opponentStones) {
        captureStones(position, player.getStone().other());
      }

      for (int position : playerStones) {
        captureStones(position, player.getStone());
      }
    } else {
      throw new IllegalMoveException();
    }
  }

  public void makeBulkMoves(int[] moves, GoPlayer player) {
    for (int move : moves) {
      try {
        makeMove(move, player);
      } catch (IllegalMoveException ignored) {}
    }
  }

  public Stone getStone(int linearPosition) {
    return goban[linearPosition / boardSize][linearPosition % boardSize];
  }

  public Stone getStone(int row, int col) {
    return goban[row][col];
  }

  public void placeStone(int linearPosition, Stone stone) {
    goban[linearPosition / boardSize][linearPosition % boardSize] = stone;
  }

  public StoneChain getStoneChain(int linearPosition, Stone stone) {
    StoneChain stoneChain = new StoneChain();
    Queue<Integer> frontOfChain = new LinkedList<>();
    frontOfChain.add(linearPosition);
    while (!frontOfChain.isEmpty()) {
      int currentPosition = frontOfChain.poll();
      stoneChain.addStone(currentPosition);
      for (int neighbour : getNeighbours(currentPosition)) {
        if (stone.equals(getStone(neighbour)) && !stoneChain.containsStone(neighbour)) {
          frontOfChain.add(neighbour);
        } else if (!stone.equals(getStone(neighbour))) {
          stoneChain.addAdjacent(neighbour);
        }
      }
    }
    return stoneChain;
  }

  public void captureStones(int linearPosition, Stone stone) {
    StoneChain stoneChain = getStoneChain(linearPosition, stone);
    for (int position : stoneChain.getAdjacentStones()) {
      if (getStone(position) == Stone.EMPTY) {
        return;
      }
    }
    if (stoneChain.getStoneChain().size() == 1) {
      previousMove = linearPosition;
    }
    for (int position : stoneChain.getStoneChain()) {
      placeStone(position, Stone.EMPTY);
    }
  }

  public List<Integer> getNeighbours(int linearPosition) {
    int row = linearPosition / boardSize;
    int col = linearPosition % boardSize;
    List<Integer> neighbourList = new ArrayList<>();
    int[] drow = {-1, 1, 0, 0};
    int[] dcol = {0, 0, -1, 1};

    for (int i = 0; i < 4; i++) {
      int adjustedRow = row + drow[i];
      int adjustedCol = col + dcol[i];

      if (adjustedRow >= 0 && adjustedRow < boardSize && adjustedCol >= 0
          && adjustedCol < boardSize) {
        neighbourList.add(adjustedRow * boardSize + adjustedCol);
      }
    }
    return neighbourList;
  }

  public void scoreGoban() {
    for (int position = 0; position < boardSize * boardSize; position++) {
      if (getStone(position) == Stone.EMPTY) {
        StoneChain emptyTerritory = getStoneChain(position, Stone.EMPTY);
        if (territoryBelongsToPlayer(Stone.BLACK, emptyTerritory.getAdjacentStones())) {
          for (int pos : emptyTerritory.getStoneChain()) {
            placeStone(pos, Stone.BLACK);
          }
        } else if (territoryBelongsToPlayer(Stone.WHITE, emptyTerritory.getAdjacentStones())) {
          for (int pos : emptyTerritory.getStoneChain()) {
            placeStone(pos, Stone.WHITE);
          }
        }
      }
    }
  }

  public boolean territoryBelongsToPlayer(Stone stone, List<Integer> adjacentStone) {
    for (int position : adjacentStone) {
      if (getStone(position) != stone) {
        return false;
      }
    }
    return true;
  }

  public Goban gobanCopy() {
    Goban gobanCopy = new Goban(boardSize);
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        gobanCopy.goban[i][j] = goban[i][j];
      }
    }
    return gobanCopy;
  }

  public String toString() {
    StringBuilder boardString = new StringBuilder();

    boardString.append("   ");
    for (int n = 0; n < boardSize; n++) {
      boardString.append(n).append("  ");
    }
    boardString.append("\n");

    for (int row = 0; row < boardSize; row++) {
      boardString.append(row).append("  ");
      for (int col = 0; col < boardSize; col++) {
        boardString.append(getStone(row, col)).append("  ");
      }
      if (row < boardSize - 1){
        boardString.append("\n");
      }
    }
    return boardString.toString();
  }

  public String toStringTest() {
    StringBuilder boardString = new StringBuilder();
    for (int row = 0; row < boardSize; row++) {
      for (int col = 0; col < boardSize; col++) {
        boardString.append(getStone(row, col)).append("  ");
      }
      for (int n = 0; n < boardSize; n++) {
        int position = row * boardSize + n;
        boardString.append(position < 10 ? position + " " : position).append("  ");
      }
      boardString.append("\n");
    }
    return boardString.toString().trim();
  }
}
