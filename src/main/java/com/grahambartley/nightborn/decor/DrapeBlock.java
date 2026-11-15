package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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
import net.minecraft.world.event.GameEvent;

public class DrapeBlock extends BlockWithEntity implements AnimatedFurniture {

  public static final MapCodec<DrapeBlock> CODEC = createCodec(DrapeBlock::new);
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final BooleanProperty OPEN = Properties.OPEN;
  public static final BooleanProperty TOP = BooleanProperty.of("top");
  public static final String ROD_BONE = "rod";

  private static final Map<Direction, VoxelShape> CLOSED =
      FacingShapes.fromNorth(Block.createCuboidShape(0, 0, 13, 16, 16, 16));
  private static final Map<Direction, VoxelShape> OPENED =
      FacingShapes.fromNorth(
          VoxelShapes.union(
              Block.createCuboidShape(0, 0, 13, 4, 16, 16),
              Block.createCuboidShape(12, 0, 13, 16, 16, 16)));

  public DrapeBlock(final Settings settings) {
    super(settings);
    this.setDefaultState(
        this.getStateManager()
            .getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(OPEN, false)
            .with(TOP, true));
  }

  @Override
  protected MapCodec<? extends BlockWithEntity> getCodec() {
    return CODEC;
  }

  @Override
  public BooleanProperty toggleProperty() {
    return OPEN;
  }

  @Override
  public String artModel() {
    return "velvet_drape";
  }

  @Override
  public Set<String> hiddenBones(final BlockState state) {
    return state.get(TOP) ? Set.of() : Set.of(ROD_BONE);
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING, OPEN, TOP);
  }

  @Override
  public BlockState getPlacementState(final ItemPlacementContext context) {
    final BlockState state =
        this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    final BlockState above = context.getWorld().getBlockState(context.getBlockPos().up());
    return state.with(TOP, !this.continues(state, above));
  }

  @Override
  protected BlockState getStateForNeighborUpdate(
      final BlockState state,
      final Direction direction,
      final BlockState neighborState,
      final WorldAccess world,
      final BlockPos pos,
      final BlockPos neighborPos) {
    if (direction == Direction.UP) {
      return state.with(TOP, !this.continues(state, neighborState));
    }
    return state;
  }

  @Override
  protected BlockState rotate(final BlockState state, final BlockRotation rotation) {
    return state.with(FACING, rotation.rotate(state.get(FACING)));
  }

  @Override
  protected BlockState mirror(final BlockState state, final BlockMirror mirror) {
    return state.rotate(mirror.getRotation(state.get(FACING)));
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return (state.get(OPEN) ? OPENED : CLOSED).get(state.get(FACING));
  }

  @Override
  protected BlockRenderType getRenderType(final BlockState state) {
    return BlockRenderType.ENTITYBLOCK_ANIMATED;
  }

  @Override
  public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
    return new FurnitureBlockEntity(pos, state);
  }

  @Override
  protected ActionResult onUse(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final BlockHitResult hit) {
    if (!world.isClient) {
      final boolean open = !state.get(OPEN);
      for (final int offset :
          DrapeColumn.offsets(dy -> this.continues(state, world.getBlockState(pos.up(dy))))) {
        final BlockPos at = pos.up(offset);
        world.setBlockState(
            at, world.getBlockState(at).with(OPEN, open), Block.NOTIFY_ALL_AND_REDRAW);
      }
      world.playSound(
          null,
          pos,
          open ? SoundEvents.BLOCK_WOOL_BREAK : SoundEvents.BLOCK_WOOL_PLACE,
          SoundCategory.BLOCKS,
          0.8f,
          open ? 0.8f : 0.7f);
      world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
    }
    return ActionResult.success(world.isClient);
  }

  private boolean continues(final BlockState self, final BlockState other) {
    return other.isOf(this) && other.get(FACING) == self.get(FACING);
  }
}
