package com.nedap.go.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

public class ClientTUI {

  private GoPlayer player;
  private Scanner input;

  public ClientTUI() {
    input = new Scanner(System.in);
    initializeConnection();
  }

  public void initializeConnection(){

    System.out.println("Enter host address and port of the server you want to connect to:");
    while (player == null) {
      if (input.hasNextLine()) {
        try {
          String[] parsedInput = input.nextLine().split("[^A-Za-z0-9.]+");
          InetAddress hostAddress = InetAddress.getByName(parsedInput[0]);
          int port = Integer.parseInt(parsedInput[1]);

          if (port < 0 || port > 65535) {
            System.out.println("Valid port numbers range between 0 and 65535.");
            continue;
          }

          player = new GoPlayer(hostAddress, port, this);
          System.out.println("Connected to server address " + hostAddress + " on port " + port);
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
        if (inputLine.equalsIgnoreCase("exit") || inputLine.equalsIgnoreCase("quit")) {
          System.out.println("Closing client....");
          player.closeConnection();
          run = false;
        }
        player.handleOutput(inputLine);
      }
    }
  }

  public void receiveInput(String inputLine) {
    System.out.println(inputLine);
  }

  public static void main(String[] args) {
    new ClientTUI();
  }
}
