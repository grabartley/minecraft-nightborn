package com.grahambartley.nightborn.mind;

import com.grahambartley.nightborn.skill.MesmerisePower;
import java.util.UUID;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

public record Gaze(RegistryKey<World> world, UUID target, long landsAt, MesmerisePower power) {

  public enum Step {
    HOLD,
    LAND,
    BREAK
  }

  public Step step(final boolean holding, final long now) {
    if (!holding) {
      return Step.BREAK;
    }
    return now >= landsAt ? Step.LAND : Step.HOLD;
  }
}
