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

  public static int huntPoints(final SkillSettings settings, final int stageRank) {
    return settings.huntPoints() + Math.max(0, stageRank) * settings.stagePoints();
  }

  public static boolean fromHunt(
      final PlayerEntity hunter, final SkillSettings settings, final int stageRank) {
    final int points = huntPoints(settings, stageRank);
    if (Vampires.isVampire(hunter) || points <= 0) {
      return false;
    }
    final TreeProgress progress = SkillAttachment.get(hunter, SkillTree.HUNTER);
    SkillAttachment.set(hunter, SkillTree.HUNTER, progress.addPoints(points));
    return true;
  }
}
