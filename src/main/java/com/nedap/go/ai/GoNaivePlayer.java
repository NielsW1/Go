package com.nedap.go.ai;

import com.nedap.go.client.GoClient;
import com.nedap.go.gamelogic.IllegalMoveException;
import com.nedap.go.gamelogic.NotYourTurnException;
import com.nedap.go.server.GoProtocol;
import java.util.ArrayList;
import java.util.List;

public class GoNaivePlayer {

  private GoAIClient client;

  public GoNaivePlayer(GoAIClient client) {
    this.client = client;
  }

  public void makeMove() {
    List<Integer> validMoves = client.getGoban().getValidMoves();
    boolean invalid = true;
    try {
      if (validMoves.size() < 2) {
        client.handleOutput(GoProtocol.PASS);
      }
      while (invalid) {
        int randomMove = validMoves.get((int) (Math.random() * validMoves.size()));
        client.handleOutput(GoProtocol.MOVE + GoProtocol.SEPARATOR + randomMove);
        invalid = false;

      }
    } catch (IllegalMoveException | NotYourTurnException | NumberFormatException |
             IndexOutOfBoundsException e) {
      client.getClientTUI().receiveInput(e.getMessage());
    }

  }

}
