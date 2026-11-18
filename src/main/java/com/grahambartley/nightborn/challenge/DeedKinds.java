package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.Nightborn;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.util.Identifier;

public final class DeedKinds {

  private static final Set<Identifier> SEEN_BY_GAME = new LinkedHashSet<>();

  public static final Identifier FED = live("fed");
  public static final Identifier FINISHER_KILL = live("finisher_kill");
  public static final Identifier CHALICE_FILLED = live("chalice_filled");
  public static final Identifier BAT_FLOWN = live("bat_flown");
  public static final Identifier HUNTER_KILLED = live("hunter_killed");
  public static final Identifier HOUSE_GUEST = live("house_guest");
  public static final Identifier BLOOD_BROUGHT_HOME = live("blood_brought_home");
  public static final Identifier NIGHT_LIVED = live("night_lived");
  public static final Identifier THRALL_LOST = live("thrall_lost");
  public static final Identifier DOWNED = live("downed");
  public static final Identifier SUSPICION_EASED = live("suspicion_eased");
  public static final Identifier INQUISITOR_DRAINED = live("inquisitor_drained");
  public static final Identifier STAGE_REACHED = live("stage_reached");
  public static final Identifier STAKED = live("staked");
  public static final Identifier VAMPIRE_BAT_KILLED = live("vampire_bat_killed");
  public static final Identifier BITE_BROKEN = live("bite_broken");
  public static final Identifier COATED_HIT = live("coated_hit");
  public static final Identifier VAMPIRE_STAKED = live("vampire_staked");
  public static final Identifier SUN_FINISHED = live("sun_finished");
  public static final Identifier CONTRACT_FINISHED = live("contract_finished");
  public static final Identifier HOUSEHOLD_CLEARED = live("household_cleared");
  public static final Identifier SEEN_THROUGH = live("seen_through");
  public static final Identifier ELDEST_ENDED = live("eldest_ended");
  public static final Identifier BITTEN = live("bitten");
  public static final Identifier RANK_REACHED = live("rank_reached");
  public static final Identifier ACCESSORIES_HELD = live("accessories_held");

  public static final String TOP = "top";
  public static final String ANIMAL = "animal";
  public static final String VILLAGER = "villager";
  public static final String SEEN = "seen";
  public static final String PROFESSION = "profession";
  public static final String PREY = "prey";
  public static final String GIFTED = "gifted";
  public static final String LAST = "last";
  public static final String THRALL = "thrall";
  public static final String HOUSE_FULL = "house_full";
  public static final String CASTLE_FULL = "castle_full";
  public static final String RANG_BELL = "rang_bell";
  public static final String ARMOURED = "armoured";
  public static final String STAGE = "stage";
  public static final String STAGE_RANK = "stage_rank";
  public static final String COATING = "coating";
  public static final String IN_VILLAGE = "in_village";
  public static final String IN_COFFIN = "in_coffin";
  public static final String LORD = "lord";
  public static final String CASTLE = "castle";
  public static final String NEAREST_PLAYER = "nearest_player";
  public static final String BY_DAY = "by_day";
  public static final String SUMMONER = "summoner";
  public static final String STAKER = "staker";
  public static final String RANK = "rank";
  public static final String VAMPIRE = "vampire";

  private DeedKinds() {}

  public static Set<Identifier> live() {
    return Set.copyOf(SEEN_BY_GAME);
  }

  private static Identifier live(final String path) {
    final Identifier id = Identifier.of(Nightborn.MOD_ID, path);
    SEEN_BY_GAME.add(id);
    return id;
  }
}
