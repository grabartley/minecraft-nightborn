package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.mojang.serialization.Codec;
import java.util.Optional;
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

  public static Optional<SkillTree> of(final LivingEntity entity) {
    if (entity instanceof PlayerEntity player) {
      return Optional.of(of(player));
    }
    return entity instanceof SkillHolder holder
        ? Optional.of(holder.skillTree())
        : Optional.empty();
  }

  public String translationKey() {
    return "nightborn.skill_tree." + id;
  }

  @Override
  public String asString() {
    return id;
  }
}
