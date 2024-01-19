package com.nedap.go.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class GoPlayer {
  private String username;
  private String symbol;
  private int score;
  private Socket clientSocket;

  public GoPlayer(InetAddress address, int port) throws IOException {
    clientSocket = new Socket(address, port);
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
