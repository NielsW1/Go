package com.nedap.go.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GoServerTest {

  private Socket client1;
  private Socket client2;

  @BeforeEach
  void setUp() throws IOException {
    client1 = new Socket(InetAddress.getLocalHost(), 8080);
    client2 = new Socket(InetAddress.getLocalHost(), 8080);
  }

  @Test
  void testServer() throws IOException {

    try (BufferedReader br1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
        PrintWriter pw1 = new PrintWriter(new OutputStreamWriter(client1.getOutputStream()), true);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));
        PrintWriter pw2 = new PrintWriter(new OutputStreamWriter(client2.getOutputStream()),
            true)) {

      // test receive HELLO message
      assertEquals("HELLO~Welcome to Niels' GO server! use LOGIN~<username> to log in.",
          br1.readLine());
      assertEquals("HELLO~Welcome to Niels' GO server! use LOGIN~<username> to log in.",
          br2.readLine());

      // test logins
      pw1.println("LOGIN~Henk");
      assertEquals("ACCEPTED~Henk", br1.readLine());
      pw1.println("LOGIN~Piet");
      assertEquals("REJECTED~Invalid username!", br1.readLine());
      pw2.println("LOGIN~   ");
      assertEquals("REJECTED~Invalid username!", br2.readLine());
      pw2.println("LOGIN~Henk");
      assertEquals("REJECTED~User by that name already logged in!", br2.readLine());
      pw2.println("LOGIN~Piet");
      assertEquals("ACCEPTED~Piet", br2.readLine());

      // test making a move while not in a game
      pw1.println("MOVE~0");
      assertEquals("ERROR~You are not in a game!", br1.readLine());

      // test queueing and automatic game start
      pw1.println("QUEUE");
      assertEquals("QUEUED", br1.readLine());
      pw2.println("QUEUE");
      assertEquals("QUEUED", br2.readLine());
      assertEquals("GAME STARTED~Henk,Piet~9", br1.readLine());
      assertEquals("GAME STARTED~Henk,Piet~9", br2.readLine());
      assertEquals("MAKE MOVE", br1.readLine());

      // test move timeout and automatic resign
      Thread.sleep(61000);
      assertEquals("ERROR~Henk, move timed out!", br1.readLine());
      assertEquals("ERROR~Henk, move timed out!", br2.readLine());
      assertEquals("GAME OVER~Winner~Piet", br1.readLine());
      assertEquals("GAME OVER~Winner~Piet", br2.readLine());

      // test re-queueing
      pw1.println("QUEUE");
      assertEquals("QUEUED", br1.readLine());
      pw2.println("QUEUE");
      assertEquals("QUEUED", br2.readLine());
      assertEquals("GAME STARTED~Henk,Piet~9", br1.readLine());
      assertEquals("GAME STARTED~Henk,Piet~9", br2.readLine());
      assertEquals("MAKE MOVE", br1.readLine());

      // test correctly handling invalid moves and playing while it's not your turn
      pw1.println("MOVE~100");
      assertEquals("ERROR~Invalid move! Try again.", br1.readLine());
      pw2.println("MOVE~80");
      assertEquals("ERROR~It's not your turn!", br2.readLine());

      // test playing valid moves
      pw1.println("MOVE~1");
      assertEquals("MOVE~1~BLACK", br1.readLine());
      assertEquals("MOVE~1~BLACK", br2.readLine());
      assertEquals("MAKE MOVE", br2.readLine());
      pw2.println("MOVE~0");
      assertEquals("MOVE~0~WHITE", br1.readLine());
      assertEquals("MOVE~0~WHITE", br2.readLine());
      assertEquals("MAKE MOVE", br1.readLine());
      pw1.println("MOVE~0,1");
      assertEquals("MOVE~9~BLACK", br1.readLine());
      assertEquals("MOVE~9~BLACK", br2.readLine());
      assertEquals("MAKE MOVE", br2.readLine());

      // test passing and ending the game
      pw2.println("PASS");
      assertEquals("PASS~WHITE", br1.readLine());
      assertEquals("PASS~WHITE", br2.readLine());
      assertEquals("MAKE MOVE", br1.readLine());
      pw1.println("PASS");
      assertEquals("PASS~BLACK", br1.readLine());
      assertEquals("PASS~BLACK", br2.readLine());
      assertEquals("GAME OVER~Winner~Henk", br1.readLine());
      assertEquals("GAME OVER~Winner~Henk", br2.readLine());

    } catch (InterruptedException ignored) {
    } finally {
      client1.close();
      client2.close();
    }
  }

  @Test
  public void testDisconnectDuringGame() throws IOException {

    try (BufferedReader br1 = new BufferedReader(new InputStreamReader(client1.getInputStream()));
        PrintWriter pw1 = new PrintWriter(new OutputStreamWriter(client1.getOutputStream()), true);
        BufferedReader br2 = new BufferedReader(new InputStreamReader(client2.getInputStream()));
        PrintWriter pw2 = new PrintWriter(new OutputStreamWriter(client2.getOutputStream()),
            true)) {

      // test receive HELLO message
      assertEquals("HELLO~Welcome to Niels' GO server! use LOGIN~<username> to log in.",
          br1.readLine());
      assertEquals("HELLO~Welcome to Niels' GO server! use LOGIN~<username> to log in.",
          br2.readLine());

      pw1.println("LOGIN~Henk");
      assertEquals("ACCEPTED~Henk", br1.readLine());
      pw2.println("LOGIN~Piet");
      assertEquals("ACCEPTED~Piet", br2.readLine());

      pw1.println("QUEUE");
      assertEquals("QUEUED", br1.readLine());
      pw2.println("QUEUE");
      assertEquals("QUEUED", br2.readLine());
      assertEquals("GAME STARTED~Henk,Piet~9", br1.readLine());
      assertEquals("GAME STARTED~Henk,Piet~9", br2.readLine());
      assertEquals("MAKE MOVE", br1.readLine());

      client1.close();

      assertEquals("GAME OVER~Winner~Piet", br2.readLine());

    } finally {
      client2.close();
    }
  }
}
