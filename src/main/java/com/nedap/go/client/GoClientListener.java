package com.nedap.go.client;

import com.nedap.go.Go;
import com.nedap.go.gamelogic.Stone;

public class GoClientListener {

  private final Go gui;
  private final GoClient client;
  private final int boardSize;

  public GoClientListener(int boardSize, GoClient client) {
    this.client = client;
    this.boardSize = boardSize;
    gui = new Go(boardSize);
  }

  public void updateGUI() {
    for (int row = 0; row < boardSize; row++) {
      for (int col = 0; col < boardSize; col++) {
        if (client.getGoban().getStone(row, col) == Stone.BLACK) {
          gui.placeStone(col, row, false);
        } else if (client.getGoban().getStone(row, col) == Stone.WHITE) {
          gui.placeStone(col, row, true);
        } else {
          gui.removeStone(col, row);
        }
      }
    }
  }

  public void clearGUI() {
    gui.clearGUI();
  }
}
