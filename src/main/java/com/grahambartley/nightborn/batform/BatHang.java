package com.grahambartley.nightborn.batform;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.network.ReleaseBatHangPayload;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class BatHang {

  public static final AttachmentType<BlockPos> TYPE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "bat_hang"),
          builder ->
              builder
                  .persistent(BlockPos.CODEC)
                  .syncWith(BlockPos.PACKET_CODEC.cast(), AttachmentSyncPredicate.all()));

  private BatHang() {}

  static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        ReleaseBatHangPayload.ID, (payload, context) -> release(context.player()));
  }

  public static boolean isHanging(final Entity entity) {
    return entity.hasAttached(TYPE);
  }

  public static Optional<BlockPos> anchor(final Entity entity) {
    return Optional.ofNullable(entity.getAttached(TYPE));
  }

  static ActionResult use(final PlayerEntity player, final World world, final BlockHitResult hit) {
    final BlockPos anchor = hit.getBlockPos();
    final Vec3d spot = BatHangRules.spot(anchor);
    if (!BatHangRules.reaches(
        hit.getSide() == Direction.DOWN,
        isHanging(player),
        player.getBoundingBox().maxY,
        anchor.getY(),
        Math.max(Math.abs(player.getX() - spot.x), Math.abs(player.getZ() - spot.z)))) {
      return ActionResult.FAIL;
    }
    if (world.isClient) {
      return ActionResult.SUCCESS;
    }
    return player instanceof ServerPlayerEntity server && hang(server, anchor)
        ? ActionResult.SUCCESS
        : ActionResult.FAIL;
  }

  public static boolean hang(final ServerPlayerEntity player, final BlockPos anchor) {
    final Vec3d spot = BatHangRules.spot(anchor);
    if (!holds(player.getWorld(), anchor)
        || !player
            .getWorld()
            .isSpaceEmpty(
                player,
                BatHangRules.sweep(player.getBoundingBox(), BatForm.dimensions().getBoxAt(spot)))) {
      return false;
    }
    player.requestTeleport(spot.x, spot.y, spot.z);
    player.setVelocity(Vec3d.ZERO);
    player.setAttached(TYPE, anchor);
    return true;
  }

  public static void release(final Entity entity) {
    if (isHanging(entity)) {
      entity.removeAttached(TYPE);
    }
  }

  static void tick(final ServerPlayerEntity player) {
    anchor(player)
        .filter(anchor -> !holds(player.getWorld(), anchor))
        .ifPresent(anchor -> release(player));
  }

  public static void afterMove(final ServerPlayerEntity player) {
    anchor(player)
        .filter(anchor -> BatHangRules.slips(player.getPos().distanceTo(BatHangRules.spot(anchor))))
        .ifPresent(anchor -> release(player));
  }

  private static boolean holds(final World world, final BlockPos anchor) {
    return world.getBlockState(anchor).isSideSolidFullSquare(world, anchor, Direction.DOWN);
  }
}
