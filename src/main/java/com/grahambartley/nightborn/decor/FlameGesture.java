package com.grahambartley.nightborn.decor;

import com.grahambartley.nightborn.mob.Gestures;
import com.grahambartley.nightborn.mob.VampirePose;
import com.grahambartley.nightborn.mob.household.FurnitureKind;
import com.grahambartley.nightborn.mob.household.FurnitureUse;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class FlameGesture {

  private FlameGesture() {}

  public static VampirePose of(final boolean lights) {
    return lights ? VampirePose.LIGHT_CANDLE : VampirePose.DOUSE_CANDLE;
  }

  public static Optional<VampirePose> changed(
      final boolean tended, final boolean litBefore, final boolean litAfter) {
    return tended && litBefore != litAfter ? Optional.of(of(litAfter)) : Optional.empty();
  }

  public static void afterUse(
      final LivingEntity user, final World world, final BlockPos pos, final boolean litBefore) {
    final BlockState after = world.getBlockState(pos);
    changed(after.isIn(FurnitureKind.TENDED_LIGHTS), litBefore, FurnitureUse.lit(after))
        .ifPresent(pose -> Gestures.show(user, pose));
  }
}
