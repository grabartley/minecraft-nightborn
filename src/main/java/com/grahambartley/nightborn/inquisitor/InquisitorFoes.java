package com.grahambartley.nightborn.inquisitor;

public final class InquisitorFoes {

  private InquisitorFoes() {}

  public static boolean seeksOut(
      final boolean player, final boolean vampire, final boolean turnedOn, final boolean peaceful) {
    return player && !peaceful && (vampire || turnedOn);
  }

  public static boolean fights(
      final boolean player, final boolean vampire, final boolean turnedOn, final boolean struck) {
    return vampire || player && (turnedOn || struck);
  }

  public static boolean shrugsOff(
      final boolean untouchable, final boolean attackerOutside, final boolean shrugged) {
    return untouchable || attackerOutside || shrugged;
  }

  public static boolean turnsOn(final boolean ringerIsVampire, final boolean rang) {
    return rang && !ringerIsVampire;
  }
}
