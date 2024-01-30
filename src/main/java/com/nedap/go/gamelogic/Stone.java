package com.nedap.go.gamelogic;

public enum Stone {
  EMPTY, BLACK, WHITE;

  public Stone other() {
    if (this == BLACK) {
      return WHITE;
    } else if (this == WHITE) {
      return BLACK;
    } else {
      return EMPTY;
    }
  }

  public String getColor() {
    if (this == BLACK) {
      return "BLACK";
    } else if (this == WHITE) {
      return "WHITE";
    } else {
      return null;
    }
  }

  public String toString() {
    if (this == BLACK) {
      return "○";
    } else if (this == WHITE) {
      return "●";
    } else {
      return "•";
    }
  }
}
