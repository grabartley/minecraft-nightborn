package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
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

  private static boolean mayMark(final ServerPlayerEntity owner, final BlockPos pos) {
    final World world = owner.getWorld();
    final BlockEntity container = world.getBlockEntity(pos);
    return MarkRules.mayMark(
        container instanceof Inventory && world.getBlockState(pos).isIn(ERRAND_CHESTS),
        owner.canModifyAt(world, pos),
        !(container instanceof LockableContainerBlockEntity lockable)
            || lockable.checkUnlocked(owner));
  }

  public static Optional<ChestMark> mark(final ServerPlayerEntity owner, final BlockPos pos) {
    if (!mayMark(owner, pos)) {
      return Optional.empty();
    }
    final GlobalPos chest = GlobalPos.create(owner.getWorld().getRegistryKey(), pos.toImmutable());
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
}
