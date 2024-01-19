package com.nedap.go.gamelogic;

import com.nedap.go.server.GoPlayer;

public class GoGameTUI {
    public static void main(String[] args) {
      GoGame game = new GoGame(9, new GoPlayer("Henk"), new GoPlayer("Piet"));

    }
}
