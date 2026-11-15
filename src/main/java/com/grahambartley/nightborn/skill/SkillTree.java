package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.faction.Faction;
import com.mojang.serialization.Codec;
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

  public String translationKey() {
    return "nightborn.skill_tree." + id;
  }

  @Override
  public String asString() {
    return id;
  }
}
