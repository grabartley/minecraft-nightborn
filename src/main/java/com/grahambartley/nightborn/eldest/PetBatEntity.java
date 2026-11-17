package com.grahambartley.nightborn.eldest;

import com.grahambartley.nightborn.mob.BatSteering;
import com.grahambartley.nightborn.mob.VampireBatEntity;
import java.util.UUID;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PetBatEntity extends VampireBatEntity {

  static final String OWNER_KEY = "Owner";

  @Nullable private UUID owner;

  public PetBatEntity(final EntityType<? extends BatEntity> type, final World world) {
    super(type, world);
  }

  public void follow(final PlayerEntity player) {
    owner = player.getUuid();
  }

  @Nullable
  public UUID owner() {
    return owner;
  }

  @Override
  protected void mobTick() {
    final PlayerEntity master = owner == null ? null : getWorld().getPlayerByUuid(owner);
    if (master == null) {
      setVelocity(getVelocity().multiply(0.8));
      return;
    }
    if (isRoosting()) {
      setRoosting(false);
    }
    final Vec3d perch = PetBatRules.perch(master.getEyePos(), age);
    if (PetBatRules.lost(squaredDistanceTo(master))) {
      requestTeleport(perch.x, perch.y, perch.z);
      return;
    }
    final Vec3d velocity = BatSteering.toward(getVelocity(), perch.subtract(getPos()));
    setVelocity(velocity);
    setYaw(BatSteering.yaw(velocity));
    forwardSpeed = 0.5F;
  }

  @Override
  protected ActionResult interactMob(final PlayerEntity player, final Hand hand) {
    if (!PetBatRules.comesHome(player.getUuid().equals(owner), player.isSneaking())) {
      return super.interactMob(player, hand);
    }
    if (!getWorld().isClient()) {
      final ItemStack stack = new ItemStack(EldestSpoils.TAME_BAT);
      if (hasCustomName()) {
        stack.set(DataComponentTypes.CUSTOM_NAME, getCustomName());
      }
      player.getInventory().offerOrDrop(stack);
      discard();
    }
    return ActionResult.success(getWorld().isClient());
  }

  @Override
  public boolean isInvulnerableTo(final DamageSource source) {
    return !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY);
  }

  @Override
  public boolean cannotDespawn() {
    return true;
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    if (owner != null) {
      nbt.putUuid(OWNER_KEY, owner);
    }
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    owner = nbt.containsUuid(OWNER_KEY) ? nbt.getUuid(OWNER_KEY) : null;
  }
}
