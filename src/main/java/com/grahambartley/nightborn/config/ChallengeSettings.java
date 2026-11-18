package com.grahambartley.nightborn.config;

import java.util.List;

public record ChallengeSettings(int trialsOnOffer, int trialDays) {

  static final String SECTION = "challenges";

  public static final IntSetting TRIALS_ON_OFFER =
      new IntSetting(SECTION, "trialsOnOffer", 3, 0, 10, c -> c.challenges().trialsOnOffer());
  public static final IntSetting TRIAL_DAYS =
      new IntSetting(SECTION, "trialDays", 7, 1, 100, c -> c.challenges().trialDays());

  public static final List<Setting<?>> SETTINGS = List.of(TRIALS_ON_OFFER, TRIAL_DAYS);

  public ChallengeSettings {
    trialsOnOffer = TRIALS_ON_OFFER.clamp(trialsOnOffer);
    trialDays = TRIAL_DAYS.clamp(trialDays);
  }

  static ChallengeSettings from(final SettingValues values) {
    return new ChallengeSettings(values.get(TRIALS_ON_OFFER), values.get(TRIAL_DAYS));
  }
}
