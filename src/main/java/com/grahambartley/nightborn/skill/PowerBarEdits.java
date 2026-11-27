package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.network.SetPowerSlotPayload;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class PowerBarEdits {

  private PowerBarEdits() {}

  public static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        SetPowerSlotPayload.ID,
        (payload, context) -> set(context.player(), payload.slot(), payload.skill()));
  }

  public static boolean set(
      final ServerPlayerEntity player, final int slot, final Optional<Identifier> skill) {
    final Registry<Skill> skills =
        Skills.skills(player.getServerWorld().getRegistryManager()).orElse(null);
    if (skills == null || !GuildMembership.sided(player)) {
      return false;
    }
    final SkillTree tree = SkillTree.of(player);
    final Optional<TreeProgress> changed =
        PowerBar.set(SkillAttachment.get(player, tree), PowerBar.active(skills), slot, skill);
    changed.ifPresent(progress -> SkillAttachment.set(player, tree, progress));
    return changed.isPresent();
  }
}
