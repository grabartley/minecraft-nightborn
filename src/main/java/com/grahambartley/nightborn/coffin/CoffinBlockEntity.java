package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.decor.ToggleClips;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CoffinBlockEntity extends BlockEntity implements GeoBlockEntity {

  public static final ToggleClips.Names LID_CLIPS =
      new ToggleClips.Names("open", "opened", "close", "closed");

  static final String OWNER_KEY = "Owner";

  private static final String CONTROLLER = "lid";

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final ToggleClips clips = new ToggleClips(LID_CLIPS);
  private final LidMotion lid = new LidMotion();
  @Nullable private UUID owner;

  public CoffinBlockEntity(final BlockPos pos, final BlockState state) {
    super(Coffins.BLOCK_ENTITY, pos, state);
  }

  @Nullable
  public UUID owner() {
    return this.owner;
  }

  public void setOwner(@Nullable final UUID owner) {
    this.owner = owner;
    markDirty();
  }

  @Override
  protected void writeNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.writeNbt(nbt, registries);
    if (this.owner != null) {
      nbt.putUuid(OWNER_KEY, this.owner);
    }
  }

  @Override
  protected void readNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.readNbt(nbt, registries);
    this.owner = nbt.containsUuid(OWNER_KEY) ? nbt.getUuid(OWNER_KEY) : null;
  }

  public void clientTick(final BlockState state) {
    this.lid.tick(state.get(CoffinBlock.OPEN));
  }

  public float openness(final float tickDelta) {
    return this.lid.openness(tickDelta);
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            CONTROLLER,
            0,
            state -> {
              this.clips
                  .observe(this.getCachedState().get(CoffinBlock.OPEN))
                  .ifPresent(
                      names -> state.getController().setAnimation(ToggleClips.sequence(names)));
              return PlayState.CONTINUE;
            }));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return this.cache;
  }
}
