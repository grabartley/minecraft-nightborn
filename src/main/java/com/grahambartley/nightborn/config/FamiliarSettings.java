package com.grahambartley.nightborn.config;

import java.util.List;

public record FamiliarSettings(
    int startLoyalty,
    int wageDaysPerEmerald,
    int wageDaysAhead,
    int paidDayLoyalty,
    int unpaidDayLoyalty,
    int bittenLoyalty,
    int hurtLoyalty,
    int betrayLoyalty,
    int betrayChance,
    int guardRange,
    int errandRange) {

  static final String SECTION = "familiars";

  public static final IntSetting START_LOYALTY =
      new IntSetting(SECTION, "startLoyalty", 60, 0, 100, c -> c.familiars().startLoyalty());
  public static final IntSetting WAGE_DAYS_PER_EMERALD =
      new IntSetting(
          SECTION, "wageDaysPerEmerald", 1, 1, 30, c -> c.familiars().wageDaysPerEmerald());
  public static final IntSetting WAGE_DAYS_AHEAD =
      new IntSetting(SECTION, "wageDaysAhead", 7, 1, 120, c -> c.familiars().wageDaysAhead());
  public static final IntSetting PAID_DAY_LOYALTY =
      new IntSetting(SECTION, "paidDayLoyalty", 5, 0, 100, c -> c.familiars().paidDayLoyalty());
  public static final IntSetting UNPAID_DAY_LOYALTY =
      new IntSetting(
          SECTION, "unpaidDayLoyalty", 15, 0, 100, c -> c.familiars().unpaidDayLoyalty());
  public static final IntSetting BITTEN_LOYALTY =
      new IntSetting(SECTION, "bittenLoyalty", 25, 0, 100, c -> c.familiars().bittenLoyalty());
  public static final IntSetting HURT_LOYALTY =
      new IntSetting(SECTION, "hurtLoyalty", 10, 0, 100, c -> c.familiars().hurtLoyalty());
  public static final IntSetting BETRAY_LOYALTY =
      new IntSetting(SECTION, "betrayLoyalty", 25, 0, 100, c -> c.familiars().betrayLoyalty());
  public static final IntSetting BETRAY_CHANCE =
      new IntSetting(SECTION, "betrayChance", 30, 0, 100, c -> c.familiars().betrayChance());
  public static final IntSetting GUARD_RANGE =
      new IntSetting(SECTION, "guardRange", 8, 1, 32, c -> c.familiars().guardRange());
  public static final IntSetting ERRAND_RANGE =
      new IntSetting(SECTION, "errandRange", 32, 4, 128, c -> c.familiars().errandRange());

  public static final List<Setting<?>> SETTINGS =
      List.of(
          START_LOYALTY,
          WAGE_DAYS_PER_EMERALD,
          WAGE_DAYS_AHEAD,
          PAID_DAY_LOYALTY,
          UNPAID_DAY_LOYALTY,
          BITTEN_LOYALTY,
          HURT_LOYALTY,
          BETRAY_LOYALTY,
          BETRAY_CHANCE,
          GUARD_RANGE,
          ERRAND_RANGE);

  public FamiliarSettings {
    startLoyalty = START_LOYALTY.clamp(startLoyalty);
    wageDaysPerEmerald = WAGE_DAYS_PER_EMERALD.clamp(wageDaysPerEmerald);
    wageDaysAhead = WAGE_DAYS_AHEAD.clamp(wageDaysAhead);
    paidDayLoyalty = PAID_DAY_LOYALTY.clamp(paidDayLoyalty);
    unpaidDayLoyalty = UNPAID_DAY_LOYALTY.clamp(unpaidDayLoyalty);
    bittenLoyalty = BITTEN_LOYALTY.clamp(bittenLoyalty);
    hurtLoyalty = HURT_LOYALTY.clamp(hurtLoyalty);
    betrayLoyalty = BETRAY_LOYALTY.clamp(betrayLoyalty);
    betrayChance = BETRAY_CHANCE.clamp(betrayChance);
    guardRange = GUARD_RANGE.clamp(guardRange);
    errandRange = ERRAND_RANGE.clamp(errandRange);
  }

  static FamiliarSettings from(final SettingValues values) {
    return new FamiliarSettings(
        values.get(START_LOYALTY),
        values.get(WAGE_DAYS_PER_EMERALD),
        values.get(WAGE_DAYS_AHEAD),
        values.get(PAID_DAY_LOYALTY),
        values.get(UNPAID_DAY_LOYALTY),
        values.get(BITTEN_LOYALTY),
        values.get(HURT_LOYALTY),
        values.get(BETRAY_LOYALTY),
        values.get(BETRAY_CHANCE),
        values.get(GUARD_RANGE),
        values.get(ERRAND_RANGE));
  }
}
