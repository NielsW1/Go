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

  public boolean getQueued() {
    return queued;
  }

  public void handleHandshake() {
    waitOneSecond();
    handleOutput(Protocol.hello() +
        "Welcome to the GO server! use LOGIN~<username> to log in.");
  }

  public void handleInput(String inputLine) {
    String[] parsedInput = inputLine.split(Protocol.SEPARATOR);
    try {
      switch (parsedInput[0]) {

        case Protocol.LOGIN:
          if (username == null) {
            if (!server.userAlreadyLoggedIn(this, parsedInput[1])) {
              username = parsedInput[1];
              server.broadCastMessage(this,
                  Protocol.login() + username + " has joined the server.");
            } else {
              handleOutput(Protocol.error() + "User by that name already logged in!");
            }
          } else {
            handleOutput(Protocol.error() +
                "You are already logged in as " + username + "!");
          }
          break;

        case Protocol.QUEUE:
          if (username != null) {
            if (queued) {
              queued = false;
              server.broadCastMessage(this, Protocol.queue() + username + " has left the queue.");
            } else {
              queued = true;
              server.broadCastMessage(this, Protocol.queue() + username + " has joined the queue.");
              handleOutput(server.getQueue());
            }
          }
          break;
      }
    } catch (IndexOutOfBoundsException ignored) {
    }
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
      socket.close();
    } catch (IOException ignored) {
    }
  }

  public void waitOneSecond() {
    try {
      Thread.sleep(1000);
    } catch (InterruptedException ignored) {
    }
  }
}
