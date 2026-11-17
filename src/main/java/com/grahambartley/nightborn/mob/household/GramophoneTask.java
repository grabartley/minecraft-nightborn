package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.mob.VampireEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class GramophoneTask implements HouseholdTask {

  static final double REACH = 2.2;
  static final int GIVE_UP_TICKS = 400;

  private final VampireEntity vampire;
  private final ServerWorld world;
  private final BlockPos gramophone;
  private final Walk walk;
  private final Countdown listening;
  private final Countdown approach = new Countdown(GIVE_UP_TICKS);
  private boolean started;

  public GramophoneTask(
      final VampireEntity vampire,
      final ServerWorld world,
      final BlockPos gramophone,
      final int ticks) {
    this.vampire = vampire;
    this.world = world;
    this.gramophone = gramophone;
    this.walk = new Walk(vampire);
    this.listening = new Countdown(ticks);
  }

  @Override
  public boolean tick() {
    if (started) {
      return listening.running();
    }
    if (!approach.running() || !FurnitureUse.gramophoneReady(world, gramophone)) {
      return false;
    }
    final Vec3d at = Vec3d.ofCenter(gramophone);
    if (walk.toward(at, REACH)) {
      vampire.getLookControl().lookAt(at);
      vampire.swingHand(Hand.MAIN_HAND);
      started = FurnitureUse.startGramophone(world, gramophone);
      return started;
    }
    return true;
  }
}
