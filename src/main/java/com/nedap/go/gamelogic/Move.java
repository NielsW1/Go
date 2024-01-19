package com.nedap.go.gamelogic;

import com.nedap.go.client.GoPlayer;

public class Move {

  private int row;
  private int col;
  private int linearPosition;
  private GoPlayer player;
  private String symbol = "•";
  private boolean captured = false;

  public Move(int row, int col, int boardSize) {
    this.row = row;
    this.col = col;
    linearPosition = row * boardSize + col;
  }

  public void setPlayer(GoPlayer player) {
    this.player = player;
    this.symbol = player.getSymbol();
  }

  public GoPlayer getPlayer() {
    return player;
  }

  public String getSymbol() {
    return symbol;
  }

  public boolean wasCaptured() {
    return captured;
  }
}
