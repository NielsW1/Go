package com.nedap.go.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

public class GoClient {
  private Socket clientSocket;
  private BufferedReader in;
  private BufferedWriter out;
  public GoClientTUI client;

  public GoClient(InetAddress address, int port) throws IOException {
    clientSocket = new Socket(address, port);
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

  public void closeConnection() {
    try {
      clientSocket.close();
    } catch (IOException ignored) {
    }
  }
}
