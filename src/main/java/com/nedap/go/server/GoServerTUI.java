package com.nedap.go.server;

import java.io.IOException;
import java.util.Scanner;

public class GoServerTUI {

  private Scanner input;
  private GoServer server;

  public GoServerTUI() {
    input = new Scanner(System.in);
    startServer();
  }

  public void startServer() {
    int port;
    System.out.println("Enter a port to start listening for connections:");
    while (server == null) {
      if (input.hasNextLine()) {
        try {
          port = Integer.parseInt(input.nextLine());
          if (port < 0 || port > 65535) {
            System.out.println("Valid port numbers range between 0 and 65535.");
            continue;
          }
          server = new GoServer(port, this);
          System.out.println("Starting server on port " + server.getServerPort());
          Thread thread = new Thread(server);
          thread.start();
          runServerTUI();

        } catch (NumberFormatException e) {
          System.out.println("Not a valid port.");
        } catch (IOException e) {
          System.out.println("Unable to start server.");
        }
      }
    }
  }

  public void runServerTUI() {
    boolean runServer = true;
    System.out.println("Use <EXIT> or <QUIT> to close the server.");
    while (runServer) {
      String inputLine;
      if (input.hasNextLine()) {
        inputLine = input.nextLine();
        switch (inputLine.toUpperCase()) {
          case "EXIT":
          case "QUIT":
            System.out.println("Closing server....");
            server.closeServer();
            runServer = false;
            break;
        }
      }
    }
  }

  public void receiveMessage(String message) {
    System.out.println(message);
  }

  public static void main(String[] args) {
    new GoServerTUI();
  }
}
