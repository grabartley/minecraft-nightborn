package com.grahambartley.nightborn.ritual;

import com.grahambartley.nightborn.config.RitualSettings;
import com.grahambartley.nightborn.decor.ChaliceContents;
import com.grahambartley.nightborn.mob.ClipCache;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RitualAltarBlockEntity extends BlockEntity implements GeoBlockEntity {

  static final String RELICS_KEY = "Relics";
  static final String CHALICE_KEY = "Chalice";
  static final String SUMMONER_KEY = "Summoner";
  static final String ELAPSED_KEY = "Elapsed";
  static final String TOTAL_KEY = "Total";
  static final int IDLE = -1;
  private static final int BLEND_TICKS = 6;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final List<ItemStack> oldRelics = new ArrayList<>();
  @Nullable private ChaliceContents chalice;
  @Nullable private UUID summoner;
  private int elapsed = IDLE;
  private int total;

  public RitualAltarBlockEntity(final BlockPos pos, final BlockState state) {
    super(RitualBlocks.ALTAR_ENTITY, pos, state);
  }

  public Optional<ChaliceContents> chalice() {
    return Optional.ofNullable(chalice);
  }

  public List<ItemStack> oldRelics() {
    return List.copyOf(oldRelics);
  }

  public boolean active() {
    return elapsed != IDLE;
  }

  public Optional<UUID> summoner() {
    return Optional.ofNullable(summoner);
  }

  public void setChalice(final ChaliceContents contents) {
    chalice = contents;
    changed();
  }

  public Optional<ChaliceContents> takeChalice() {
    final Optional<ChaliceContents> taken = chalice();
    chalice = null;
    changed();
    return taken;
  }

  public void begin(final int buildUpTicks, @Nullable final UUID caller) {
    total = buildUpTicks;
    elapsed = 0;
    summoner = caller;
    changed();
  }

  public static void serverTick(
      final World world,
      final BlockPos pos,
      final BlockState state,
      final RitualAltarBlockEntity altar) {
    if (!(world instanceof ServerWorld server)) {
      return;
    }
    altar.dropOldRelics(server);
    if (!altar.active()) {
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
    if (Ritual.summon(server, pos, state.get(RitualAltarBlock.FACING), settings, altar.summoner)) {
      altar.chalice = altar.chalice == null ? null : ChaliceContents.EMPTY;
    }
    altar.elapsed = IDLE;
    altar.summoner = null;
    altar.changed();
  }

  private void dropOldRelics(final ServerWorld server) {
    if (oldRelics.isEmpty()) {
      return;
    }
    oldRelics.forEach(
        relic -> ItemScatterer.spawn(server, pos.getX(), pos.getY() + 1, pos.getZ(), relic));
    oldRelics.clear();
    markDirty();
  }

  private void changed() {
    markDirty();
    if (world == null) {
      return;
    }
    final BlockState state = getCachedState();
    final BlockState next =
        state.with(
            RitualAltarBlock.PHASE, AltarPhase.of(active(), chalice != null && chalice.full()));
    if (next != state) {
      world.setBlockState(pos, next, Block.NOTIFY_ALL);
    }
    world.updateListeners(pos, state, next, Block.NOTIFY_LISTENERS);
  }

  @Override
  protected void readNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.readNbt(nbt, registries);
    oldRelics.clear();
    for (final NbtElement element : nbt.getList(RELICS_KEY, NbtElement.COMPOUND_TYPE)) {
      ItemStack.fromNbt(registries, element).ifPresent(oldRelics::add);
    }
    chalice =
        nbt.contains(CHALICE_KEY)
            ? ChaliceContents.CODEC
                .parse(NbtOps.INSTANCE, nbt.get(CHALICE_KEY))
                .result()
                .orElse(null)
            : null;
    summoner = nbt.containsUuid(SUMMONER_KEY) ? nbt.getUuid(SUMMONER_KEY) : null;
    elapsed = nbt.contains(ELAPSED_KEY) ? nbt.getInt(ELAPSED_KEY) : IDLE;
    total = nbt.getInt(TOTAL_KEY);
  }

  @Override
  protected void writeNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.writeNbt(nbt, registries);
    if (!oldRelics.isEmpty()) {
      final NbtList list = new NbtList();
      oldRelics.forEach(stack -> list.add(stack.encode(registries)));
      nbt.put(RELICS_KEY, list);
    }
    if (chalice != null) {
      ChaliceContents.CODEC
          .encodeStart(NbtOps.INSTANCE, chalice)
          .result()
          .ifPresent(encoded -> nbt.put(CHALICE_KEY, encoded));
    }
    if (summoner != null) {
      nbt.putUuid(SUMMONER_KEY, summoner);
    }
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
