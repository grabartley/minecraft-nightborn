package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.mob.VampireEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class ReadTask implements HouseholdTask {

  static final double READ_REACH = 2.2;
  static final int GIVE_UP_TICKS = 400;

  private final VampireEntity vampire;
  private final ServerWorld world;
  private final BlockPos books;
  private final Walk walk;
  private final Countdown reading;
  private final Countdown approach = new Countdown(GIVE_UP_TICKS);
  private boolean open;

  public ReadTask(
      final VampireEntity vampire, final ServerWorld world, final BlockPos books, final int ticks) {
    this.vampire = vampire;
    this.world = world;
    this.books = books;
    this.walk = new Walk(vampire);
    this.reading = new Countdown(ticks);
  }

  @Override
  public boolean tick() {
    if (open) {
      return reading.running();
    }
    if (!approach.running() || !FurnitureUse.is(world, books, FurnitureKind.BOOKS)) {
      return false;
    }
    final Vec3d shelf = Vec3d.ofCenter(books);
    if (walk.toward(shelf, READ_REACH)) {
      vampire.getLookControl().lookAt(shelf);
      vampire.swingHand(Hand.MAIN_HAND);
      vampire.lendHand(new ItemStack(Items.BOOK));
      vampire.setReading(true);
      open = true;
    }
    return true;
  }

  @Override
  public void stop() {
    if (open) {
      vampire.setReading(false);
      vampire.returnHand();
      open = false;
    }
  }
}
