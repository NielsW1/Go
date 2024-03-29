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
  private Stone stone;
  private int score;
  private String color;

  public GoPlayer(String username) {
    this.username = username;
  }

  public void setStone(Stone stone) {
    this.stone = stone;
    color = stone.getColor();
  }

  public void incrementScore() {
    score += 1;
  }

  public Stone getStone() {
    return stone;
  }

  public String getColor() {
    return color;
  }

  public String getUsername() {
    return username;
  }

  public int getScore() {
    return score;
  }

  public void resetPlayer() {
    stone = null;
    color = null;
    score = 0;
  }
}
