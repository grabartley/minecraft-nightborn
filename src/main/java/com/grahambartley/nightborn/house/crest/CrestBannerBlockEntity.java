package com.grahambartley.nightborn.house.crest;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CrestBannerBlockEntity extends BlockEntity implements GeoBlockEntity {

  static final String CREST_KEY = "crest";
  private static final String CONTROLLER = "cloth";
  private static final RawAnimation SWAY = RawAnimation.begin().thenLoop("sway");

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private Optional<Crest> crest = Optional.empty();

  public CrestBannerBlockEntity(final BlockPos pos, final BlockState state) {
    super(CrestBanners.BLOCK_ENTITY, pos, state);
  }

  public Optional<Crest> crest() {
    return crest;
  }

  public void show(final Crest shown) {
    this.crest = Optional.of(shown);
    this.markDirty();
    if (this.world != null) {
      this.world.updateListeners(
          this.pos, this.getCachedState(), this.getCachedState(), Block.NOTIFY_LISTENERS);
    }
  }

  public void preview(final Optional<Crest> shown) {
    this.crest = shown;
  }

  @Override
  protected void readNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.readNbt(nbt, registries);
    this.crest =
        nbt.contains(CREST_KEY)
            ? Crest.CODEC
                .parse(NbtOps.INSTANCE, nbt.get(CREST_KEY))
                .resultOrPartial(
                    error -> Nightborn.LOGGER.warn("Skipping a damaged crest: {}", error))
            : Optional.empty();
  }

  @Override
  protected void writeNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.writeNbt(nbt, registries);
    this.crest
        .flatMap(shown -> Crest.CODEC.encodeStart(NbtOps.INSTANCE, shown).result())
        .ifPresent(encoded -> nbt.put(CREST_KEY, encoded));
  }

  @Override
  protected void readComponents(final BlockEntity.ComponentsAccess components) {
    super.readComponents(components);
    this.crest = Optional.ofNullable(components.get(CrestBanners.CREST));
  }

  @Override
  protected void addComponents(final ComponentMap.Builder builder) {
    super.addComponents(builder);
    this.crest.ifPresent(shown -> builder.add(CrestBanners.CREST, shown));
  }

  @Override
  public void removeFromCopiedStackNbt(final NbtCompound nbt) {
    nbt.remove(CREST_KEY);
  }

  @Override
  public NbtCompound toInitialChunkDataNbt(final RegistryWrapper.WrapperLookup registries) {
    return this.createNbt(registries);
  }

  @Override
  public Packet<ClientPlayPacketListener> toUpdatePacket() {
    return BlockEntityUpdateS2CPacket.create(this);
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(this, CONTROLLER, 0, state -> state.setAndContinue(SWAY)));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }
}
