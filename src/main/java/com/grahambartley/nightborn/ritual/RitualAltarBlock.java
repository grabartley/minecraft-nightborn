package com.grahambartley.nightborn.ritual;

import com.grahambartley.nightborn.core.NightSight;
import com.grahambartley.nightborn.server.ServerConfigService;
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
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class RitualAltarBlock extends BlockWithEntity {

  public static final MapCodec<RitualAltarBlock> CODEC = createCodec(RitualAltarBlock::new);
  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
  public static final EnumProperty<AltarPhase> PHASE = EnumProperty.of("phase", AltarPhase.class);
  private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 14, 16);

  public RitualAltarBlock(final Settings settings) {
    super(settings);
    setDefaultState(getDefaultState().with(FACING, Direction.NORTH).with(PHASE, AltarPhase.IDLE));
  }

  @Override
  protected MapCodec<RitualAltarBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING, PHASE);
  }

  @Override
  public BlockState getPlacementState(final ItemPlacementContext context) {
    return getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
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
    return SHAPE;
  }

  @Override
  protected BlockRenderType getRenderType(final BlockState state) {
    return BlockRenderType.ENTITYBLOCK_ANIMATED;
  }

  @Override
  public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
    return new RitualAltarBlockEntity(pos, state);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
      final World world, final BlockState state, final BlockEntityType<T> type) {
    return world.isClient
        ? null
        : validateTicker(type, RitualBlocks.ALTAR_ENTITY, RitualAltarBlockEntity::serverTick);
  }

  private static AltarUse decide(
      final RitualAltarBlockEntity altar,
      final ItemStack held,
      final PlayerEntity player,
      final World world) {
    return AltarUse.decide(
        new AltarUse.Altar(altar.active(), altar.relics().size(), RitualItems.relicsNeeded()),
        new AltarUse.Hand(
            held.isEmpty(),
            held.isIn(RitualItems.RELICS),
            altar.holds(held.getItem()),
            player.isSneaking()),
        !Vampires.isVampire(player),
        NightSight.isNight(world));
  }

  @Override
  protected ItemActionResult onUseWithItem(
      final ItemStack stack,
      final BlockState state,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final Hand hand,
      final BlockHitResult hit) {
    if (!(world.getBlockEntity(pos) instanceof RitualAltarBlockEntity altar)) {
      return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    final AltarUse use = decide(altar, stack, player, world);
    if (use == AltarUse.BUSY) {
      return ItemActionResult.CONSUME;
    }
    if (use != AltarUse.PLACE) {
      return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    if (!world.isClient) {
      altar.place(stack.splitUnlessCreative(1, player));
      world.playSound(null, pos, RitualSounds.RELIC_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
      world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
    }
    return ItemActionResult.success(world.isClient);
  }

  @Override
  protected ActionResult onUse(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final PlayerEntity player,
      final BlockHitResult hit) {
    if (!(world.getBlockEntity(pos) instanceof RitualAltarBlockEntity altar)) {
      return ActionResult.PASS;
    }
    final AltarUse use = decide(altar, player.getMainHandStack(), player, world);
    if (use == AltarUse.PASS || use == AltarUse.PLACE) {
      return ActionResult.PASS;
    }
    if (world instanceof ServerWorld server) {
      act(use, server, pos, altar, player);
    }
    return ActionResult.success(world.isClient);
  }

  private static void act(
      final AltarUse use,
      final ServerWorld world,
      final BlockPos pos,
      final RitualAltarBlockEntity altar,
      final PlayerEntity player) {
    use.refusal().ifPresent(key -> player.sendMessage(Text.translatable(key), true));
    if (use == AltarUse.TAKE) {
      player.setStackInHand(Hand.MAIN_HAND, altar.take());
      world.playSound(null, pos, RitualSounds.RELIC_TAKE, SoundCategory.BLOCKS, 1.0F, 1.0F);
      world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
    }
    if (use == AltarUse.START) {
      final var settings = ServerConfigService.current().ritual();
      altar.begin(settings.buildUpTicks());
      Ritual.begin(world, pos, settings);
      world.emitGameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
    }
  }

  @Override
  protected void onStateReplaced(
      final BlockState state,
      final World world,
      final BlockPos pos,
      final BlockState newState,
      final boolean moved) {
    if (!state.isOf(newState.getBlock())
        && world.getBlockEntity(pos) instanceof RitualAltarBlockEntity altar) {
      if (altar.active() && world instanceof ServerWorld server) {
        Ritual.lift(server, pos, ServerConfigService.current().ritual().fightRange());
      }
      altar
          .relics()
          .forEach(stack -> ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack));
    }
    super.onStateReplaced(state, world, pos, newState, moved);
  }
}
