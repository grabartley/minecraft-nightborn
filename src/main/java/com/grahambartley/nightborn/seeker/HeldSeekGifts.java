package com.grahambartley.nightborn.seeker;

import com.grahambartley.nightborn.skill.HeldRanks;
import java.util.List;
import net.minecraft.entity.LivingEntity;

public final class HeldSeekGifts {

  private HeldSeekGifts() {}

  public static List<SeekGift> of(final LivingEntity holder) {
    return HeldRanks.of(holder).stream().flatMap(rank -> rank.seekGifts().stream()).toList();
  }
}
