package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.guild.build.HunterGrowth;
import com.grahambartley.nightborn.rank.RankLadder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.test.TestContext;

final class TestHunters {

  private TestHunters() {}

  static void unskilled(final TestContext context, final LivingEntity hunter) {
    OathAttachment.set(
        hunter, HunterGrowth.sworn(RankLadder.of(context.getWorld().getRegistryManager()), 0));
  }
}
