package com.nedap.go.ai;

import com.nedap.go.gamelogic.IllegalMoveException;
import com.nedap.go.gamelogic.NotYourTurnException;
import com.nedap.go.server.GoProtocol;
import java.util.List;

public class GoNaivePlayer {

  private final GoAIClient client;

  public GoNaivePlayer(GoAIClient client) {
    this.client = client;
  }

  public void makeMove() {
    List<Integer> validMoves = client.getGoban().getValidMoves(client.getStone());
    try {
      if (validMoves.size() < 3) {
        client.handleOutput(GoProtocol.PASS);
      }
        int randomMove = validMoves.get((int) (Math.random() * validMoves.size()));
        client.handleOutput(GoProtocol.MOVE + GoProtocol.SEPARATOR + randomMove);
    } catch (IllegalMoveException | NotYourTurnException | NumberFormatException |
             IndexOutOfBoundsException e) {
      client.sendToTUI(e.getMessage());
    }
  }

}
