package com.grahambartley.nightborn.gramophone;

import com.grahambartley.nightborn.config.StructureSettings;
import com.grahambartley.nightborn.decor.DecorBlockEntities;
import com.grahambartley.nightborn.decor.ToggleClips;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.jukebox.JukeboxManager;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SingleStackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Clearable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GramophoneBlockEntity extends BlockEntity
    implements Clearable, SingleStackInventory.SingleStackBlockEntityInventory, GeoBlockEntity {

  public static final String RECORD_KEY = "RecordItem";
  static final String TICKS_KEY = "ticks_since_song_started";
  public static final String AMBIENT_KEY = "Ambient";
  static final String WAIT_KEY = "AmbientWait";
  private static final String CONTROLLER = "disc";
  private static final double DROP_HEIGHT = 1.01;
  private static final float DROP_SPREAD = 0.7F;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final GramophoneClips clips = new GramophoneClips();
  private final JukeboxManager manager = new JukeboxManager(this::onManagerChange, this.getPos());
  private ItemStack record = ItemStack.EMPTY;
  private boolean ambient;
  private int wait = GramophoneAmbience.UNSET;

  public GramophoneBlockEntity(final BlockPos pos, final BlockState state) {
    super(DecorBlockEntities.GRAMOPHONE, pos, state);
  }

  public boolean playing() {
    return this.manager.isPlaying();
  }

  public boolean ready() {
    return !this.record.isEmpty() && !this.manager.isPlaying();
  }

  public boolean ambient() {
    return this.ambient;
  }

  public boolean play() {
    if (this.world == null || !this.ready()) {
      return false;
    }
    final Optional<RegistryEntry<JukeboxSong>> song = this.song();
    song.ifPresent(entry -> this.manager.startPlaying(this.world, entry));
    return song.isPresent();
  }

  private Optional<RegistryEntry<JukeboxSong>> song() {
    return this.world == null
        ? Optional.empty()
        : JukeboxSong.getSongEntryFromStack(this.world.getRegistryManager(), this.record);
  }

  public void dropRecord() {
    if (this.world == null || this.world.isClient || this.record.isEmpty()) {
      return;
    }
    final ItemStack dropped = this.emptyStack();
    final Vec3d at =
        Vec3d.add(this.pos, 0.5, DROP_HEIGHT, 0.5).addRandom(this.world.random, DROP_SPREAD);
    final ItemEntity item = new ItemEntity(this.world, at.getX(), at.getY(), at.getZ(), dropped);
    item.setToDefaultPickupDelay();
    this.world.spawnEntity(item);
  }

  public int comparatorOutput() {
    return this.song().map(RegistryEntry::value).map(JukeboxSong::comparatorOutput).orElse(0);
  }

  public static void tick(
      final World world,
      final BlockPos pos,
      final BlockState state,
      final GramophoneBlockEntity gramophone) {
    gramophone.manager.tick(world, state);
    gramophone.syncPlaying();
    if (gramophone.ambient && world instanceof ServerWorld server) {
      gramophone.tickAmbience(server);
    }
  }

  private void tickAmbience(final ServerWorld server) {
    if (this.manager.isPlaying() || server.getTime() % GramophoneAmbience.CHECK_TICKS != 0) {
      return;
    }
    final StructureSettings settings = ServerConfigService.current().structures();
    final boolean near =
        server.isPlayerInRange(
            this.pos.getX() + 0.5,
            this.pos.getY() + 0.5,
            this.pos.getZ() + 0.5,
            settings.gramophoneRange());
    final int before = this.wait;
    this.wait =
        GramophoneAmbience.step(
            before, near, settings.gramophoneSeconds(), server.getRandom().nextDouble());
    if (this.wait != before) {
      this.markDirty();
    }
    if (GramophoneAmbience.due(this.wait)) {
      this.wait = GramophoneAmbience.UNSET;
      this.play();
    }
  }

  private void onManagerChange() {
    if (this.world == null) {
      return;
    }
    this.world.updateNeighborsAlways(this.pos, this.getCachedState().getBlock());
    this.syncPlaying();
    this.markDirty();
  }

  private void syncPlaying() {
    if (this.world == null) {
      return;
    }
    final BlockState state = this.world.getBlockState(this.pos);
    if (state.getBlock() instanceof GramophoneBlock
        && state.get(GramophoneBlock.PLAYING) != this.manager.isPlaying()) {
      this.world.setBlockState(
          this.pos,
          state.with(GramophoneBlock.PLAYING, this.manager.isPlaying()),
          Block.NOTIFY_LISTENERS);
    }
  }

  private void onRecordChanged(final boolean hasRecord) {
    if (this.world == null || this.world.getBlockState(this.pos) != this.getCachedState()) {
      return;
    }
    this.world.setBlockState(
        this.pos,
        this.getCachedState()
            .with(GramophoneBlock.HAS_RECORD, hasRecord)
            .with(GramophoneBlock.PLAYING, hasRecord && this.song().isPresent()),
        Block.NOTIFY_LISTENERS);
    this.world.emitGameEvent(
        GameEvent.BLOCK_CHANGE, this.pos, GameEvent.Emitter.of(this.getCachedState()));
  }

  @Override
  protected void readNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.readNbt(nbt, registries);
    this.record =
        nbt.contains(RECORD_KEY, NbtElement.COMPOUND_TYPE)
            ? ItemStack.fromNbt(registries, nbt.getCompound(RECORD_KEY)).orElse(ItemStack.EMPTY)
            : ItemStack.EMPTY;
    this.ambient = nbt.getBoolean(AMBIENT_KEY);
    this.wait =
        nbt.contains(WAIT_KEY, NbtElement.INT_TYPE)
            ? nbt.getInt(WAIT_KEY)
            : GramophoneAmbience.UNSET;
    if (nbt.contains(TICKS_KEY, NbtElement.LONG_TYPE)) {
      JukeboxSong.getSongEntryFromStack(registries, this.record)
          .ifPresent(song -> this.manager.setValues(song, nbt.getLong(TICKS_KEY)));
    }
  }

  @Override
  protected void writeNbt(final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    super.writeNbt(nbt, registries);
    if (!this.record.isEmpty()) {
      nbt.put(RECORD_KEY, this.record.encode(registries));
    }
    if (this.manager.getSong() != null) {
      nbt.putLong(TICKS_KEY, this.manager.getTicksSinceSongStarted());
    }
    if (this.ambient) {
      nbt.putBoolean(AMBIENT_KEY, true);
    }
    if (this.wait != GramophoneAmbience.UNSET) {
      nbt.putInt(WAIT_KEY, this.wait);
    }
  }

  @Override
  public ItemStack getStack() {
    return this.record;
  }

  @Override
  public ItemStack decreaseStack(final int count) {
    final ItemStack taken = this.record;
    this.setStack(ItemStack.EMPTY);
    return taken;
  }

  @Override
  public void setStack(final ItemStack stack) {
    this.record = stack;
    final boolean hasRecord = !stack.isEmpty();
    this.onRecordChanged(hasRecord);
    if (hasRecord) {
      this.play();
      return;
    }
    this.ambient = false;
    this.wait = GramophoneAmbience.UNSET;
    if (this.world != null) {
      this.manager.stopPlaying(this.world, this.getCachedState());
    }
    this.markDirty();
  }

  @Override
  public int getMaxCountPerStack() {
    return 1;
  }

  @Override
  public BlockEntity asBlockEntity() {
    return this;
  }

  @Override
  public boolean isValid(final int slot, final ItemStack stack) {
    return stack.contains(DataComponentTypes.JUKEBOX_PLAYABLE) && this.getStack(slot).isEmpty();
  }

  @Override
  public boolean canTransferTo(
      final Inventory hopperInventory, final int slot, final ItemStack stack) {
    return hopperInventory.containsAny(ItemStack::isEmpty);
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            CONTROLLER,
            0,
            state -> {
              final BlockState block = this.getCachedState();
              this.clips
                  .observe(
                      GramophoneState.of(
                          block.get(GramophoneBlock.HAS_RECORD),
                          block.get(GramophoneBlock.PLAYING)))
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
