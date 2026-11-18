package com.grahambartley.nightborn.config;

import java.util.List;

public record PartySettings(
    int partyMinutes,
    int partyChance,
    int partyMinSize,
    int partyMaxSize,
    int partyExtraNightChance,
    int partyMaxNights,
    int partyLeaveMinutes,
    int partySleepers,
    int campBatChance,
    int campBatCount) {

  static final String SECTION = "parties";

  public static final IntSetting PARTY_MINUTES =
      new IntSetting(SECTION, "partyMinutes", 10, 1, 120, c -> c.parties().partyMinutes());
  public static final IntSetting PARTY_CHANCE =
      new IntSetting(SECTION, "partyChance", 20, 0, 100, c -> c.parties().partyChance());
  public static final IntSetting PARTY_MIN_SIZE =
      new IntSetting(SECTION, "partyMinSize", 4, 1, 12, c -> c.parties().partyMinSize());
  public static final IntSetting PARTY_MAX_SIZE =
      new IntSetting(SECTION, "partyMaxSize", 8, 1, 12, c -> c.parties().partyMaxSize());
  public static final IntSetting PARTY_EXTRA_NIGHT_CHANCE =
      new IntSetting(
          SECTION, "partyExtraNightChance", 25, 0, 100, c -> c.parties().partyExtraNightChance());
  public static final IntSetting PARTY_MAX_NIGHTS =
      new IntSetting(SECTION, "partyMaxNights", 3, 1, 3, c -> c.parties().partyMaxNights());
  public static final IntSetting PARTY_LEAVE_MINUTES =
      new IntSetting(SECTION, "partyLeaveMinutes", 5, 1, 60, c -> c.parties().partyLeaveMinutes());
  public static final IntSetting PARTY_SLEEPERS =
      new IntSetting(SECTION, "partySleepers", 50, 0, 100, c -> c.parties().partySleepers());
  public static final IntSetting CAMP_BAT_CHANCE =
      new IntSetting(SECTION, "campBatChance", 15, 0, 100, c -> c.parties().campBatChance());
  public static final IntSetting CAMP_BAT_COUNT =
      new IntSetting(SECTION, "campBatCount", 5, 1, 12, c -> c.parties().campBatCount());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          PARTY_MINUTES,
          PARTY_CHANCE,
          PARTY_MIN_SIZE,
          PARTY_MAX_SIZE,
          PARTY_EXTRA_NIGHT_CHANCE,
          PARTY_MAX_NIGHTS,
          PARTY_LEAVE_MINUTES,
          PARTY_SLEEPERS,
          CAMP_BAT_CHANCE,
          CAMP_BAT_COUNT);

  public PartySettings {
    partyMinutes = PARTY_MINUTES.clamp(partyMinutes);
    partyChance = PARTY_CHANCE.clamp(partyChance);
    partyMinSize = PARTY_MIN_SIZE.clamp(partyMinSize);
    partyMaxSize = Math.max(partyMinSize, PARTY_MAX_SIZE.clamp(partyMaxSize));
    partyExtraNightChance = PARTY_EXTRA_NIGHT_CHANCE.clamp(partyExtraNightChance);
    partyMaxNights = PARTY_MAX_NIGHTS.clamp(partyMaxNights);
    partyLeaveMinutes = PARTY_LEAVE_MINUTES.clamp(partyLeaveMinutes);
    partySleepers = PARTY_SLEEPERS.clamp(partySleepers);
    campBatChance = CAMP_BAT_CHANCE.clamp(campBatChance);
    campBatCount = CAMP_BAT_COUNT.clamp(campBatCount);
  }

  static PartySettings from(final SettingValues values) {
    return new PartySettings(
        values.get(PARTY_MINUTES),
        values.get(PARTY_CHANCE),
        values.get(PARTY_MIN_SIZE),
        values.get(PARTY_MAX_SIZE),
        values.get(PARTY_EXTRA_NIGHT_CHANCE),
        values.get(PARTY_MAX_NIGHTS),
        values.get(PARTY_LEAVE_MINUTES),
        values.get(PARTY_SLEEPERS),
        values.get(CAMP_BAT_CHANCE),
        values.get(CAMP_BAT_COUNT));
  }
}
