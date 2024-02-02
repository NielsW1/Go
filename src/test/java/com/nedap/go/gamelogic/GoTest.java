package com.nedap.go.gamelogic;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;

public class GoTest {

  private GoGame game;
  private GoPlayer player1;
  private GoPlayer player2;
  private Goban gobanCopy;

  @BeforeEach
  void setup() {
    player1 = new GoPlayer("Henk");
    player2 = new GoPlayer("Piet");
    game = new GoGame(9, player1, player2);
    gobanCopy = game.getGobanCopy();
  }

  @Test
  void testMoves() {
    assertThrows(IllegalMoveException.class, () -> game.makeMove(100, player1));
    assertThrows(IllegalMoveException.class, () -> game.makeMove(10, 10, player1));
    try {
      game.makeMove(0, player1);
      game.makeMove(1, player2);
    } catch (IllegalMoveException | NotYourTurnException e) {
      e.printStackTrace();
    }
    assertEquals(Stone.BLACK, game.getGoban().getStone(0));
    assertEquals(Stone.WHITE, game.getGoban().getStone(1));
    assertThrows(IllegalMoveException.class, () -> game.makeMove(1, player1));
    assertEquals(Stone.WHITE, game.getGoban().getStone(1));
  }

  @Test
  void testTurns() {
    assertEquals(player1, game.getTurn());
    assertThrows(NotYourTurnException.class, () -> game.makeMove(3, player2));
    try {
      game.makeMove(0, player1);
    } catch (IllegalMoveException | NotYourTurnException e) {
      e.printStackTrace();
    }
    assertEquals(player2, game.getTurn());
    assertThrows(NotYourTurnException.class, () -> game.makeMove(3, player1));
    try {
      game.makeMove(1, player2);
    } catch (IllegalMoveException | NotYourTurnException e) {
      e.printStackTrace();
    }
    assertEquals(player1, game.getTurn());
    assertThrows(NotYourTurnException.class, () -> game.makeMove(3, player2));
  }

