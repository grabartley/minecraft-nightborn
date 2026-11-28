package com.grahambartley.nightborn.balance;

public enum Side {
  VAMPIRES,
  HUNTERS;

  public Side other() {
    return this == VAMPIRES ? HUNTERS : VAMPIRES;
  }
}
