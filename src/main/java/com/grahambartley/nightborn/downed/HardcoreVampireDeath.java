package com.grahambartley.nightborn.downed;

import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerCooldowns;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public final class HardcoreVampireDeath {

  private HardcoreVampireDeath() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.register(HardcoreVampireDeath::afterDeath);
  }

  private static void afterDeath(final LivingEntity entity, final DamageSource source) {
    if (entity instanceof ServerPlayerEntity player
        && Vampires.isVampire(player)
        && ServerConfigService.current().downed().hardcoreVampireDeath()) {
      reset(player);
    }
  }

  static void reset(final ServerPlayerEntity player) {
    FactionAttachment.set(player, Faction.HUMAN);
    BiteSicknessAttachment.clear(player);
    SkillAttachment.clear(player);
    PowerCooldownAttachment.set(player, PowerCooldowns.NONE);
    player.getEnderChestInventory().clear();
    if (player.getServerWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
      player.getInventory().clear();
      player.setExperienceLevel(0);
      player.setExperiencePoints(0);
      player.experienceProgress = 0.0F;
      player.totalExperience = 0;
    }
    player.setSpawnPoint(World.OVERWORLD, null, 0.0F, false, false);
  }
}
