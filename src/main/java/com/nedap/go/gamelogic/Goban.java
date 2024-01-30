package com.nedap.go.gamelogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Goban {

  private Stone[][] goban;
  private final int boardSize;
  private int koMove = -1;

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
        getStone(linearPosition) == Stone.EMPTY && koMove != linearPosition;
  }


  public void makeMove(int linearPosition, Stone stone) throws IllegalMoveException {
    if (isValidMove(linearPosition)) {
      koMove = -1;
      List<Integer> playerStones = new ArrayList<>();
      List<Integer> opponentStones = new ArrayList<>();
      List<Integer> neighbours = getNeighbours(linearPosition);

      placeStone(linearPosition, stone);

      for (int neighbour : neighbours) {
        if (getStone(neighbour) == stone) {
          playerStones.add(neighbour);
        } else if (getStone(neighbour) == stone.other()) {
          opponentStones.add(neighbour);
        }
      }

      if (neighbours.size() == opponentStones.size()) {
        playerStones.add(linearPosition);
      }

      for (int position : opponentStones) {
        captureStones(position, stone.other());
      }

      for (int position : playerStones) {
        captureStones(position, stone);
      }
    } else {
      throw new IllegalMoveException("Invalid move! Try again.");
    }
  }

  public void makeBulkMoves(int[] moves, GoPlayer player) {
    for (int move : moves) {
      try {
        makeMove(move, player.getStone());
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
      koMove = linearPosition;
    }
    for (int position : stoneChain.getStoneChain()) {
      placeStone(position, Stone.EMPTY);
    }
  }

  public List<Integer> getNeighbours(int linearPosition) {
    int row = linearPosition / boardSize;
    int col = linearPosition % boardSize;
    int[] drow = {-1, 1, 0, 0};
    int[] dcol = {0, 0, -1, 1};
    List<Integer> neighbourList = new ArrayList<>();

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
    HashSet<Integer> neutralTerritory = new HashSet<>();

    for (int position = 0; position < boardSize * boardSize; position++) {
      if (getStone(position) == Stone.EMPTY && !neutralTerritory.contains(position)) {
        StoneChain emptyTerritory = getStoneChain(position, Stone.EMPTY);

        if (territoryBelongsToPlayer(Stone.BLACK, emptyTerritory.getAdjacentStones())) {
          for (int pos : emptyTerritory.getStoneChain()) {
            placeStone(pos, Stone.BLACK);
          }

        } else if (territoryBelongsToPlayer(Stone.WHITE, emptyTerritory.getAdjacentStones())) {
          for (int pos : emptyTerritory.getStoneChain()) {
            placeStone(pos, Stone.WHITE);
          }

        } else {
          neutralTerritory.addAll(emptyTerritory.getStoneChain());
        }
      }
    }
  }

  public boolean territoryBelongsToPlayer(Stone stone, HashSet<Integer> adjacentStone) {
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
      if (row < boardSize - 1) {
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
