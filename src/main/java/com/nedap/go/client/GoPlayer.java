package com.nedap.go.client;

import com.nedap.go.server.Protocol;
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
  private Socket clientSocket;
  private BufferedReader in;
  private BufferedWriter out;
  private ClientTUI client;

  public GoPlayer(InetAddress address, int port, ClientTUI client) throws IOException {
    clientSocket = new Socket(address, port);
    this.client = client;
    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    receiveInput();
  }

  public void receiveInput() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          String inputLine;
          while ((inputLine = in.readLine()) != null) {
            handleInput(inputLine);
          }
        } catch (IOException ignored) {
        } finally {
          closeConnection();
        }
      }
    }).start();
  }

  public void handleInput(String inputLine) {
    String[] parsedInput = inputLine.split(Protocol.SEPARATOR + "\\s");
    if (parsedInput[0].equals(Protocol.LOGIN)) {
      username = parsedInput[1];
    }
    client.receiveInput(inputLine);
  }

  public void handleOutput(String outputLine) {
    try {
      out.write(outputLine);
      out.newLine();
      out.flush();
    } catch (IOException e) {
      closeConnection();
    }
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

  public void closeConnection() {
    try {
      clientSocket.close();
    } catch (IOException ignored) {
    }
  }
}
