package com.grahambartley.nightborn.rank;

public final class RankFeatures {

  private RankFeatures() {}

  public static void register() {
    RankData.register();
    Ranking.register();
    RankDeeds.register();
  }
}
