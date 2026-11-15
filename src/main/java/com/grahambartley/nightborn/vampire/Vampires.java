package com.grahambartley.nightborn.vampire;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.faction.FactionAttachment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class Vampires {

  public static final TagKey<EntityType<?>> VAMPIRE_MOBS =
      TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, "vampires"));

  private Vampires() {}

  public static boolean isVampire(final Entity entity) {
    if (entity instanceof PlayerEntity player) {
      return FactionAttachment.get(player) == Faction.VAMPIRE;
    }
    return entity != null && entity.getType().isIn(VAMPIRE_MOBS);
  }
}
