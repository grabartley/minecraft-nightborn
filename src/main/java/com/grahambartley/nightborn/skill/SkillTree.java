package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.StringIdentifiable;

public enum SkillTree implements StringIdentifiable {
  VAMPIRE("vampire", 0),
  HUNTER("hunter", 1);

  public static final Codec<SkillTree> CODEC = StringIdentifiable.createCodec(SkillTree::values);

  private final String id;
  private final int revision;

  SkillTree(final String id, final int revision) {
    this.id = id;
    this.revision = revision;
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

  public String linesKey() {
    return translationKey() + ".lines";
  }

  public int revision() {
    return revision;
  }

  @Override
  public String asString() {
    return id;
  }
}
