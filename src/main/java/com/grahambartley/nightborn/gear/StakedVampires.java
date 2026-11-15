package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillAttributes;
import com.grahambartley.nightborn.skill.SkillBranch;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.skill.StakePenalty;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class StakedVampires {

  private StakedVampires() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(StakedVampires::afterDeath);
  }

  private static void afterDeath(final LivingEntity entity, final DamageSource source) {
    final NightbornConfig config = ServerConfigService.current();
    if (entity instanceof ServerPlayerEntity player
        && Vampires.isVampire(player)
        && Staking.staked(source)
        && !config.downed().hardcoreVampireDeath()) {
      penalise(player, config.skills().stakePenalty());
    }
  }

  static void penalise(final ServerPlayerEntity player, final int percent) {
    final DynamicRegistryManager registries = player.getServerWorld().getRegistryManager();
    final TreeProgress before = SkillAttachment.get(player, SkillTree.VAMPIRE);
    final TreeProgress after =
        StakePenalty.apply(before, percent, skill -> bloodline(registries, skill));
    SkillAttachment.set(player, SkillTree.VAMPIRE, after);
    SkillAttributes.apply(player);
    final int lost = before.earned() - after.earned();
    if (lost > 0) {
      player.sendMessage(Text.translatable("nightborn.staked.penalty", lost), false);
    }
  }

  private static Optional<Identifier> bloodline(
      final DynamicRegistryManager registries, final Identifier id) {
    final Skill skill = Skills.skill(registries, id);
    if (skill == null) {
      return Optional.empty();
    }
    final SkillBranch branch = Skills.branch(registries, skill.branch());
    return branch != null && branch.bloodline() ? Optional.of(skill.branch()) : Optional.empty();
  }
}