  @Test
  void testCapture() {
    int[] moves = {1, 9, 11, 19};
    gobanCopy.makeBulkMoves(moves, player1);
    try {
      game.fixTurn = true;
      game.makeMove(1, player1);
      game.makeMove(9, player1);
      game.fixTurn = false;
      game.makeMove(11, player1);
      game.makeMove(10, player2);
      game.makeMove(19, player1);
    } catch (IllegalMoveException | NotYourTurnException e) {
      e.printStackTrace();
    }
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureMultipleStones() {
    int[] moves = {12, 13, 14, 21, 22, 23, 30, 31, 32};
    int[] oppMoves = {3, 4, 5, 11, 15, 20, 24, 29, 33, 39, 40, 41};
    gobanCopy.makeBulkMoves(oppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureMultipleStones2() {
    int[] moves = {14, 23, 32, 41, 50, 59, 68};
    int[] oppMoves = {5, 13, 15, 22, 24, 31, 33, 40, 42, 49, 51, 58, 60, 67, 69, 77};
    gobanCopy.makeBulkMoves(oppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureComplexPattern() {
    int[] moves = {13, 21, 22, 23, 30, 31, 32, 39, 40, 41, 48, 49, 50, 51, 56, 57, 58, 59,
        66, 67, 68};
    int[] oppMoves = {4, 14, 24, 33, 42, 52, 60, 69, 77, 76, 75, 65, 64, 55, 46, 47, 38,
        29, 20, 12};
    gobanCopy.makeBulkMoves(oppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureCorner() {
    int[] moves = {1, 9};
    gobanCopy.makeBulkMoves(moves, player1);
    try {
      game.makeMove(1, player1);
      game.makeMove(0, player2);
      game.makeMove(9, player1);
    } catch (IllegalMoveException | NotYourTurnException e) {
      e.printStackTrace();
    }
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureEdge() {
    int[] moves = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    int[] oppMoves = {9, 10, 11, 12, 13, 14, 15, 16, 17};
    gobanCopy.makeBulkMoves(oppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureEdge2() {
    int[] moves = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 53, 54,
        62, 63, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80};
    int[] oppMoves = {10, 11, 12, 13, 14, 15, 16, 19, 25, 28, 34, 37, 43, 46, 52, 55, 61,
        64, 65, 66, 67, 68, 69, 70};
    gobanCopy.makeBulkMoves(oppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureEdge3() {
    int[] moves = {0, 1, 2, 9, 10, 11, 18, 19, 20, 27, 28, 29, 36, 38, 45, 46};
    int[] oppMoves = {3, 12, 21, 30, 39, 47, 54, 55};
    gobanCopy.makeBulkMoves(oppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    try {
      game.setTurn();
      game.makeMove(4,1, player2);
      gobanCopy.makeMove(37, Stone.WHITE);
    } catch (IllegalMoveException | NotYourTurnException e) {
      e.printStackTrace();
    }
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureMultipleGroups() {
    int[] moves = {29, 30, 38, 39, 32, 41, 49};
    int[] oppMoves = {20, 21, 23, 28, 33, 37, 40, 42, 47, 48, 50, 58};
    int[] testOppMoves = {20, 21, 23, 28, 31, 33, 37, 40, 42, 47, 48, 50, 58};
    gobanCopy.makeBulkMoves(testOppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    try {
      game.setTurn();
      game.makeMove(31, player2);
    } catch (IllegalMoveException | NotYourTurnException e) {
      e.printStackTrace();
    }
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testScoring() {
    int[] moves = {22, 32, 40, 30};
    int[] oppMoves = {10, 11, 12, 13, 14, 15, 16, 19, 25, 28, 34, 37, 43, 46, 52, 55, 61,
        64, 65, 66, 67, 68, 69, 70};
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    game.scoreGame();
    assertEquals(player2, game.getWinner());
    assertEquals(56, player2.getScore());
    assertEquals(5, player1.getScore());
  }

  @Test
  void testScoring2() {
    int[] moves = {8, 16, 24, 32, 40, 48, 56, 64, 72};
    int[] oppMoves = {7, 15, 23, 31, 39, 47, 55, 63};
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    game.scoreGame();
    assertEquals(player1, game.getWinner());
    assertEquals(45, player1.getScore());
    assertEquals(36, player2.getScore());
  }

  @Test
  void testDraw() {
    int[] moves = {0, 1, 9, 10};
    int[] oppMoves = {80, 79, 71, 70};
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    game.scoreGame();
    assertNull(game.getWinner());
    assertEquals(4, player1.getScore());
    assertEquals(4, player2.getScore());
  }

  @Test
  void testKOrule() {
    int[] moves = {11, 19, 21, 29};
    int[] oppMoves = {12, 22, 30};
    int[] testMoves = {0, 11, 19, 21, 29};
    int[] testOppMoves = {1, 12, 22, 30};
    gobanCopy.makeBulkMoves(testMoves, player1);
    gobanCopy.makeBulkMoves(testOppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    game.setTurn();
    try {
      game.makeMove(2, 2, player2);
    } catch (IllegalMoveException | NotYourTurnException e) {
      e.printStackTrace();
    }
    assertThrows(IllegalMoveException.class, () -> game.makeMove(2, 3, player1));
    try {
      game.makeMove(0, player1);
      game.makeMove(1, player2);
      game.makeMove(2, 3, player1);
    } catch (IllegalMoveException | NotYourTurnException e) {
      e.printStackTrace();
    }
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testMultipleSelfCapture() {
    int[] moves = {10, 11, 19, 20, 29};
    int[] oppMoves = {0, 1, 2, 3, 9, 12, 18, 21, 28, 31, 38, 39, 40};
    gobanCopy.makeBulkMoves(oppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    System.out.println(game.toString());
    try {
      game.makeMove(3, 3, player1);
    } catch (IllegalMoveException | NotYourTurnException e) {
      e.printStackTrace();
    }
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testSuicideFullBoard() {
    int[] oppMoves = new int[80];
    for (int i = 1; i < game.getBoardSize() * game.getBoardSize() - 1; i++) {
      oppMoves[i] = i;
    }
    game.makeBulkMoves(oppMoves, player2);
    try {
      gobanCopy.makeMove(80, Stone.BLACK);
      game.makeMove(80, player1);
    } catch (IllegalMoveException | NotYourTurnException e) {
      e.printStackTrace();
    }
    assertEquals(gobanCopy.toString(), game.toString());

  }

}
