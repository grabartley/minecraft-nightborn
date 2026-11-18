package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.wheel.CommandWheel;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class HouseWheel {

  private HouseWheel() {}

  static void register() {
    CommandWheel.subject(VampireEntity.class, SkillTree.VAMPIRE).add(new MemberOrders());
    CommandWheel.subject(PlayerEntity.class, SkillTree.VAMPIRE)
        .add(new HouseInvite())
        .add(new HouseLeave());
    UseEntityCallback.EVENT.register(HouseWheel::onUse);
  }

  static ActionResult onUse(
      final PlayerEntity player,
      final World world,
      final Hand hand,
      final Entity entity,
      @Nullable final EntityHitResult hit) {
    if (!HouseRules.opensWheel(
        hand == Hand.MAIN_HAND,
        player.getMainHandStack().isEmpty(),
        player.isSneaking(),
        player.isSpectator(),
        entity instanceof VampireEntity || entity instanceof PlayerEntity)) {
      return ActionResult.PASS;
    }
    if (!(player instanceof ServerPlayerEntity server)) {
      return ActionResult.PASS;
    }
    return CommandWheel.open(server, entity) ? ActionResult.SUCCESS : ActionResult.PASS;
  }
}
