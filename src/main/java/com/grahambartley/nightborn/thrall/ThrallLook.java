package com.grahambartley.nightborn.thrall;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerType;

public final class ThrallLook {

  private ThrallLook() {}

  public static Identifier texture(final VillagerType type) {
    return Identifier.of(
        Nightborn.MOD_ID,
        "textures/entity/thrall/" + Registries.VILLAGER_TYPE.getId(type).getPath() + ".png");
  }
}
