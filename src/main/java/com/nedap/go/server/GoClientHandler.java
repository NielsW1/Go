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
    waitOneSecond();
    handleOutput(GoProtocol.HELLO + GoProtocol.SEPARATOR +
        "Welcome to the GO server! use LOGIN~<username> to log in.");
  }

  public void handleInput(String inputLine) {
    String[] parsedInput = inputLine.split(GoProtocol.SEPARATOR);
    try {
      switch (parsedInput[0]) {

        case GoProtocol.LOGIN:
          if (username == null) {
            if (!server.userAlreadyLoggedIn(this, parsedInput[1])) {
              username = parsedInput[1];
              handleOutput(GoProtocol.ACCEPTED + GoProtocol.SEPARATOR + "Login successful.");
              server.broadCastMessage(GoProtocol.LOGIN + GoProtocol.SEPARATOR + username + " has joined the server.");
            } else {
              handleOutput(GoProtocol.REJECTED + GoProtocol.SEPARATOR + "User by that name already logged in!");
            }
          } else {
            handleOutput(GoProtocol.ERROR + GoProtocol.SEPARATOR +
                "You are already logged in as " + username + "!");
          }
          break;

        case GoProtocol.QUEUE:
          if (username != null) {
            if (server.getQueue().contains(this)) {
              server.removeFromQueue(this);
              server.broadCastMessage(GoProtocol.QUEUED + GoProtocol.SEPARATOR + username + " has left the queue.");
            } else {
              server.addToQueue(this);
              server.broadCastMessage(GoProtocol.QUEUED + GoProtocol.SEPARATOR + username + " has joined the queue.");
              if (server.getQueue().size() == 2) {

              }
            }
          }
          break;

        case GoProtocol.MOVE:

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
