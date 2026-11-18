package com.grahambartley.nightborn.skill;

import java.util.List;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
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

  public static String stage(final Identifier id) {
    return "vampire_stage." + id.getNamespace() + "." + id.getPath();
  }

  public static Text lines(final List<Identifier> lines) {
    if (lines.isEmpty()) {
      return Text.translatable("nightborn.command.player.skills.none");
    }
    final MutableText names = Text.empty();
    for (int i = 0; i < lines.size(); i++) {
      if (i > 0) {
        names.append(", ");
      }
      names.append(Text.translatable(branch(lines.get(i))));
    }
    return names;
  }
}
