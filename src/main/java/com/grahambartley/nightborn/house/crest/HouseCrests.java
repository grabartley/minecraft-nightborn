package com.grahambartley.nightborn.house.crest;

import com.grahambartley.nightborn.house.Houses;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

public final class HouseCrests {

  private HouseCrests() {}

  public static Optional<Crest> of(final ServerPlayerEntity member) {
    return Houses.get(member.getServer())
        .of(member.getUuid())
        .flatMap(house -> house.identity().crest());
  }

  static void cue(final ServerPlayerEntity member, final BlockPos at) {
    member
        .getServerWorld()
        .playSound(null, at, SoundEvents.ITEM_DYE_USE, SoundCategory.BLOCKS, 1.0F, 0.8F);
  }
}
