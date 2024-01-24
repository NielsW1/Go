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

  public String toString() {
    if (this == EMPTY) {
      return "•";
    } else if (this == WHITE) {
      return "●";
    } else {
      return "○";
    }
  }
}
