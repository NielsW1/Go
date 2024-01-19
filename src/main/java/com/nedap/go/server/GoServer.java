package com.nedap.go.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class GoServer {

  private boolean runServer = true;
  private final ServerSocket serverSocket;
  public ArrayList<GoClientHandler> handlers;

  public GoServer(int port) throws IOException {
    serverSocket = new ServerSocket(port);
    handlers = new ArrayList<>();
  }

  public int getServerPort() {
    return serverSocket.getLocalPort();
  }

  public List<GoClientHandler> getConnectedClients() {
    return handlers;
  }

  public String getQueue() {
    StringBuilder currentQueue = new StringBuilder();
    currentQueue.append("Currently queued players:\n");
    for (GoClientHandler handler : handlers) {
      if (handler.getQueued()) {
        try {
          currentQueue.append(handler.getUsername()).append("\n");
        } catch (NullPointerException ignored) {
        }
      }
    }
    return currentQueue.toString().trim();
  }

  public boolean userAlreadyLoggedIn(GoClientHandler clientHandler, String username) {
    for (GoClientHandler handler : handlers) {
      if (!handler.equals(clientHandler)) {
        try {
          if (handler.getUsername().equals(username)) {
            return true;
          }
        } catch (NullPointerException ignored) {
        }
      }
    }
    return false;
  }

  public void acceptConnections() throws IOException {
    while (!serverSocket.isClosed()) {
      try {
        Socket socket = serverSocket.accept();
        handleConnection(socket);
      } catch (SocketException ignored) {
      }
    }
  }

  public void handleConnection(Socket socket) {
    try {
      GoClientHandler clientHandler = new GoClientHandler(socket, this);
      handlers.add(clientHandler);
      Thread thread = new Thread(clientHandler);
      thread.start();
    } catch (IOException e) {
      System.out.println("Something went wrong initializing the connection.");
    }
  }

  public void startServer() {
    while (runServer) {
      try {
        acceptConnections();
      } catch (IOException e) {
        System.out.println("Something went wrong, closing the server");
        runServer = false;
        closeServer();
      }
    }
  }

  public void broadCastMessage(GoClientHandler clientHandler, String inputLine) {
    System.out.println(inputLine);
    for (GoClientHandler handler : handlers) {
      handler.handleOutput(inputLine);
      }
    }

  public void handleDisconnect(GoClientHandler clientHandler) {
    handlers.remove(clientHandler);
    String disconnectMessage = clientHandler.getUsername() != null ?
        clientHandler.getUsername() : "<Unknown user>";
    disconnectMessage += " has disconnected from the server.";
    broadCastMessage(clientHandler, disconnectMessage);
  }

  public synchronized void closeServer() {
    try {
      if (!serverSocket.isClosed()) {
        serverSocket.close();
      }
    } catch (IOException ignored) {
    }
  }

  public static void main(String[] args) {
    boolean validPort = false;
    int port = -1;
    Scanner input = new Scanner(System.in);
    System.out.println("Enter a port to start listening for connections:");
    while (!validPort) {
      if (input.hasNextLine()) {
        try {
          port = Integer.parseInt(input.nextLine());
          if (port < 0 || port > 65535) {
            System.out.println("Valid port numbers range between 0 and 65535.");
            continue;
          }
          validPort = true;
        } catch (NumberFormatException e) {
          System.out.println("Not a valid port.");
        }
      }
    }
    try {
      GoServer server = new GoServer(port);
      System.out.println("Starting server on port " + server.getServerPort());
      server.startServer();
    } catch (IOException e) {
      System.out.println("Unable to start server!");
    }
  }

}
