package com.nedap.go.client;

import com.nedap.go.ai.GoAIClient;
import com.nedap.go.gamelogic.IllegalMoveException;
import com.nedap.go.gamelogic.NotYourTurnException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class GoClientTUI {

  private GoClient client;
  private final Scanner input;
  private boolean run = true;

  public GoClientTUI() {
    input = new Scanner(System.in);
    initializeConnection();
  }

  public void initializeConnection() {
    System.out.println("Enter host address and port of the server you want to connect to:");
    while (client == null && run) {
      if (input.hasNextLine()) {
        String command = input.nextLine();

        if (command.equalsIgnoreCase("EXIT") || command.equalsIgnoreCase("QUIT")) {
          run = false;
          System.out.println("Closing client....");

        } else {
          try {
            String[] parsedInput = command.split("[^A-Za-z0-9.]+");
            InetAddress hostAddress = InetAddress.getByName(parsedInput[0]);
            int port = Integer.parseInt(parsedInput[1]);

            if (port < 0 || port > 65535) {
              System.out.println("Valid port numbers range between 0 and 65535.");
              continue;
            }
            System.out.println("Human or AI player?");
            if (input.hasNextLine()) {
              command = input.nextLine();
              if (command.equalsIgnoreCase("AI")) {
                client = new GoAIClient(hostAddress, port, this);
              } else {
                client = new GoClient(hostAddress, port, this);

              }
              System.out.println("Connection established with " + hostAddress + " on port " + port);
              runClient();
            }

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
  }

  public void runClient() {
    System.out.println("Use <EXIT> or <QUIT> to exit the client.");
    while (run) {
      String inputLine;
      if (input.hasNextLine()) {
        inputLine = input.nextLine();
        switch (inputLine.toUpperCase()) {

          case "GUI":
            client.setGUI();
            System.out.println("GUI has been enabled.");
            break;

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
            try {
              client.handleOutput(inputLine);
            } catch (IllegalMoveException | NotYourTurnException e) {
              System.out.println(e.getMessage());
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
              System.out.println("Invalid command!");
            }
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
        QUEUE ......................... Join the queue to wait for a new game to start.\s
        Use the command again to exit the queue.
        MOVE~<number> ................. Make a move at this specific coordinate.
        MOVE~<col, row> ............... Make a move at position col, row.
        PASS .......................... Pass your current move.
        RESIGN ........................ Forfeit the game, the opponent will automatically win
        EXIT, QUIT .................... Close the client, disconnecting you from the server.
        GUI ........................... Creates a GUI that displays the board state""");
  }

  public static void main(String[] args) {
    new GoClientTUI();
  }
}
