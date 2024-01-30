package com.nedap.go.gamelogic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class StoneChain {

  private final HashSet<Integer> stones;
  private final HashSet<Integer> adjacentStones;

  public StoneChain() {
    stones = new HashSet<>();
    adjacentStones = new HashSet<>();
  }

  public void addStone(int position) {
    stones.add(position);
  }

  public void addAdjacent(int position) {
    adjacentStones.add(position);
  }

  public boolean containsStone(int position) {
    return stones.contains(position);
  }

  public HashSet<Integer> getStoneChain() {
    return stones;
  }

  public HashSet<Integer> getAdjacentStones() {
    return adjacentStones;
  }
}
