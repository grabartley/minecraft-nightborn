package com.grahambartley.nightborn.config;

import java.util.List;

public record VillageSettings(
    int waryAt,
    int wardedAt,
    int huntedAt,
    int quietNightFade,
    int charmFade,
    int giftFade,
    int giftsPerNight,
    int reportSeconds,
    int nightMarkup,
    int villageReach,
    int nemesisVisitFewest,
    int nemesisVisitMost,
    int nemesisLimit) {

  static final String SECTION = "villages";

  public static final IntSetting WARY_AT =
      new IntSetting(SECTION, "waryAt", 25, 1, 100, c -> c.villages().waryAt());
  public static final IntSetting WARDED_AT =
      new IntSetting(SECTION, "wardedAt", 50, 1, 100, c -> c.villages().wardedAt());
  public static final IntSetting HUNTED_AT =
      new IntSetting(SECTION, "huntedAt", 75, 1, 100, c -> c.villages().huntedAt());
  public static final IntSetting QUIET_NIGHT_FADE =
      new IntSetting(SECTION, "quietNightFade", 2, 0, 100, c -> c.villages().quietNightFade());
  public static final IntSetting CHARM_FADE =
      new IntSetting(SECTION, "charmFade", 5, 0, 100, c -> c.villages().charmFade());
  public static final IntSetting GIFT_FADE =
      new IntSetting(SECTION, "giftFade", 2, 0, 100, c -> c.villages().giftFade());
  public static final IntSetting GIFTS_PER_NIGHT =
      new IntSetting(SECTION, "giftsPerNight", 3, 0, 20, c -> c.villages().giftsPerNight());
  public static final IntSetting REPORT_SECONDS =
      new IntSetting(SECTION, "reportSeconds", 10, 1, 60, c -> c.villages().reportSeconds());
  public static final IntSetting NIGHT_MARKUP =
      new IntSetting(SECTION, "nightMarkup", 25, 0, 200, c -> c.villages().nightMarkup());
  public static final IntSetting VILLAGE_REACH =
      new IntSetting(SECTION, "villageReach", 48, 16, 128, c -> c.villages().villageReach());
  public static final IntSetting NEMESIS_VISIT_FEWEST =
      new IntSetting(
          SECTION, "nemesisVisitFewest", 5, 1, 100, c -> c.villages().nemesisVisitFewest());
  public static final IntSetting NEMESIS_VISIT_MOST =
      new IntSetting(SECTION, "nemesisVisitMost", 10, 1, 100, c -> c.villages().nemesisVisitMost());
  public static final IntSetting NEMESIS_LIMIT =
      new IntSetting(SECTION, "nemesisLimit", 3, 0, 10, c -> c.villages().nemesisLimit());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          WARY_AT,
          WARDED_AT,
          HUNTED_AT,
          QUIET_NIGHT_FADE,
          CHARM_FADE,
          GIFT_FADE,
          GIFTS_PER_NIGHT,
          REPORT_SECONDS,
          NIGHT_MARKUP,
          VILLAGE_REACH,
          NEMESIS_VISIT_FEWEST,
          NEMESIS_VISIT_MOST,
          NEMESIS_LIMIT);

  public VillageSettings {
    waryAt = WARY_AT.clamp(waryAt);
    wardedAt = Math.max(waryAt, WARDED_AT.clamp(wardedAt));
    huntedAt = Math.max(wardedAt, HUNTED_AT.clamp(huntedAt));
    quietNightFade = QUIET_NIGHT_FADE.clamp(quietNightFade);
    charmFade = CHARM_FADE.clamp(charmFade);
    giftFade = GIFT_FADE.clamp(giftFade);
    giftsPerNight = GIFTS_PER_NIGHT.clamp(giftsPerNight);
    reportSeconds = REPORT_SECONDS.clamp(reportSeconds);
    nightMarkup = NIGHT_MARKUP.clamp(nightMarkup);
    villageReach = VILLAGE_REACH.clamp(villageReach);
    nemesisVisitFewest = NEMESIS_VISIT_FEWEST.clamp(nemesisVisitFewest);
    nemesisVisitMost = Math.max(nemesisVisitFewest, NEMESIS_VISIT_MOST.clamp(nemesisVisitMost));
    nemesisLimit = NEMESIS_LIMIT.clamp(nemesisLimit);
  }

  static VillageSettings from(final SettingValues values) {
    return new VillageSettings(
        values.get(WARY_AT),
        values.get(WARDED_AT),
        values.get(HUNTED_AT),
        values.get(QUIET_NIGHT_FADE),
        values.get(CHARM_FADE),
        values.get(GIFT_FADE),
        values.get(GIFTS_PER_NIGHT),
        values.get(REPORT_SECONDS),
        values.get(NIGHT_MARKUP),
        values.get(VILLAGE_REACH),
        values.get(NEMESIS_VISIT_FEWEST),
        values.get(NEMESIS_VISIT_MOST),
        values.get(NEMESIS_LIMIT));
  }
}
