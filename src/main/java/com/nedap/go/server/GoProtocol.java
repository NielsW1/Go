package com.nedap.go.server;

public class GoProtocol {
  public static final String SEPARATOR = "~";
  public static final String LOGIN = "LOGIN";
  public static final String QUEUE = "QUEUE";
  public static final String MOVE = "MOVE";
  public static final String ERROR = "ERROR";
  public static final String HELLO = "HELLO";
  public static final String ACCEPTED = "ACCEPTED";
  public static final String REJECTED = "REJECTED";
  public static final String QUEUED = "QUEUED";
  public static final String RESIGN = "RESIGN";
  public static final String MAKE_MOVE = "MAKE MOVE";
  public static final String GAME_STARTED = "GAME STARTED";
  public static final String GAME_OVER = "GAME OVER";
  public static final String DISCONNECTED = "DISCONNECTED";
  public static final String PASS = "PASS";


  private GoProtocol() {
  }
}
