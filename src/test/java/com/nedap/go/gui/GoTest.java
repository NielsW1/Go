package com.nedap.go.gui;


import com.nedap.go.gamelogic.GoPlayer;
import com.nedap.go.gamelogic.GoGame;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GoTest {

  private GoGame game;
  private GoPlayer player1;
  private GoPlayer player2;

  @BeforeEach
  void setup() throws IOException {
    player1 = new GoPlayer("Henk");
    player2 = new GoPlayer("Piet");
    game = new GoGame(9, player1, player2);
    player1.game = game;
    player2.game = game;
  }

  @Test
  void testTurns() {
    assertEquals(game.currentTurn, player1);
    player1.doMove(0,0);
    assertFalse(player2.doMove(0,0));
    System.out.println(game.toString());
  }

  @Test
  void testCapture() {
    game.fixTurn = true;
    player1.doMove(1, 3);
    player1.doMove(1, 4);
    player1.doMove(1, 5);
    player1.doMove(2, 3);
    player1.doMove(2, 4);
    player1.doMove(2, 5);
    game.currentTurn = player2;
    player2.doMove(0, 3);
    player2.doMove(0, 4);
    player2.doMove(0, 5);
    player2.doMove(3,3);
    player2.doMove(3, 4);
    player2.doMove(3, 5);
    player2.doMove(1, 2);
    player2.doMove(2, 2);
    player2.doMove(1, 6);
    System.out.println(game.toString());
    player2.doMove(2, 6);
    System.out.println(game.toString());
  }
  @Test
  void testCaptureCorner() {
    game.fixTurn = true;
    player1.doMove(0, 0);
    game.currentTurn = player2;
    player2.doMove(0, 1);
    System.out.println(game.toString());
    player2.doMove(1, 0);
    System.out.println(game.toString());
  }

  @Test
  void testKOrule() {
    game.fixTurn = true;
    player1.doMove(0,1);
    player1.doMove(1, 0);
    player1.doMove(1,2);
    player1.doMove(2, 1);
    game.currentTurn = player2;
    player2.doMove(0,2);
    player2.doMove(1, 3);
    player2.doMove(2,2);
    player2.doMove(1,1);
    System.out.println(game.toString());
    game.currentTurn = player1;
    assertFalse(player1.doMove(1,2));
    System.out.println(game.toString());
    player1.doMove(0,0);
    player1.doMove(1, 2);
    System.out.println(game.toString());
    game.currentTurn = player2;
    assertFalse(player2.doMove(1, 1));
  }

  @Test
  void testNeighbours() {
  }

}
