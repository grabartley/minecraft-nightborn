package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.brain.Intents;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class Unsticking {

  static final AttachmentType<StuckState> STATE =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "stuck_state"));

  static final int BACK_RANGE = 6;
  static final int BACK_HEIGHT = 3;

  private Unsticking() {}

  public static void tick(final PathAwareEntity mob, final int patience) {
    final LivingEntity target = mob.getTarget();
    final long now = mob.getWorld().getTime();
    StuckState state =
        mob.getAttachedOrElse(STATE, StuckState.FRESH)
            .observe(
                target == null ? Double.NaN : mob.distanceTo(target),
                target != null && Intents.read(mob).fights(),
                patience);
    switch (state.step()) {
      case LAND -> {
        state = state.landed(now);
        Levitation.sink(mob);
      }
      case STEP_BACK -> state = state.steppingBack(spotAwayFrom(mob, target), now);
      case GIVE_UP -> {
        state = state.gaveUp(target.getUuid(), now);
        mob.setTarget(null);
        mob.getNavigation().stop();
      }
      case NONE -> {}
    }
    mob.setAttached(STATE, state);
  }

  public static boolean stuck(final MobEntity mob) {
    return mob.getAttachedOrElse(STATE, StuckState.FRESH).stuck();
  }

  public static boolean holdsDown(final MobEntity mob) {
    return mob.getAttachedOrElse(STATE, StuckState.FRESH).holdsDown(mob.getWorld().getTime());
  }

  @Nullable
  public static Vec3d backingTo(final MobEntity mob) {
    return mob.getAttachedOrElse(STATE, StuckState.FRESH).backingTo(mob.getWorld().getTime());
  }

  public static boolean forgoes(final MobEntity mob, final LivingEntity target) {
    return mob.getAttachedOrElse(STATE, StuckState.FRESH)
        .forgoes(target.getUuid(), mob.getWorld().getTime());
  }

  @Nullable
  private static Vec3d spotAwayFrom(final PathAwareEntity mob, final LivingEntity target) {
    return NoPenaltyTargeting.findFrom(mob, BACK_RANGE, BACK_HEIGHT, target.getPos());
  }
}
