package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.config.SkillSettings;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.player.PlayerEntity;

public final class SkillEarning {

  private SkillEarning() {}

  public static void fromBlood(
      final PlayerEntity vampire, final float worth, final SkillSettings settings) {
    if (Vampires.isVampire(vampire)) {
      final TreeProgress progress = SkillAttachment.get(vampire, SkillTree.VAMPIRE);
      SkillAttachment.set(
          vampire, SkillTree.VAMPIRE, progress.addBlood(worth, settings.bloodPerPoint()));
    }
  }

  public static void fromDrain(final PlayerEntity vampire, final SkillSettings settings) {
    if (Vampires.isVampire(vampire) && settings.drainBonusPoints() > 0) {
      final TreeProgress progress = SkillAttachment.get(vampire, SkillTree.VAMPIRE);
      SkillAttachment.set(
          vampire, SkillTree.VAMPIRE, progress.addPoints(settings.drainBonusPoints()));
    }
  }
}
