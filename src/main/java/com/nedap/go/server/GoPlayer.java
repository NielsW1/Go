package com.nedap.go.server;

public class GoPlayer {
  private String username;
  private String symbol;
  private int score;

  public GoPlayer(String username) {
    this.username = username;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public void incrementScore(int amount) {
    score += amount;
  }

  public String getUsername() {
    return username;
  }

  public String getSymbol() {
    return symbol;
  }

  public int getScore() {
    return score;
  }

  public void resetPlayer() {
    symbol = null;
    score = 0;
  }
}
