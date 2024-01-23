package com.nedap.go.gamelogic;

import com.nedap.go.client.GoClientTUI;
import com.nedap.go.server.GoProtocol;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class GoPlayer {
  private String username;
  private String symbol;
  private int score;
  public GoGame game;

  public GoPlayer(String username) {
    this.username = username;
  }

  public boolean doMove(int row, int col) {
    if (game.getTurn().equals(this)) {
      Move move = game.isValidMove(row, col);
      if (move != null) {
        game.makeMove(move);
        return true;
      }
    }
    return false;
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
