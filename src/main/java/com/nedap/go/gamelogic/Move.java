package com.nedap.go.gamelogic;

public class Move {

  private int row;
  private int col;
  private int linearPosition;
  private GoPlayer player;
  private String symbol = "•";

  public Move(int row, int col, int boardSize) {
    this.row = row;
    this.col = col;
    linearPosition = row * boardSize + col;
  }

  public int getRow() {
    return row;
  }

  public int getCol() {
    return col;
  }

  public GoPlayer getPlayer() {
    return player;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setPlayer(GoPlayer player) {
    this.player = player;
    if (player == null) {
      this.symbol = "•";
    } else {
      this.symbol = player.getSymbol();
    }
  }

}
