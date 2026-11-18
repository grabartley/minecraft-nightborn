package com.grahambartley.nightborn.config;

import java.util.List;

public record HouseSettings(
    int fledglingMembers, int adultMembers, int elderMembers, int ancientMembers) {

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

  public static final List<Setting<?>> SETTINGS =
      List.of(FLEDGLING_MEMBERS, ADULT_MEMBERS, ELDER_MEMBERS, ANCIENT_MEMBERS);

  public HouseSettings {
    fledglingMembers = FLEDGLING_MEMBERS.clamp(fledglingMembers);
    adultMembers = ADULT_MEMBERS.clamp(adultMembers);
    elderMembers = ELDER_MEMBERS.clamp(elderMembers);
    ancientMembers = ANCIENT_MEMBERS.clamp(ancientMembers);
  }

  static HouseSettings from(final SettingValues values) {
    return new HouseSettings(
        values.get(FLEDGLING_MEMBERS),
        values.get(ADULT_MEMBERS),
        values.get(ELDER_MEMBERS),
        values.get(ANCIENT_MEMBERS));
  }

  public int members(final int ageRank) {
    final List<Integer> byAge =
        List.of(fledglingMembers, adultMembers, elderMembers, ancientMembers);
    return byAge.get(Math.clamp(ageRank, 0, byAge.size() - 1));
  }
}
