package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.config.RitualSettings;
import com.grahambartley.nightborn.config.SkillSettings;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class SkillEarning {

  static final String POINT_EARNED = "nightborn.skills.point_earned";

  private SkillEarning() {}

  public static void fromBlood(
      final PlayerEntity vampire, final float worth, final SkillSettings settings) {
    if (Vampires.isVampire(vampire)) {
      gain(vampire, SkillTree.VAMPIRE, bloodExperience(worth, settings), settings);
    }
  }

  public static void fromDrain(final PlayerEntity vampire, final SkillSettings settings) {
    if (Vampires.isVampire(vampire)) {
      gain(vampire, SkillTree.VAMPIRE, settings.drainBonusExperience(), settings);
    }
  }

  public static float bloodExperience(final float worth, final SkillSettings settings) {
    return Math.max(0.0F, worth) * settings.bloodExperience();
  }

  public static int huntExperience(final SkillSettings settings, final int stageRank) {
    return settings.huntExperience() + Math.max(0, stageRank) * settings.stageExperience();
  }

  public static boolean fromHunt(
      final PlayerEntity hunter, final SkillSettings settings, final int stageRank) {
    return grantHunter(hunter, huntExperience(settings, stageRank), settings);
  }

  public static boolean fromContract(final PlayerEntity hunter, final SkillSettings settings) {
    return grantHunter(hunter, settings.contractExperience(), settings);
  }

  public static boolean fromEldest(
      final PlayerEntity hunter, final RitualSettings ritual, final SkillSettings settings) {
    return grantHunter(hunter, ritual.eldestExperience(), settings);
  }

  private static boolean grantHunter(
      final PlayerEntity hunter, final int experience, final SkillSettings settings) {
    if (Vampires.isVampire(hunter) || experience <= 0) {
      return false;
    }
    gain(hunter, SkillTree.HUNTER, experience, settings);
    return true;
  }

  private static void gain(
      final PlayerEntity player,
      final SkillTree tree,
      final float experience,
      final SkillSettings settings) {
    final TreeProgress before = SkillAttachment.get(player, tree);
    final TreeProgress after =
        before.addExperience(experience, settings.firstPointExperience(), settings.pointCostRise());
    SkillAttachment.set(player, tree, after);
    if (after.earned() > before.earned() && player instanceof ServerPlayerEntity earner) {
      earner.sendMessage(Text.translatable(POINT_EARNED, after.free()), true);
      earner
          .getServerWorld()
          .playSound(
              null,
              earner.getBlockPos(),
              SoundEvents.ENTITY_PLAYER_LEVELUP,
              SoundCategory.PLAYERS,
              0.5F,
              0.7F);
    }
  }
}
