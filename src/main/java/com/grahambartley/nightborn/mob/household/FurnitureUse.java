package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.decor.ChairBlock;
import com.grahambartley.nightborn.decor.DrapeBlock;
import com.grahambartley.nightborn.decor.FlameToggle;
import com.grahambartley.nightborn.decor.LightableFurnitureBlock;
import com.grahambartley.nightborn.gramophone.GramophoneBlockEntity;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CandleCakeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;

public final class FurnitureUse {

  private FurnitureUse() {}

  public static boolean seatFree(final ServerWorld world, final BlockPos pos) {
    return world.getBlockState(pos).getBlock() instanceof ChairBlock
        && !ChairBlock.taken(world, pos);
  }

  public static void sit(final ServerWorld world, final BlockPos pos, final Entity sitter) {
    ChairBlock.seat(world, pos, world.getBlockState(pos), sitter);
  }

  public static boolean is(final ServerWorld world, final BlockPos pos, final FurnitureKind kind) {
    return FurnitureKind.of(world.getBlockState(pos)).filter(kind::equals).isPresent();
  }

  public static boolean gramophoneReady(final ServerWorld world, final BlockPos pos) {
    return world.getBlockEntity(pos) instanceof GramophoneBlockEntity gramophone
        && gramophone.ready();
  }

  public static boolean startGramophone(final ServerWorld world, final BlockPos pos) {
    return world.getBlockEntity(pos) instanceof GramophoneBlockEntity gramophone
        && gramophone.play();
  }

  public static boolean needs(final ServerWorld world, final BlockPos pos, final Chore chore) {
    final BlockState state = world.getBlockState(pos);
    return switch (chore) {
      case LIGHT -> is(world, pos, FurnitureKind.LIGHT) && lightable(state);
      case DOUSE -> is(world, pos, FurnitureKind.LIGHT) && lit(state);
      case OPEN_DRAPES -> is(world, pos, FurnitureKind.DRAPE) && !open(state);
      case CLOSE_DRAPES -> is(world, pos, FurnitureKind.DRAPE) && open(state);
    };
  }

  public static boolean lightable(final BlockState state) {
    return state.getBlock() instanceof LightableFurnitureBlock
        ? !lit(state)
        : CandleBlock.canBeLit(state) || CandleCakeBlock.canBeLit(state);
  }

  public static boolean lit(final BlockState state) {
    return state.contains(Properties.LIT) && state.get(Properties.LIT);
  }

  public static boolean open(final BlockState state) {
    return state.contains(Properties.OPEN) && state.get(Properties.OPEN);
  }

  public static void tend(
      final ServerWorld world, final BlockPos pos, final Entity actor, final Chore chore) {
    final BlockState state = world.getBlockState(pos);
    switch (chore) {
      case LIGHT, DOUSE -> setLit(world, pos, state, actor, chore.lights());
      case OPEN_DRAPES, CLOSE_DRAPES -> {
        if (state.getBlock() instanceof DrapeBlock drape && open(state) != chore.opens()) {
          drape.draw(world, pos, state, actor, chore.opens());
        }
      }
    }
  }

  private static void setLit(
      final ServerWorld world,
      final BlockPos pos,
      final BlockState state,
      final Entity actor,
      final boolean light) {
    if (!state.contains(Properties.LIT) || lit(state) == light) {
      return;
    }
    if (state.getBlock() instanceof LightableFurnitureBlock furniture) {
      furniture.apply(
          world, pos, state, actor, light ? FlameToggle.LIGHT_WITH_FLINT : FlameToggle.DOUSE);
      return;
    }
    if (!light) {
      AbstractCandleBlock.extinguish(null, state, world, pos);
      return;
    }
    if (lightable(state)) {
      world.setBlockState(pos, state.with(Properties.LIT, true), Block.NOTIFY_ALL_AND_REDRAW);
      world.playSound(
          null,
          pos,
          SoundEvents.ITEM_FLINTANDSTEEL_USE,
          SoundCategory.BLOCKS,
          1.0f,
          world.getRandom().nextFloat() * 0.4f + 0.8f);
      world.emitGameEvent(actor, GameEvent.BLOCK_CHANGE, pos);
    }
  }
}
