package com.grahambartley.nightborn.downed;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.age.Ageing;
import com.grahambartley.nightborn.coffin.CoffinOwnership;
import com.grahambartley.nightborn.coffin.CoffinRespawn;
import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.familiar.FamiliarRelease;
import com.grahambartley.nightborn.feeding.TurningBite;
import com.grahambartley.nightborn.hunt.Hunts;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import com.grahambartley.nightborn.skill.PowerCooldownAttachment;
import com.grahambartley.nightborn.skill.PowerCooldowns;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.thrall.Thralls;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

public final class HardcoreVampireDeath {

  private HardcoreVampireDeath() {}

  private static final Identifier RESET_PHASE = Identifier.of(Nightborn.MOD_ID, "hardcore_reset");

  public static void register() {
    ServerLivingEntityEvents.AFTER_DEATH.addPhaseOrdering(Event.DEFAULT_PHASE, RESET_PHASE);
    ServerLivingEntityEvents.AFTER_DEATH.register(RESET_PHASE, HardcoreVampireDeath::afterDeath);
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
    Ageing.clear(player);
    Hunts.clear(player);
    BiteSicknessAttachment.clear(player);
    TurningBite.set(player, false);
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
    CoffinOwnership.release(player.server, player.getUuid());
    CoffinRespawn.clear(player);
    Thralls.freeOwner(player.server, player.getUuid());
    FamiliarRelease.freeOwner(player.server, player.getUuid());
  }
}
