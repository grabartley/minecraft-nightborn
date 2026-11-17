package com.grahambartley.nightborn.ritual;

import com.grahambartley.nightborn.config.RitualSettings;
import com.grahambartley.nightborn.mob.ClipCache;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RitualAltarBlockEntity extends BlockEntity implements GeoBlockEntity {

  static final String RELICS_KEY = "Relics";
  static final String ELAPSED_KEY = "Elapsed";
  static final String TOTAL_KEY = "Total";
  static final int IDLE = -1;
  private static final int BLEND_TICKS = 6;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final List<ItemStack> relics = new ArrayList<>();
  private int elapsed = IDLE;
  private int total;

  public RitualAltarBlockEntity(final BlockPos pos, final BlockState state) {
    super(RitualBlocks.ALTAR_ENTITY, pos, state);
  }

  public List<ItemStack> relics() {
    return List.copyOf(relics);
  }

  public boolean holds(final Item relic) {
    return relics.stream().anyMatch(stack -> stack.isOf(relic));
  }

  public boolean active() {
    return elapsed != IDLE;
  }

  public void place(final ItemStack relic) {
    relics.add(relic);
    changed();
  }

  public ItemStack take() {
    final ItemStack taken = relics.isEmpty() ? ItemStack.EMPTY : relics.removeLast();
    changed();
    return taken;
  }

  public void begin(final int buildUpTicks) {
    total = buildUpTicks;
    elapsed = 0;
    changed();
  }

  public static void serverTick(
      final World world,
      final BlockPos pos,
      final BlockState state,
      final RitualAltarBlockEntity altar) {
    if (!altar.active() || !(world instanceof ServerWorld server)) {
      return;
    }
    final RitualBuildUp at = new RitualBuildUp(altar.elapsed, altar.total);
    if (!at.done()) {
      Ritual.tick(server, pos, at);
      altar.elapsed++;
      altar.markDirty();
      return;
    }
    final RitualSettings settings = ServerConfigService.current().ritual();
    if (Ritual.summon(server, pos, state.get(RitualAltarBlock.FACING), settings)) {
      altar.relics.clear();
    }
    altar.elapsed = IDLE;
    altar.changed();
  }

  private void changed() {
    markDirty();
    if (world == null) {
      return;
    }
    final BlockState state = getCachedState();
    final BlockState next =
        state.with(RitualAltarBlock.PHASE, AltarPhase.of(active(), relics.size()));
    if (next != state) {
      world.setBlockState(pos, next, Block.NOTIFY_ALL);
    }
    world.updateListeners(pos, state, next, Block.NOTIFY_LISTENERS);
  }

  @Override
  protected void readNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.readNbt(nbt, registries);
    relics.clear();
    for (final NbtElement element : nbt.getList(RELICS_KEY, NbtElement.COMPOUND_TYPE)) {
      ItemStack.fromNbt(registries, element).ifPresent(relics::add);
    }
    elapsed = nbt.contains(ELAPSED_KEY) ? nbt.getInt(ELAPSED_KEY) : IDLE;
    total = nbt.getInt(TOTAL_KEY);
  }

  @Override
  protected void writeNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.writeNbt(nbt, registries);
    final NbtList list = new NbtList();
    relics.forEach(stack -> list.add(stack.encode(registries)));
    nbt.put(RELICS_KEY, list);
    nbt.putInt(ELAPSED_KEY, elapsed);
    nbt.putInt(TOTAL_KEY, total);
  }

  @Override
  public Packet<ClientPlayPacketListener> toUpdatePacket() {
    return BlockEntityUpdateS2CPacket.create(this);
  }

  @Override
  public NbtCompound toInitialChunkDataNbt(final RegistryWrapper.WrapperLookup registries) {
    return createNbt(registries);
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "phase",
            BLEND_TICKS,
            state ->
                state.setAndContinue(
                    ClipCache.loop(getCachedState().get(RitualAltarBlock.PHASE).clip()))));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
