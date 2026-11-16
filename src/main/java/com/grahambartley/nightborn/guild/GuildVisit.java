package com.grahambartley.nightborn.guild;

public enum GuildVisit {
  REFUSE,
  CLAIM,
  TRADE;

  public static GuildVisit of(final boolean vampire, final boolean holdsFinishedContract) {
    if (vampire) {
      return REFUSE;
    }
    return holdsFinishedContract ? CLAIM : TRADE;
  }
}
