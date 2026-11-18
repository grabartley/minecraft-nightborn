package com.grahambartley.nightborn.hunt;

import com.grahambartley.nightborn.config.HuntSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

final class BellRing {

  static final float RING_VOLUME = 1.5F;
  static final float ANSWER_VOLUME = 6.0F;
  static final float ANSWER_PITCH = 0.8F;

  private BellRing() {}

  static void ring(final ServerPlayerEntity player) {
    final ServerWorld world = player.getServerWorld();
    world.playSound(
        null, player.getBlockPos(), HuntSounds.BELL_RING, SoundCategory.PLAYERS, RING_VOLUME, 1.0F);
    final HuntSettings settings = ServerConfigService.current().hunt();
    final int seal = sealSlot(player.getInventory());
    final boolean busy =
        player.hasAttached(HuntAttachment.CALL)
            || HuntAttachment.get(player).phase() == HuntRecord.Phase.VISIT;
    if (!BellCall.calls(
        seal >= 0,
        HuntRules.inArrivalWindow(Hunts.time(player), settings),
        world.getRegistryKey() == World.OVERWORLD,
        HuntVisits.underOpenSky(player),
        busy)) {
      return;
    }
    if (!player.getAbilities().creativeMode) {
      player.getInventory().getStack(seal).decrement(1);
    }
    player.setAttached(HuntAttachment.CALL, BellCall.rung(settings.bellBuildUpTicks()));
    player.sendMessage(Text.translatable("nightborn.hunt.bell.answered"), false);
  }

  static void tick(final ServerPlayerEntity player) {
    final BellCall call = player.getAttached(HuntAttachment.CALL);
    if (call == null) {
      return;
    }
    if (!player.isAlive() || !HuntVisits.underOpenSky(player)) {
      player.removeAttached(HuntAttachment.CALL);
      player.sendMessage(Text.translatable("nightborn.hunt.bell.unanswered"), false);
      return;
    }
    final BellCall next = call.advanced();
    if (next.answers()) {
      answer(player, next);
    }
    if (!next.done()) {
      player.setAttached(HuntAttachment.CALL, next);
      return;
    }
    player.removeAttached(HuntAttachment.CALL);
    StandIn.arrive(player).ifPresent(man -> Hunts.visited(player, man));
  }

  static int sealSlot(final PlayerInventory inventory) {
    for (int slot = 0; slot < inventory.size(); slot++) {
      if (inventory.getStack(slot).isOf(HuntItems.GUILD_MASTERS_SEAL)) {
        return slot;
      }
    }
    return -1;
  }

  private static void answer(final ServerPlayerEntity player, final BellCall call) {
    final ServerWorld world = player.getServerWorld();
    final double angle = world.getRandom().nextDouble() * MathHelper.TAU;
    final Vec3d from =
        player
            .getPos()
            .add(
                Math.cos(angle) * call.answerDistance(),
                0.0,
                Math.sin(angle) * call.answerDistance());
    world.playSound(
        null,
        from.x,
        from.y,
        from.z,
        HuntSounds.BELLS_ANSWER,
        SoundCategory.AMBIENT,
        ANSWER_VOLUME,
        ANSWER_PITCH);
    world.spawnParticles(
        ParticleTypes.END_ROD,
        player.getX(),
        player.getY() + 1.0,
        player.getZ(),
        call.light(),
        1.5,
        1.0,
        1.5,
        0.02);
  }
}
