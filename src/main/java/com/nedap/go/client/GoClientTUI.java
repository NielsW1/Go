package com.nedap.go.client;

import com.nedap.go.gamelogic.GoPlayer;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class GoClientTUI {

  private GoClient client;
  private Scanner input;

  public GoClientTUI() {
    input = new Scanner(System.in);
    initializeConnection();
  }

  public void initializeConnection() {
    System.out.println("Enter host address and port of the server you want to connect to:");
    while (client == null) {
      if (input.hasNextLine()) {
        try {
          String[] parsedInput = input.nextLine().split("[^A-Za-z0-9.]+");
          InetAddress hostAddress = InetAddress.getByName(parsedInput[0]);
          int port = Integer.parseInt(parsedInput[1]);

          if (port < 0 || port > 65535) {
            System.out.println("Valid port numbers range between 0 and 65535.");
            continue;
          }

          client = new GoClient(hostAddress, port);
          client.client = this;
          System.out.println("Connection established with " + hostAddress + " on port " + port);
          runClient();

        } catch (IndexOutOfBoundsException e) {
          System.out.println("Invalid number of arguments.");

        } catch (NumberFormatException e) {
          System.out.println("Invalid port.");

        } catch (IOException e) {
          System.out.println("Unable to connect to server.");
        }
      }
    }
  }

  public void runClient() {
    boolean run = true;
    System.out.println("Use <EXIT> or <QUIT> to exit the client.");
    while (run) {
      String inputLine;
      if (input.hasNextLine()) {
        inputLine = input.nextLine();
        switch (inputLine.toUpperCase()) {
          case "EXIT":
          case "QUIT":
            System.out.println("Closing client....");
            client.closeConnection();
            run = false;
            break;
          case "HELP":
            help();
            break;
          default:
            client.handleOutput(inputLine);
        }
      }
    }
  }

  public void receiveInput(String inputLine) {
    System.out.println(inputLine);
  }

  public void help() {
    System.out.println("""
        Valid commands:
                
        LOGIN~<username> .............. Login to the server with your username.
        QUEUE ......................... Join the queue to wait for a new game to start. Use the command again to exit the queue.
        MOVE~<number> ................. Make a move at this specific coordinate.
        MOVE~<col, row> .............. Make a move at position row, col.
        PASS .......................... Pass your current move.""");
  }

  public static void main(String[] args) {
    new GoClientTUI();
  }
}
