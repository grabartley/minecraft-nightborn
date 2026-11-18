package com.grahambartley.nightborn.oldblood;

import com.grahambartley.nightborn.journal.Journal;
import com.grahambartley.nightborn.journal.Journals;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import com.grahambartley.nightborn.vampire.Vampires;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class OldBloodFontBlock extends BlockWithEntity {

  public static final MapCodec<OldBloodFontBlock> CODEC = createCodec(OldBloodFontBlock::new);
  public static final BooleanProperty FILLED = BooleanProperty.of("filled");
  private static final VoxelShape SHAPE = Block.createCuboidShape(1, 0, 1, 15, 16, 15);

  public OldBloodFontBlock(final Settings settings) {
    super(settings);
    setDefaultState(getDefaultState().with(FILLED, true));
  }

  @Override
  protected MapCodec<OldBloodFontBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FILLED);
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return SHAPE;
  }

  @Override
  protected BlockRenderType getRenderType(final BlockState state) {
    return BlockRenderType.MODEL;
  }

  @Override
  public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
    return new OldBloodFontBlockEntity(pos, state);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      final World world, final BlockState state, final BlockEntityType<T> type) {
    return world.isClient
        ? null
        : validateTicker(type, OldBloodBlocks.FONT_ENTITY, OldBloodFontBlockEntity::serverTick);
  }

  @Override
  protected ActionResult onUse(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final BlockHitResult hit) {
    if (!(world.getBlockEntity(pos) instanceof OldBloodFontBlockEntity font)) {
      return ActionResult.PASS;
    }
    if (!world.isClient) {
      font.refill(world);
    }
    final FontDrink drink =
        FontDrink.decide(
            player.getMainHandStack().isEmpty(),
            Vampires.isVampire(player),
            world.getBlockState(pos).get(FILLED),
            BiteSicknessAttachment.isSick(player),
            Journals.carries(player, Journal.VAMPIRE));
    if (drink == FontDrink.PASS) {
      return ActionResult.PASS;
    }
    if (player instanceof ServerPlayerEntity human) {
      drink.refusal().ifPresent(key -> human.sendMessage(Text.translatable(key), true));
      if (drink == FontDrink.JOURNAL) {
        Journals.give(human, Journal.VAMPIRE);
      }
      if (drink == FontDrink.DRINKS && BiteSickness.startFromOldBlood(human)) {
        font.drain(world.getTime());
        world.playSound(
            null, pos, SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.BLOCKS, 1.0F, 0.6F);
        world.emitGameEvent(human, GameEvent.FLUID_PICKUP, pos);
      }
    }
    return ActionResult.success(world.isClient);
  }
}
