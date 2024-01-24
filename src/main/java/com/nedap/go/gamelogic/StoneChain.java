package com.nedap.go.gamelogic;

import java.util.ArrayList;
import java.util.List;

public class StoneChain {

  private List<Integer> stoneChain;
  private List<Integer> adjacentStones;

  public StoneChain() {
    stoneChain = new ArrayList<>();
    adjacentStones = new ArrayList<>();
  }

  public void addStone(int position) {
    stoneChain.add(position);
  }

  public void addAdjacent(int position) {
    adjacentStones.add(position);
  }

  public boolean containsStone(int position) {
    return stoneChain.contains(position);
  }

  public List<Integer> getStoneChain() {
    return stoneChain;
  }

  public List<Integer> getAdjacentStones() {
    return adjacentStones;
  }
}
