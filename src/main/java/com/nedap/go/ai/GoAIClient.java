package com.nedap.go.ai;

import com.nedap.go.client.GoClient;
import com.nedap.go.client.GoClientTUI;
import java.io.IOException;
import java.net.InetAddress;

public class GoAIClient extends GoClient {

  private GoNaivePlayer aiPlayer;
  private final GoClientTUI clientTUI;

  public GoAIClient(InetAddress address, int port, GoClientTUI client)
      throws IOException {
    super(address, port, client);
    clientTUI = client;
  }

  public GoClientTUI getClientTUI() {
    return clientTUI;
  }

  @Override
  public void handleTurn() {
    super.handleTurn();
    aiPlayer.makeMove();
  }

  @Override
  public void handleGameStart() {
    aiPlayer = new GoNaivePlayer(this);
    setGUI();
    super.handleGameStart();
  }

}
