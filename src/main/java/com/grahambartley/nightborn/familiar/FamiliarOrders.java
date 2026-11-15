package com.grahambartley.nightborn.familiar;

import java.util.Optional;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public final class FamiliarOrders {

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
        || !player.getOffHandStack().isEmpty()
        || !(world.getBlockEntity(hit.getBlockPos()) instanceof Inventory)) {
      return ActionResult.PASS;
    }
    return mark(owner, GlobalPos.create(world.getRegistryKey(), hit.getBlockPos()))
        .map(
            mark -> {
              owner.sendMessage(Text.translatable(mark.translationKey()), true);
              return ActionResult.SUCCESS;
            })
        .orElse(ActionResult.PASS);
  }

  public static Optional<ChestMark> mark(final ServerPlayerEntity owner, final GlobalPos chest) {
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
