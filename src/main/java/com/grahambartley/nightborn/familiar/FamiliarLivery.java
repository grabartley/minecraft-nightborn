package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerType;

public final class FamiliarLivery {

  private FamiliarLivery() {}

  public static Identifier texture(final VillagerType type) {
    return Identifier.of(
        Nightborn.MOD_ID,
        "textures/entity/human_familiar/"
            + Registries.VILLAGER_TYPE.getId(type).getPath()
            + ".png");
  }
}
