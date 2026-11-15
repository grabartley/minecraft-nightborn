package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.mojang.serialization.Codec;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.StringIdentifiable;

public enum SkillTree implements StringIdentifiable {
  VAMPIRE("vampire"),
  HUNTER("hunter");

  public static final Codec<SkillTree> CODEC = StringIdentifiable.createCodec(SkillTree::values);

  private final String id;

  SkillTree(final String id) {
    this.id = id;
  }

  public static SkillTree of(final Faction faction) {
    return faction == Faction.VAMPIRE ? VAMPIRE : HUNTER;
  }

  public static SkillTree of(final PlayerEntity player) {
    return of(FactionAttachment.get(player));
  }

  public static SkillTree of(final LivingEntity entity) {
    return entity instanceof PlayerEntity player ? of(player) : VAMPIRE;
  }

  public String translationKey() {
    return "nightborn.skill_tree." + id;
  }

  @Override
  public String asString() {
    return id;
  }
}
