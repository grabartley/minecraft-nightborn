package com.grahambartley.nightborn.servant.job;

public enum ScoutSighting {
  HUNTER,
  VAMPIRE,
  VILLAGER,
  HUMAN,
  NONE;

  public static ScoutSighting of(
      final boolean hunter, final boolean vampire, final boolean villager, final boolean player) {
    if (hunter) {
      return HUNTER;
    }
    if (vampire) {
      return VAMPIRE;
    }
    if (villager) {
      return VILLAGER;
    }
    return player ? HUMAN : NONE;
  }
}
