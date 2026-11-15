package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ContainerLock;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public final class FamiliarOrders {

  public static final TagKey<Block> ERRAND_CHESTS =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(Nightborn.MOD_ID, "familiar_errand_chests"));

  private FamiliarOrders() {}

  static void register() {
    UseBlockCallback.EVENT.register(FamiliarOrders::onUseBlock);
  }

  private static ActionResult onUseBlock(
      final PlayerEntity player, final World world, final Hand hand, final BlockHitResult hit) {
    if (!(player instanceof ServerPlayerEntity owner)
        || hand != Hand.MAIN_HAND
        || !player.isSneaking()
        || player.isSpectator()
        || !player.getMainHandStack().isEmpty()
        || !player.getOffHandStack().isEmpty()) {
      return ActionResult.PASS;
    }
    return mark(owner, hit.getBlockPos())
        .map(
            mark -> {
              owner.sendMessage(Text.translatable(mark.translationKey()), true);
              return ActionResult.SUCCESS;
            })
        .orElse(ActionResult.PASS);
  }

  public static boolean isErrandChest(final World world, final BlockPos pos) {
    final BlockEntity container = world.getBlockEntity(pos);
    return MarkRules.mayMark(
        container instanceof Inventory && world.getBlockState(pos).isIn(ERRAND_CHESTS),
        true,
        !(container instanceof LockableContainerBlockEntity lockable)
            || lockable.lock.equals(ContainerLock.EMPTY));
  }

  public static Optional<ChestMark> mark(final ServerPlayerEntity owner, final BlockPos pos) {
    final World world = owner.getWorld();
    final BlockPos chestPos = oneHalf(world, pos);
    final BlockEntity container = world.getBlockEntity(chestPos);
    if (!MarkRules.mayMark(
        isErrandChest(world, chestPos),
        owner.canModifyAt(world, chestPos),
        !(container instanceof LockableContainerBlockEntity lockable)
            || lockable.checkUnlocked(owner))) {
      return Optional.empty();
    }
    final GlobalPos chest = GlobalPos.create(world.getRegistryKey(), chestPos);
    final FamiliarRegistry registry = FamiliarRegistry.of(owner.getServer());
    return registry
        .bondOf(owner.getUuid())
        .map(
            bond -> {
              final ChestMarks.Marked marked = bond.marks().mark(chest);
              registry.update(owner.getUuid(), bond.withMarks(marked.marks()));
              return marked.mark();
            });
  }

  public static void forget(final MinecraftServer server, final UUID owner, final GlobalPos chest) {
    final FamiliarRegistry registry = FamiliarRegistry.of(server);
    registry
        .bondOf(owner)
        .ifPresent(bond -> registry.update(owner, bond.withMarks(bond.marks().without(chest))));
  }

  private static BlockPos oneHalf(final World world, final BlockPos pos) {
    final BlockState state = world.getBlockState(pos);
    if (!(state.getBlock() instanceof ChestBlock)
        || state.get(ChestBlock.CHEST_TYPE) == ChestType.SINGLE) {
      return pos.toImmutable();
    }
    final BlockPos other = pos.offset(ChestBlock.getFacing(state));
    return (pos.compareTo(other) <= 0 ? pos : other).toImmutable();
  }
}
