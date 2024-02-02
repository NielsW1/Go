package com.nedap.go.gamelogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Goban {

  private final Stone[][] goban;
  private final int boardSize;
  private final Queue<Stone[][]> previousGobans;

  public Goban(int boardSize) {
    this.boardSize = boardSize;
    previousGobans = new LinkedList<>();
    goban = new Stone[boardSize][boardSize];
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        goban[i][j] = Stone.EMPTY;
      }
    }
  }

  public boolean isValidMove(int linearPosition) {
    return linearPosition >= 0 && linearPosition < boardSize * boardSize &&
        getStone(linearPosition) == Stone.EMPTY;
  }

  public List<Integer> getValidMoves(Stone stone) {
    List<Integer> validMoves = new ArrayList<>();
    for (int i = 0; i < boardSize * boardSize; i++) {
      try {
        if (isValidMove(i) && attemptMove(i, stone) != null) {
          validMoves.add(i);
        }
      } catch (IllegalMoveException ignored) {
      }
    }
    return validMoves;
  }

  public Goban attemptMove(int linearPosition, Stone stone) throws IllegalMoveException {
    if (!isValidMove(linearPosition)) {
      throw new IllegalMoveException("Invalid move! Try again.");
    }
    Goban gameCopy = gobanCopy();

    gameCopy.placeStone(linearPosition, stone);

    List<Integer> playerStones = new ArrayList<>();
    List<Integer> opponentStones = new ArrayList<>();
    List<Integer> neighbours = gameCopy.getNeighbours(linearPosition);

    for (int neighbour : neighbours) {
      if (gameCopy.getStone(neighbour) == stone) {
        playerStones.add(neighbour);
      } else if (gameCopy.getStone(neighbour) == stone.other()) {
        opponentStones.add(neighbour);
      }
    }

    if (neighbours.size() == opponentStones.size()) {
      playerStones.add(linearPosition);
    }

    for (int position : opponentStones) {
      gameCopy.captureStones(position, stone.other());
    }

    for (int position : playerStones) {
      gameCopy.captureStones(position, stone);
    }
    for (Stone[][] previousGoban : previousGobans) {
      if (Arrays.deepEquals(previousGoban, gameCopy.getGoban())) {
        return null;
      }
    }
    return gameCopy;
  }

  /**
   * Places a stone on the board, then checks if it leads to any captures.
   * It checks all the neighbouring stones and adds them to playerStones or
   * opponentStones. It then attempts to capture, starting from the neighbouring
   * opponent's moves.

   */

  public void makeMove(int linearPosition, Stone stone) throws IllegalMoveException {
    Goban gobanCopy = attemptMove(linearPosition, stone);
    if (gobanCopy != null) {
      if (previousGobans.size() > 1) {
        previousGobans.poll();
      }
      previousGobans.add(gobanCopy.getGoban());
      updateGoban(gobanCopy);
    } else {
      throw new IllegalMoveException("Invalid move! Try again.");
    }
  }

  public void makeBulkMoves(int[] moves, GoPlayer player) {
    for (int move : moves) {
      try {
        makeMove(move, player.getStone());
      } catch (IllegalMoveException ignored) {
      }
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

  public void updateGoban(Goban gobanCopy) {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        goban[i][j] = gobanCopy.getGoban()[i][j];
      }
    }
  }

  /**
   * Creates a StoneChain object for a stone belonging to the current player
   * starting from linearPosition.
   * Checks every neighbour starting from linearPosition and adds it to
   * the stones HashSet if the neighbour is the same as stone. Otherwise, the neighbour
   * is added to the adjacentStones HashSet. The method will keep checking neighbours
   * until no more surrounding stones belong to the current player.
   * @return StoneChain
   */

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

  /**
   * Gets the StoneChain starting from linearPosition and checks if all adjacent stones
   * belong to the other player. If true, removes all stones in the StoneChain from the board.
   * If only a single stone is captured in this move, sets the koMove to the position of the
   * captured stone.
   */

  public void captureStones(int linearPosition, Stone stone) {
    StoneChain stoneChain = getStoneChain(linearPosition, stone);
    for (int position : stoneChain.getAdjacentStones()) {
      if (getStone(position) == Stone.EMPTY) {
        return;
      }
    }
    for (int position : stoneChain.getStoneChain()) {
      placeStone(position, Stone.EMPTY);
    }
  }

  /**
   * Gets all neighbouring positions of linearPosition on the board, and returns them
   * as a list. Adjusts for corners and edges based on the drow and dcol arrays.
   * @return list of all neighbouring positions on the board.
   */

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

  /**
   * Scores the Goban. Checks each empty position on the board and gets the StoneChain
   * from this position. If all stones adjacent to this (empty) StoneChain belong to one
   * player, the territory belongs to that player and the entire StoneChain
   * is filled with that player's stones. If the StoneChain
   * is surrounded by stones from both players, the territory is neutral and the StoneChain
   * remains empty.
   */

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

  public String getScores() {
    int blackScore = 0;
    int whiteScore = 0;
    for (int i = 0; i < boardSize * boardSize; i++) {
      if (getStone(i) == Stone.BLACK) {
        blackScore += 1;
      } else if (getStone(i) == Stone.WHITE) {
        whiteScore += 1;
      }
    }
    return "BLACK: " + blackScore + "\nWHITE: " + whiteScore;
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

  public Stone[][] getGoban() {
    return goban;
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
}
