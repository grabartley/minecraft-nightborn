package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.decor.ChaliceBlock;
import com.grahambartley.nightborn.decor.ChaliceContents;
import com.grahambartley.nightborn.servant.ChestMarking;
import java.util.Optional;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public final class BloodStore {

  static final String MARKED = "nightborn.house.store.marked";
  static final String CLEARED = "nightborn.house.store.cleared";
  static final String NO_HOUSE = "nightborn.house.store.no_house";

  private BloodStore() {}

  static void register() {
    UseBlockCallback.EVENT.register(BloodStore::onUseBlock);
  }

  static ActionResult onUseBlock(
      final PlayerEntity player, final World world, final Hand hand, final BlockHitResult hit) {
    final BlockPos pos = hit.getBlockPos();
    if (hand != Hand.MAIN_HAND
        || !player.isSneaking()
        || !player.getMainHandStack().isOf(BloodItems.BLOOD_BOTTLE)
        || !holdsBlood(world, pos)) {
      return ActionResult.PASS;
    }
    if (!(player instanceof ServerPlayerEntity lord)) {
      return ActionResult.SUCCESS;
    }
    final Houses houses = Houses.get(lord.getServer());
    final Optional<House> house = houses.ledBy(lord.getUuid());
    if (house.isEmpty()) {
      lord.sendMessage(Text.translatable(NO_HOUSE), true);
      return ActionResult.SUCCESS;
    }
    final House marked = house.get().storeToggled(GlobalPos.create(world.getRegistryKey(), pos));
    houses.put(marked);
    lord.sendMessage(Text.translatable(marked.store().isPresent() ? MARKED : CLEARED), true);
    world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 0.8F, 0.7F);
    return ActionResult.SUCCESS;
  }

  public static boolean holdsBlood(final World world, final BlockPos pos) {
    return world.getBlockState(pos).getBlock() instanceof ChaliceBlock
        || ChestMarking.isErrandChest(world, pos);
  }

  public static boolean deposit(
      final ServerWorld world, final BlockPos pos, final BloodQuality quality) {
    final BlockState state = world.getBlockState(pos);
    if (state.getBlock() instanceof ChaliceBlock) {
      final ChaliceContents contents = ChaliceBlock.contents(state);
      if (!contents.takes(quality)) {
        return false;
      }
      world.setBlockState(
          pos, ChaliceBlock.with(state, contents.poured(quality)), Block.NOTIFY_ALL);
      world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
      return true;
    }
    final BlockEntity container = world.getBlockEntity(pos);
    if (!(container instanceof Inventory inventory) || !holdsBlood(world, pos)) {
      return false;
    }
    final ItemStack left =
        HopperBlockEntity.transfer(null, inventory, BloodBottleItem.of(quality), null);
    if (!left.isEmpty()) {
      return false;
    }
    inventory.markDirty();
    world.playSound(null, pos, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, 1.0F);
    return true;
  }
}
