package com.grahambartley.nightborn.eldest;

import java.util.function.Supplier;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

public class PetBatItem extends Item {

  private final Supplier<EntityType<? extends PetBatEntity>> pet;

  public PetBatItem(
      final Supplier<EntityType<? extends PetBatEntity>> pet, final Settings settings) {
    super(settings);
    this.pet = pet;
  }

  @Override
  public ActionResult useOnBlock(final ItemUsageContext context) {
    final PlayerEntity player = context.getPlayer();
    if (player == null) {
      return ActionResult.PASS;
    }
    if (context.getWorld() instanceof ServerWorld world) {
      final PetBatEntity bat = pet.get().create(world);
      if (bat == null) {
        return ActionResult.FAIL;
      }
      final Vec3d at = Vec3d.ofCenter(context.getBlockPos().offset(context.getSide()));
      bat.refreshPositionAndAngles(at.x, at.y, at.z, player.getYaw(), 0.0F);
      bat.follow(player);
      bat.setCustomName(context.getStack().get(DataComponentTypes.CUSTOM_NAME));
      world.spawnEntity(bat);
      context.getStack().decrement(1);
    }
    return ActionResult.success(context.getWorld().isClient());
  }
}
