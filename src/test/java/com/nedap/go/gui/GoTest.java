package com.nedap.go.gui;


import com.nedap.go.gamelogic.GoPlayer;
import com.nedap.go.gamelogic.GoGame;
import com.nedap.go.gamelogic.Goban;
import com.nedap.go.gamelogic.IllegalMoveException;
import com.nedap.go.gamelogic.NotYourTurnException;
import com.nedap.go.gamelogic.Stone;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
  void testToString() {
    System.out.println(game.toString());
  }

  @Test
  void testMoves() {
    assertThrows(IllegalMoveException.class, () -> {
      game.makeMove(100, player1);
    });
    assertThrows(IllegalMoveException.class, () -> {
      game.makeMove(10, 10, player1);
    });

    try {
      game.makeMove(0, player1);
      game.makeMove(1, player2);
    } catch (IllegalMoveException | NotYourTurnException ignored) {}

    assertEquals(Stone.BLACK, game.getGoban().getStone(0));
    assertEquals(Stone.WHITE, game.getGoban().getStone(1));
    assertThrows(IllegalMoveException.class, () -> {
      game.makeMove(0, player1);
    });
    assertEquals(Stone.BLACK, game.getGoban().getStone(0));
  }

  @Test
  void testTurns() {
    assertThrows(NotYourTurnException.class, () -> {
      game.makeMove(3, player2);
    });

    try {
      game.makeMove(0, player1);
    } catch (IllegalMoveException | NotYourTurnException ignored) {
    }

    assertThrows(NotYourTurnException.class, () -> {
      game.makeMove(3, player1);
    });
  }

  @Test
  void testCapture() {
    int[] moves = new int[]{1, 9, 11, 19};
    gobanCopy.makeBulkMoves(moves, player1);
    try {
      game.fixTurn = true;
      game.makeMove(1, player1);
      game.makeMove(9, player1);
      game.fixTurn = false;
      game.makeMove(11, player1);
      game.makeMove(10, player2);
      game.makeMove(19, player1);
    } catch (IllegalMoveException | NotYourTurnException ignored) {
    }
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureMultipleStones() {
    int[] moves = new int[]{12, 13, 14, 21, 22, 23, 30, 31, 32};
    int[] oppMoves = new int[]{3, 4, 5, 11, 15, 20, 24, 29, 33, 39, 40, 41};
    gobanCopy.makeBulkMoves(oppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureMultipleStones2() {
    int[] moves = new int[]{14, 23, 32, 41, 50, 59, 68};
    int[] oppMoves = new int[]{5, 13, 15, 22, 24, 31, 33, 40, 42, 49, 51, 58, 60, 67, 69, 77};
    gobanCopy.makeBulkMoves(oppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureComplexPattern() {
    int[] moves = new int[]{13, 21, 22, 23, 30, 31, 32, 39, 40, 41, 48, 49, 50, 51, 56, 57, 58, 59,
        66, 67, 68};
    int[] oppMoves = new int[]{4, 14, 24, 33, 42, 52, 60, 69, 77, 76, 75, 65, 64, 55, 46, 47, 38,
        29, 20, 12};
    gobanCopy.makeBulkMoves(oppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureCorner() {
    int[] moves = new int[]{1, 9};
    gobanCopy.makeBulkMoves(moves, player1);
    try {
      game.makeMove(1, player1);
      game.makeMove(0, player2);
      game.makeMove(9, player1);
    } catch (IllegalMoveException | NotYourTurnException ignored) {
    }
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureEdge() {
    int[] moves = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
    int[] oppMoves = new int[]{9, 10, 11, 12, 13, 14, 15, 16, 17};
    gobanCopy.makeBulkMoves(oppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testCaptureEdge2() {
    int[] moves = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 53, 54,
        62, 63, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80};
    int[] oppMoves = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 25, 28, 34, 37, 43, 46, 52, 55, 61,
        64, 65, 66, 67, 68, 69, 70};
    gobanCopy.makeBulkMoves(oppMoves, player2);
    game.makeBulkMoves(moves, player1);
    game.makeBulkMoves(oppMoves, player2);
    assertEquals(gobanCopy.toString(), game.toString());
  }

  @Test
  void testKOrule() {

  }

  @Test
  void testNeighbours() {

  }

}
