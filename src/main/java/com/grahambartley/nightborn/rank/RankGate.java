package com.grahambartley.nightborn.rank;

import com.grahambartley.nightborn.mob.stage.StageGate;
import com.grahambartley.nightborn.skill.Skill;
import java.util.OptionalInt;
import net.minecraft.registry.DynamicRegistryManager;

public final class RankGate {

  private RankGate() {}

  public static OptionalInt needed(final DynamicRegistryManager registries, final Skill skill) {
    return needed(RankLadder.of(registries), skill);
  }

  public static OptionalInt needed(final RankLadder ladder, final Skill skill) {
    return skill
        .rank()
        .map(
            id -> {
              final OptionalInt rank = ladder.rankOf(id);
              return OptionalInt.of(rank.isPresent() ? rank.getAsInt() : StageGate.UNKNOWN_STAGE);
            })
        .orElse(OptionalInt.empty());
  }
}
