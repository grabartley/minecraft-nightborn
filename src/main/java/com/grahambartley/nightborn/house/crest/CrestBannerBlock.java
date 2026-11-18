package com.grahambartley.nightborn.house.crest;

import com.grahambartley.nightborn.decor.FacingShapes;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class CrestBannerBlock extends BlockWithEntity {

  public static final MapCodec<CrestBannerBlock> CODEC = createCodec(CrestBannerBlock::new);
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  private static final Map<Direction, VoxelShape> SHAPES =
      FacingShapes.fromNorth(Block.createCuboidShape(0, 0, 11, 16, 16, 16));

  public CrestBannerBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH));
  }

  @Override
  protected MapCodec<CrestBannerBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  protected BlockRenderType getRenderType(final BlockState state) {
    return BlockRenderType.ENTITYBLOCK_ANIMATED;
  }

  @Override
  public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
    return new CrestBannerBlockEntity(pos, state);
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return SHAPES.get(state.get(FACING));
  }

  @Override
  protected VoxelShape getCollisionShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return VoxelShapes.empty();
  }

  static Direction support(final BlockState state) {
    return state.get(FACING).getOpposite();
  }

  @Override
  protected boolean canPlaceAt(final BlockState state, final WorldView world, final BlockPos pos) {
    final Direction support = support(state);
    final BlockPos behind = pos.offset(support);
    return world.getBlockState(behind).isSideSolidFullSquare(world, behind, support.getOpposite());
  }

  @Override
  @Nullable
  public BlockState getPlacementState(final ItemPlacementContext context) {
    for (final Direction direction : context.getPlacementDirections()) {
      if (direction.getAxis().isHorizontal()) {
        final BlockState state = this.getDefaultState().with(FACING, direction.getOpposite());
        if (state.canPlaceAt(context.getWorld(), context.getBlockPos())) {
          return state;
        }
      }
    }
    return null;
  }

  @Override
  protected BlockState getStateForNeighborUpdate(
      final BlockState state,
      final Direction direction,
      final BlockState neighborState,
      final WorldAccess world,
      final BlockPos pos,
      final BlockPos neighborPos) {
    return direction == support(state) && !state.canPlaceAt(world, pos)
        ? Blocks.AIR.getDefaultState()
        : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
  }

  @Override
  public void onPlaced(
      final World world,
      final BlockPos pos,
      final BlockState state,
      @Nullable final LivingEntity placer,
      final ItemStack stack) {
    super.onPlaced(world, pos, state, placer, stack);
    if (placer instanceof ServerPlayerEntity player
        && world.getBlockEntity(pos) instanceof CrestBannerBlockEntity banner
        && banner.crest().isEmpty()) {
      HouseCrests.of(player).ifPresent(banner::show);
    }
  }

  @Override
  protected ActionResult onUse(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final BlockHitResult hit) {
    if (!(world.getBlockEntity(pos) instanceof CrestBannerBlockEntity banner)) {
      return ActionResult.PASS;
    }
    if (!(player instanceof ServerPlayerEntity server)) {
      return ActionResult.SUCCESS;
    }
    return HouseCrests.of(server)
        .filter(crest -> banner.crest().filter(crest::equals).isEmpty())
        .map(
            crest -> {
              banner.show(crest);
              HouseCrests.cue(server, pos);
              return ActionResult.SUCCESS;
            })
        .orElse(ActionResult.PASS);
  }

  @Override
  protected BlockState rotate(final BlockState state, final BlockRotation rotation) {
    return state.with(FACING, rotation.rotate(state.get(FACING)));
  }

  @Override
  protected BlockState mirror(final BlockState state, final BlockMirror mirror) {
    return state.rotate(mirror.getRotation(state.get(FACING)));
  }
}
