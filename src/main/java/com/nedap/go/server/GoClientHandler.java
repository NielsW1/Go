package com.nedap.go.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class GoClientHandler implements Runnable{

  private GoServer server;
  private Socket socket;
  private BufferedReader in;
  private BufferedWriter out;
  private String username;
  private boolean queued = false;

  public GoClientHandler(Socket socket, GoServer server) throws IOException {
    this.socket = socket;
    this.server = server;
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out = new BufferedWriter((new OutputStreamWriter(socket.getOutputStream())));
  }

  @Override
  public void run() {
    handleHandshake();
    try {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        handleInput(inputLine);
      }
    } catch (IOException ignored) {
    } finally {
      server.handleDisconnect(this);
      closeConnection();
    }
  }

  public String getUsername() {
    return username;
  }

  public void handleHandshake() {
    handleOutput(Protocol.hello() +
        "Welcome to the GO server! use LOGIN~<username> to log in.\n");
  }

  public void handleInput(String inputLine) {
    String[] parsedInput = inputLine.split(Protocol.SEPARATOR);
    try {
      switch (parsedInput[0]) {
        case Protocol.LOGIN:
          if (username == null) {
            username = parsedInput[1];
            server.broadCastMessage(this,
                Protocol.login() + username + " has joined the server.\n");
          } else {
            handleOutput(Protocol.error() +
                "You are already logged in!\n");
          }
          break;
        case Protocol.QUEUE:
          if (queued) {
            queued = false;
            handleOutput(Protocol.queue() + "You have been removed from the queue.\n");
          } else {
            queued = true;
            handleOutput(Protocol.queue() + "You are now in the queue.\n");
          }
          break;
      }
    } catch (IndexOutOfBoundsException ignored) {
    }
  }

  public void handleOutput(String outputLine) {
    try {
      out.write(outputLine);
      out.flush();
    } catch (IOException e) {
      closeConnection();
    }
  }

  public void closeConnection() {
    try {
      in.close();
      out.close();
      socket.close();
    } catch (IOException ignored) {
    }
  }
}
