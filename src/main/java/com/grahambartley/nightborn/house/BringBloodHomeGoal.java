package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.HomeCoffins;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec3d;

public class BringBloodHomeGoal extends Goal {

  static final double SPEED = 1.0;
  static final double REACH = 2.5;
  static final int REPATH_TICKS = 20;

  private final VampireEntity member;
  private int repath;

  public BringBloodHomeGoal(final VampireEntity member) {
    this.member = member;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    return store().isPresent()
        && member.getTarget() == null
        && !member.isSleeping()
        && member.householdTie().carried().isPresent()
        && !member.homeLife().out(member, member.hungry());
  }

  @Override
  public boolean shouldContinue() {
    return canStart();
  }

  @Override
  public void start() {
    repath = 0;
  }

  @Override
  public void stop() {
    member.getNavigation().stop();
  }

  @Override
  public void tick() {
    final Optional<BlockPos> store = store();
    final Optional<BloodQuality> carried = member.householdTie().carried();
    if (store.isEmpty() || carried.isEmpty() || !(member.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final Vec3d at = Vec3d.ofCenter(store.get());
    member.getLookControl().lookAt(at);
    if (member.getPos().isInRange(at, REACH)) {
      if (!BloodStore.deposit(world, store.get(), carried.get())) {
        member.dropStack(BloodBottleItem.of(carried.get()));
      }
      Deeds.record(
          world.getServer(), member.householdTie().lord(), Deed.of(DeedKinds.BLOOD_BROUGHT_HOME));
      member.householdTie().delivered();
      return;
    }
    if (--repath <= 0) {
      repath = getTickCount(REPATH_TICKS);
      member.getNavigation().startMovingTo(at.x, at.y, at.z, SPEED);
    }
  }

  private Optional<BlockPos> store() {
    if (!(member.getWorld() instanceof ServerWorld world)) {
      return Optional.empty();
    }
    return MemberLife.houseOf(world.getServer(), member)
        .flatMap(House::store)
        .filter(store -> store.dimension().equals(world.getRegistryKey()))
        .map(GlobalPos::pos)
        .filter(pos -> HomeCoffins.loaded(world, pos));
  }
}
