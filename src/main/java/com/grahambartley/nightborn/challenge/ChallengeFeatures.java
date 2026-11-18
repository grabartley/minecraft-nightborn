package com.grahambartley.nightborn.challenge;

public final class ChallengeFeatures {

  private ChallengeFeatures() {}

  public static void register() {
    ChallengeData.register();
    ChallengeAttachment.register();
    OfferKeeper.register();
    Titles.register();
    ChallengeHooks.register();
  }
}
