package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.skill.HeldRanks;
import java.util.List;
import net.minecraft.entity.LivingEntity;

public final class HeldTrapGifts {

  private HeldTrapGifts() {}

  public static List<TrapGift> of(final LivingEntity holder) {
    return HeldRanks.of(holder).stream().flatMap(rank -> rank.trapGifts().stream()).toList();
  }
}
