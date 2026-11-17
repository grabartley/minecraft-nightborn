package com.grahambartley.nightborn.config;

import com.grahambartley.nightborn.age.AgeNeeds;
import java.util.List;

public record AgeSettings(
    int adultNights,
    int elderNights,
    int ancientNights,
    int adultBlood,
    int elderBlood,
    int ancientBlood,
    int stageHealth,
    int stageDamage,
    int stageBlood,
    int stageDrainSlower) {

  static final String SECTION = "age";

  public static final IntSetting ADULT_NIGHTS =
      new IntSetting(SECTION, "adultNights", 10, 0, 10000, c -> c.age().adultNights());
  public static final IntSetting ELDER_NIGHTS =
      new IntSetting(SECTION, "elderNights", 40, 0, 10000, c -> c.age().elderNights());
  public static final IntSetting ANCIENT_NIGHTS =
      new IntSetting(SECTION, "ancientNights", 100, 0, 10000, c -> c.age().ancientNights());
  public static final IntSetting ADULT_BLOOD =
      new IntSetting(SECTION, "adultBlood", 100, 0, 100000, c -> c.age().adultBlood());
  public static final IntSetting ELDER_BLOOD =
      new IntSetting(SECTION, "elderBlood", 500, 0, 100000, c -> c.age().elderBlood());
  public static final IntSetting ANCIENT_BLOOD =
      new IntSetting(SECTION, "ancientBlood", 1500, 0, 100000, c -> c.age().ancientBlood());
  public static final IntSetting STAGE_HEALTH =
      new IntSetting(SECTION, "stageHealth", 4, 0, 40, c -> c.age().stageHealth());
  public static final IntSetting STAGE_DAMAGE =
      new IntSetting(SECTION, "stageDamage", 1, 0, 20, c -> c.age().stageDamage());
  public static final IntSetting STAGE_BLOOD =
      new IntSetting(SECTION, "stageBlood", 2, 0, 10, c -> c.age().stageBlood());
  public static final IntSetting STAGE_DRAIN_SLOWER =
      new IntSetting(SECTION, "stageDrainSlower", 10, 0, 30, c -> c.age().stageDrainSlower());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          ADULT_NIGHTS,
          ELDER_NIGHTS,
          ANCIENT_NIGHTS,
          ADULT_BLOOD,
          ELDER_BLOOD,
          ANCIENT_BLOOD,
          STAGE_HEALTH,
          STAGE_DAMAGE,
          STAGE_BLOOD,
          STAGE_DRAIN_SLOWER);

  public AgeSettings {
    adultNights = ADULT_NIGHTS.clamp(adultNights);
    elderNights = ELDER_NIGHTS.clamp(elderNights);
    ancientNights = ANCIENT_NIGHTS.clamp(ancientNights);
    adultBlood = ADULT_BLOOD.clamp(adultBlood);
    elderBlood = ELDER_BLOOD.clamp(elderBlood);
    ancientBlood = ANCIENT_BLOOD.clamp(ancientBlood);
    stageHealth = STAGE_HEALTH.clamp(stageHealth);
    stageDamage = STAGE_DAMAGE.clamp(stageDamage);
    stageBlood = STAGE_BLOOD.clamp(stageBlood);
    stageDrainSlower = STAGE_DRAIN_SLOWER.clamp(stageDrainSlower);
  }

  public List<AgeNeeds> needs() {
    return List.of(
        new AgeNeeds(adultNights, adultBlood),
        new AgeNeeds(elderNights, elderBlood),
        new AgeNeeds(ancientNights, ancientBlood));
  }

  static AgeSettings from(final SettingValues values) {
    return new AgeSettings(
        values.get(ADULT_NIGHTS),
        values.get(ELDER_NIGHTS),
        values.get(ANCIENT_NIGHTS),
        values.get(ADULT_BLOOD),
        values.get(ELDER_BLOOD),
        values.get(ANCIENT_BLOOD),
        values.get(STAGE_HEALTH),
        values.get(STAGE_DAMAGE),
        values.get(STAGE_BLOOD),
        values.get(STAGE_DRAIN_SLOWER));
  }
}
