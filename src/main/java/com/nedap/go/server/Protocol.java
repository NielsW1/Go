package com.nedap.go.server;

public class Protocol {
  public static final String SEPARATOR = "~";
  public static final String LOGIN = "LOGIN";
  public static final String QUEUE = "QUEUE";
  public static final String QUEUED = "QUEUED";
  public static final String MOVE = "MOVE";
  public static final String ERROR = "ERROR";
  public static final String HELLO = "HELLO";
  public static final String ACCEPTED = "ACCEPTED";
  public static final String REFUSED = "REFUSED";


  private Protocol() {

  }

  public static String hello() {
    return HELLO + SEPARATOR;
  }

  public static String login() {
    return LOGIN + SEPARATOR;
  }

  public static String queue() {
    return QUEUED + SEPARATOR;
  }

  public static String error() {
    return ERROR + SEPARATOR;
  }
}
