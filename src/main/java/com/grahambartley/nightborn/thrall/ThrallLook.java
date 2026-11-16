package com.grahambartley.nightborn.thrall;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerType;

public final class ThrallLook {

  static final String PLAINS = "plains";

  private ThrallLook() {}

  public static Identifier texture(final VillagerType type) {
    final Identifier id = Registries.VILLAGER_TYPE.getId(type);
    return Identifier.of(
        Nightborn.MOD_ID,
        "textures/entity/thrall/"
            + (Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace()) ? id.getPath() : PLAINS)
            + ".png");
  }
}
