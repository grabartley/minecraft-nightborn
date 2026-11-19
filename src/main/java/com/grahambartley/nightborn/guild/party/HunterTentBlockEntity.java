package com.grahambartley.nightborn.guild.party;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HunterTentBlockEntity extends BlockEntity implements GeoBlockEntity {

  static final String PITCHED_KEY = "Pitched";
  static final long UNKNOWN = -1L;
  static final String CONTROLLER = "tent";
  static final int BLEND_TICKS = 4;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final TentClips clips = new TentClips();
  private long pitchedAt = UNKNOWN;

  public HunterTentBlockEntity(final BlockPos pos, final BlockState state) {
    super(TentBlocks.HUNTER_TENT_ENTITY, pos, state);
  }

  public void pitch(final long now) {
    if (pitchedAt == UNKNOWN) {
      pitchedAt = now;
    }
  }

  public long age(final long now) {
    return pitchedAt == UNKNOWN ? Long.MAX_VALUE : now - pitchedAt;
  }

  @Override
  public void setWorld(final World world) {
    super.setWorld(world);
    if (!world.isClient) {
      pitch(world.getTime());
    }
  }

  @Override
  protected void readNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.readNbt(nbt, registries);
    pitchedAt = nbt.contains(PITCHED_KEY) ? nbt.getLong(PITCHED_KEY) : UNKNOWN;
  }

  @Override
  protected void writeNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.writeNbt(nbt, registries);
    nbt.putLong(PITCHED_KEY, pitchedAt);
  }

  @Override
  public NbtCompound toInitialChunkDataNbt(final RegistryWrapper.WrapperLookup registries) {
    return createNbt(registries);
  }

  @Override
  public Packet<ClientPlayPacketListener> toUpdatePacket() {
    return BlockEntityUpdateS2CPacket.create(this);
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            CONTROLLER,
            BLEND_TICKS,
            state -> {
              final long now = world == null ? 0L : world.getTime();
              clips
                  .observe(getCachedState().get(HunterTentBlock.STRUCK), age(now))
                  .ifPresent(
                      names -> state.getController().setAnimation(TentClips.animation(names)));
              return PlayState.CONTINUE;
            }));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
