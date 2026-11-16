package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.mob.VampireEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class ChoreTask implements HouseholdTask {

  static final double TEND_REACH = 3.0;
  static final int GIVE_UP_TICKS = 400;
  static final int TEND_TICKS = 20;

  private final VampireEntity vampire;
  private final ServerWorld world;
  private final BlockPos spot;
  private final Chore chore;
  private final Walk walk;
  private final Countdown approach = new Countdown(GIVE_UP_TICKS);
  private Countdown tending;
  private ItemStack held = ItemStack.EMPTY;

  public ChoreTask(
      final VampireEntity vampire,
      final ServerWorld world,
      final BlockPos spot,
      final Chore chore) {
    this.vampire = vampire;
    this.world = world;
    this.spot = spot;
    this.chore = chore;
    this.walk = new Walk(vampire);
  }

  @Override
  public boolean tick() {
    final Vec3d target = Vec3d.ofCenter(spot);
    vampire.getLookControl().lookAt(target);
    if (tending != null) {
      if (tending.running()) {
        return true;
      }
      FurnitureUse.tend(world, spot, vampire, chore);
      return false;
    }
    if (!approach.running()) {
      return false;
    }
    if (walk.toward(target, TEND_REACH)) {
      if (chore.lights()) {
        held = vampire.getEquippedStack(EquipmentSlot.MAINHAND);
        vampire.householdLife().borrowHand(true);
        vampire.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.FLINT_AND_STEEL));
      }
      vampire.swingHand(Hand.MAIN_HAND);
      tending = new Countdown(TEND_TICKS);
    }
    return true;
  }

  @Override
  public void stop() {
    if (chore.lights() && tending != null) {
      vampire.equipStack(EquipmentSlot.MAINHAND, held);
      vampire.householdLife().borrowHand(false);
    }
  }
}
