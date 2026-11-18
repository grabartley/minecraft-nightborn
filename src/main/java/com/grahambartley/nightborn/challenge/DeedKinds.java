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
  public static final Identifier ACCESSORIES_WORN = live("accessories_worn");
  public static final Identifier OLD_BLOOD_DRUNK = live("old_blood_drunk");
  public static final Identifier HOUSE_FOUNDED = live("house_founded");
  public static final Identifier HOUSE_WON = live("house_won");
  public static final Identifier INQUISITOR_ENDED = live("inquisitor_ended");
  public static final Identifier TRIAL_COMPLETED = live("trial_completed");
  public static final Identifier STRUCTURE_ENTERED = live("structure_entered");
  public static final Identifier STAFF_KEPT = live("staff_kept");
  public static final Identifier CRUCIFIXES_HELD = live("crucifixes_held");
  public static final Identifier BANES_DEALT = live("banes_dealt");
  public static final Identifier SKILL_LEARNED = live("skill_learned");
  public static final Identifier JOURNAL_PAGES = live("journal_pages");
  public static final Identifier MIRROR_SELF = live("mirror_self");
  public static final Identifier FOOD_BROUGHT_UP = live("food_brought_up");
  public static final Identifier HUNG_THROUGH_NIGHT = live("hung_through_night");
  public static final Identifier WRONG_CALL = live("wrong_call");
  public static final Identifier ATE = live("ate");
  public static final Identifier PARASOL_WALKED = live("parasol_walked");
  public static final Identifier WOKE_RESTED = live("woke_rested");
  public static final Identifier NEMESIS_ENDED = live("nemesis_ended");

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
  public static final String GIFT = "gift";
  public static final String WARDED = "warded";
  public static final String HOUSEHOLD = "household";
  public static final String TRIAL = "trial";
  public static final String SIDE = "side";
  public static final String DISTINCT = "distinct";
  public static final String STRUCTURE = "structure";
  public static final String SLOTS = "slots";
  public static final String KINDS = "kinds";
  public static final String BANES = "banes";
  public static final String TREE = "tree";
  public static final String LINE = "line";
  public static final String SHARE = "share";
  public static final String JOURNAL = "journal";
  public static final String FOOD = "food";
  public static final String GARLIC_IN_OTHER_HAND = "garlic_in_other_hand";
  public static final String BOSS = "boss";

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
