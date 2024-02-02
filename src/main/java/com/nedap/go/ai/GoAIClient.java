package com.nedap.go.ai;

import com.nedap.go.client.GoClient;
import com.nedap.go.client.GoClientTUI;
import com.nedap.go.gamelogic.Stone;
import java.io.IOException;
import java.net.InetAddress;

public class GoAIClient extends GoClient {

  private GoNaivePlayer aiPlayer;

  public GoAIClient(InetAddress address, int port, GoClientTUI client)
      throws IOException {
    super(address, port, client);
  }

  @Override
  public void handleTurn() {
    super.handleTurn();
    aiPlayer.makeMove();
  }

  @Override
  public void handleGameStart(String name1, String name2) {
    aiPlayer = new GoNaivePlayer(this);
    setGUI();
    super.handleGameStart(name1, name2);
  }

}
