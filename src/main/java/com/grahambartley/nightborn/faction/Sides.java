package com.grahambartley.nightborn.faction;

public final class Sides {

  private Sides() {}

  public static boolean sameSide(
      final boolean hunter,
      final boolean vampire,
      final boolean otherHunter,
      final boolean otherVampire) {
    return hunter && otherHunter || vampire && otherVampire;
  }

  public static boolean opposed(
      final boolean hunter,
      final boolean vampire,
      final boolean otherHunter,
      final boolean otherVampire) {
    return hunter && otherVampire || vampire && otherHunter;
  }

  public static boolean allied(
      final boolean self,
      final boolean sameSide,
      final boolean teammate,
      final boolean fightingEachOther) {
    return !self && (sameSide || teammate) && !fightingEachOther;
  }

  public static boolean spared(final boolean boss, final boolean allied) {
    return !boss && allied;
  }

  public static boolean foe(final boolean opposed, final boolean fightsUs, final boolean fought) {
    return opposed || fightsUs || fought;
  }
}
