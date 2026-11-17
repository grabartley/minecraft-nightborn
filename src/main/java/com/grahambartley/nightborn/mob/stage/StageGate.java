package com.grahambartley.nightborn.mob.stage;

import com.grahambartley.nightborn.skill.Skill;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

public final class StageGate {

  public static final int UNKNOWN_STAGE = Integer.MAX_VALUE;

  private StageGate() {}

  public static boolean open(final OptionalInt needed, final int rank) {
    return needed.isEmpty() || rank >= needed.getAsInt();
  }

  public static OptionalInt needed(final DynamicRegistryManager registries, final Skill skill) {
    return needed(skill, id -> VampireStages.stage(registries, id));
  }

  public static OptionalInt needed(final Map<Identifier, VampireStage> stages, final Skill skill) {
    return needed(skill, id -> Optional.ofNullable(stages.get(id)));
  }

  private static OptionalInt needed(
      final Skill skill, final Function<Identifier, Optional<VampireStage>> stages) {
    return skill
        .stage()
        .map(id -> OptionalInt.of(stages.apply(id).map(VampireStage::rank).orElse(UNKNOWN_STAGE)))
        .orElse(OptionalInt.empty());
  }
}
