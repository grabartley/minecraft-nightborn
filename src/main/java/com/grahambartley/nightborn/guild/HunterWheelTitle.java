package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.skill.SkillNames;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class HunterWheelTitle {

  static final String KEY = "nightborn.hunter.wheel_title";

  private HunterWheelTitle() {}

  public static Text of(final Text name, final List<Identifier> paths) {
    return paths.isEmpty() ? name : Text.translatable(KEY, name, SkillNames.lines(paths));
  }
}
