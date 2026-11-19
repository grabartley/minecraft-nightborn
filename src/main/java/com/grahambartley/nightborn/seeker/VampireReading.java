package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.age.AgeLadder;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.Skills;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class VampireReading {

  private VampireReading() {}

  public static Scene of(final LivingEntity vampire, final Vec3d spot) {
    return new Scene(
        vampire.getUuid(),
        stage(vampire),
        lines(
            SkillAttachment.owned(vampire).ownedInOrder(),
            id -> Skills.lineOf(vampire.getWorld().getRegistryManager(), id)),
        spot,
        vampire.getWorld().getTime(),
        true);
  }

  public static List<Identifier> lines(
      final Collection<Identifier> owned, final Function<Identifier, Optional<Identifier>> lineOf) {
    return owned.stream().map(lineOf).flatMap(Optional::stream).distinct().toList();
  }

  private static Optional<Identifier> stage(final LivingEntity vampire) {
    if (vampire instanceof VampireEntity mob) {
      return Optional.of(mob.stageId());
    }
    if (vampire instanceof PlayerEntity player) {
      return AgeLadder.of(player.getRegistryManager())
          .rung(AgeAttachment.get(player).rank())
          .map(AgeLadder.Rung::id);
    }
    return Optional.empty();
  }
}
