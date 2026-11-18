package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.coffin.CoffinOwnership;
import com.grahambartley.nightborn.structure.HouseholdRoll;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public final class HouseFounding {

  private HouseFounding() {}

  static void register() {
    EntitySleepEvents.START_SLEEPING.register(
        (sleeper, pos) -> {
          if (sleeper instanceof ServerPlayerEntity player) {
            final BlockState state = player.getServerWorld().getBlockState(pos);
            if (state.getBlock() instanceof CoffinBlock) {
              lieDown(player, CoffinBlock.head(pos, state));
            }
          }
        });
  }

  public static HouseRules.Founding lieDown(final ServerPlayerEntity player, final BlockPos head) {
    if (!Vampires.isVampire(player)) {
      return HouseRules.Founding.NONE;
    }
    final ServerWorld world = player.getServerWorld();
    final UUID self = player.getUuid();
    final GlobalPos coffin = GlobalPos.create(world.getRegistryKey(), head.toImmutable());
    final Houses houses = Houses.get(world.getServer());
    final Optional<House> led = houses.ledBy(self);
    final HouseholdRoll roll = HouseholdRoll.get(world.getServer());
    final Optional<GlobalPos> home = roll.homeOfHeadCoffin(coffin);
    final HouseRules.Founding verdict =
        HouseRules.founding(
            AgeAttachment.get(player).rank(),
            led.isPresent(),
            houses.of(self).filter(house -> !house.lord().equals(self)).isPresent(),
            CoffinOwnership.ownerAt(world, head).filter(owner -> !owner.equals(self)).isEmpty(),
            home.map(
                structure ->
                    new HouseRules.Claim(
                        roll.record(structure).gone(),
                        houses
                            .holding(structure)
                            .filter(house -> !house.lord().equals(self))
                            .isPresent(),
                        led.flatMap(House::seat).filter(structure::equals).isPresent())));
    switch (verdict) {
      case FOUND -> houses.put(House.founded(self, coffin));
      case CLAIM ->
          houses.put(
              led.orElseGet(() -> House.founded(self, coffin)).anchoredAt(coffin).seatedAt(home));
      case NONE, LIVED_IN, CLAIMED -> {}
    }
    if (HouseRules.founds(verdict, led.isPresent())) {
      Deeds.record(player, Deed.of(DeedKinds.HOUSE_FOUNDED));
    }
    verdict.key().ifPresent(key -> cue(player, key, verdict));
    if ((verdict == HouseRules.Founding.FOUND || verdict == HouseRules.Founding.CLAIM)
        && houses.ledBy(self).filter(house -> house.identity().name().isEmpty()).isPresent()) {
      HouseNaming.offer(player);
    }
    return verdict;
  }

  private static void cue(
      final ServerPlayerEntity player, final String key, final HouseRules.Founding verdict) {
    player.sendMessage(Text.translatable(key), false);
    if (verdict == HouseRules.Founding.FOUND || verdict == HouseRules.Founding.CLAIM) {
      player
          .getServerWorld()
          .playSound(
              null,
              player.getBlockPos(),
              SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN,
              SoundCategory.PLAYERS,
              1.0F,
              0.6F);
    }
  }
}
