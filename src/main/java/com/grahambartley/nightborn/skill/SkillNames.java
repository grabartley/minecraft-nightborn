package com.grahambartley.nightborn.skill;

import net.minecraft.util.Identifier;

public final class SkillNames {

  private SkillNames() {}

  public static String skill(final Identifier id) {
    return "skill." + id.getNamespace() + "." + id.getPath().replace('/', '.');
  }

  public static String description(final Identifier id) {
    return skill(id) + ".description";
  }

  public static String branch(final Identifier id) {
    return "skill_branch." + id.getNamespace() + "." + id.getPath();
  }
}
