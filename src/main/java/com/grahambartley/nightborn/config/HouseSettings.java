package com.grahambartley.nightborn.config;

import java.util.List;

public record HouseSettings(
    int fledglingMembers,
    int adultMembers,
    int elderMembers,
    int ancientMembers,
    int answerNights,
    int repeatNights,
    int duelReach,
    int duelRadius) {

  static final String SECTION = "households";
  static final int MOST_MEMBERS = 32;

  public static final IntSetting FLEDGLING_MEMBERS =
      new IntSetting(
          SECTION, "fledglingMembers", 0, 0, MOST_MEMBERS, c -> c.houses().fledglingMembers());
  public static final IntSetting ADULT_MEMBERS =
      new IntSetting(SECTION, "adultMembers", 2, 0, MOST_MEMBERS, c -> c.houses().adultMembers());
  public static final IntSetting ELDER_MEMBERS =
      new IntSetting(SECTION, "elderMembers", 4, 0, MOST_MEMBERS, c -> c.houses().elderMembers());
  public static final IntSetting ANCIENT_MEMBERS =
      new IntSetting(
          SECTION, "ancientMembers", 6, 0, MOST_MEMBERS, c -> c.houses().ancientMembers());
  public static final IntSetting ANSWER_NIGHTS =
      new IntSetting(SECTION, "answerNights", 3, 1, 30, c -> c.houses().answerNights());
  public static final IntSetting REPEAT_NIGHTS =
      new IntSetting(SECTION, "repeatNights", 7, 0, 60, c -> c.houses().repeatNights());
  public static final IntSetting DUEL_REACH =
      new IntSetting(SECTION, "duelReach", 6, 2, 32, c -> c.houses().duelReach());
  public static final IntSetting DUEL_RADIUS =
      new IntSetting(SECTION, "duelRadius", 8, 4, 32, c -> c.houses().duelRadius());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          FLEDGLING_MEMBERS,
          ADULT_MEMBERS,
          ELDER_MEMBERS,
          ANCIENT_MEMBERS,
          ANSWER_NIGHTS,
          REPEAT_NIGHTS,
          DUEL_REACH,
          DUEL_RADIUS);

  public HouseSettings {
    fledglingMembers = FLEDGLING_MEMBERS.clamp(fledglingMembers);
    adultMembers = ADULT_MEMBERS.clamp(adultMembers);
    elderMembers = ELDER_MEMBERS.clamp(elderMembers);
    ancientMembers = ANCIENT_MEMBERS.clamp(ancientMembers);
    answerNights = ANSWER_NIGHTS.clamp(answerNights);
    repeatNights = REPEAT_NIGHTS.clamp(repeatNights);
    duelReach = DUEL_REACH.clamp(duelReach);
    duelRadius = DUEL_RADIUS.clamp(duelRadius);
  }

  static HouseSettings from(final SettingValues values) {
    return new HouseSettings(
        values.get(FLEDGLING_MEMBERS),
        values.get(ADULT_MEMBERS),
        values.get(ELDER_MEMBERS),
        values.get(ANCIENT_MEMBERS),
        values.get(ANSWER_NIGHTS),
        values.get(REPEAT_NIGHTS),
        values.get(DUEL_REACH),
        values.get(DUEL_RADIUS));
  }

  public int members(final int ageRank) {
    final List<Integer> byAge =
        List.of(fledglingMembers, adultMembers, elderMembers, ancientMembers);
    return byAge.get(Math.clamp(ageRank, 0, byAge.size() - 1));
  }
}
