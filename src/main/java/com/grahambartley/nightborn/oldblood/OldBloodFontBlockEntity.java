package com.grahambartley.nightborn.oldblood;

import com.grahambartley.nightborn.decor.ToggleClips;
import com.grahambartley.nightborn.house.Houses;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class OldBloodFontBlockEntity extends BlockEntity implements GeoBlockEntity {

  static final String PROGRESS_KEY = "Refill";
  static final String UPDATED_KEY = "Updated";
  static final int CHECK_TICKS = 100;
  static final int HOUSEHOLD_REACH = 64;
  static final long UNKNOWN = -1L;

  static final String CONTROLLER = "level";
  static final int BLEND_TICKS = 10;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final FontClips clips = new FontClips();
  private long progress = FontRefill.FULL;
  private long updatedAt = UNKNOWN;

  public OldBloodFontBlockEntity(final BlockPos pos, final BlockState state) {
    super(OldBloodBlocks.FONT_ENTITY, pos, state);
  }

  public boolean full() {
    return FontRefill.full(progress);
  }

  public long progress() {
    return progress;
  }

  public void drain(final long now) {
    progress = 0L;
    updatedAt = now;
    show();
  }

  public static void serverTick(
      final World world,
      final BlockPos pos,
      final BlockState state,
      final OldBloodFontBlockEntity font) {
    if (world.getTime() % CHECK_TICKS == 0) {
      font.refill(world);
    }
  }

  public void refill(final World world) {
    final long now = world.getTime();
    if (full()) {
      updatedAt = now;
      return;
    }
    final long elapsed = updatedAt == UNKNOWN ? 0L : now - updatedAt;
    updatedAt = now;
    progress =
        FontRefill.advance(
            progress,
            elapsed,
            householdLives(world),
            ServerConfigService.current().turning().oldBloodRefillDays());
    show();
  }

  private boolean householdLives(final World world) {
    if (world instanceof ServerWorld server
        && Houses.get(server.getServer())
            .seatNear(GlobalPos.create(world.getRegistryKey(), pos), HOUSEHOLD_REACH)) {
      return true;
    }
    return !world
        .getEntitiesByClass(
            VampireEntity.class,
            new Box(pos).expand(HOUSEHOLD_REACH),
            vampire ->
                vampire.isAlive()
                    && vampire
                        .homeLife()
                        .home()
                        .map(home -> home.isWithinDistance(pos, HOUSEHOLD_REACH))
                        .orElse(false))
        .isEmpty();
  }

  private void show() {
    markDirty();
    if (world == null) {
      return;
    }
    final BlockState state = getCachedState();
    final FontLevel level =
        FontLevel.of(progress, ServerConfigService.current().turning().oldBloodRefillDays());
    if (state.get(OldBloodFontBlock.LEVEL) != level) {
      world.setBlockState(pos, state.with(OldBloodFontBlock.LEVEL, level), Block.NOTIFY_ALL);
    }
  }

  @Override
  protected void readNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.readNbt(nbt, registries);
    progress = nbt.contains(PROGRESS_KEY) ? nbt.getLong(PROGRESS_KEY) : FontRefill.FULL;
    updatedAt = nbt.contains(UPDATED_KEY) ? nbt.getLong(UPDATED_KEY) : UNKNOWN;
  }

  @Override
  protected void writeNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.writeNbt(nbt, registries);
    nbt.putLong(PROGRESS_KEY, progress);
    nbt.putLong(UPDATED_KEY, updatedAt);
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            CONTROLLER,
            BLEND_TICKS,
            state -> {
              clips
                  .observe(getCachedState().get(OldBloodFontBlock.LEVEL))
                  .ifPresent(
                      names -> state.getController().setAnimation(ToggleClips.sequence(names)));
              return PlayState.CONTINUE;
            }));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
