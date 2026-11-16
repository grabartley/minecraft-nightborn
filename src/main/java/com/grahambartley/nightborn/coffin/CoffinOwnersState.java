package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.PersistentState;

public final class CoffinOwnersState extends PersistentState {

  static final String ID = Nightborn.MOD_ID + "_coffin_owners";
  static final String OWNERS_KEY = "owners";

  private final CoffinOwners<GlobalPos> owners = new CoffinOwners<>();

  public static CoffinOwnersState get(final MinecraftServer server) {
    return server
        .getOverworld()
        .getPersistentStateManager()
        .getOrCreate(
            new PersistentState.Type<>(CoffinOwnersState::new, CoffinOwnersState::fromNbt, null),
            ID);
  }

  public Optional<UUID> ownerOf(final GlobalPos coffin) {
    return this.owners.ownerOf(coffin);
  }

  public Optional<GlobalPos> coffinOf(final UUID owner) {
    return this.owners.coffinOf(owner);
  }

  public boolean assign(final UUID owner, final GlobalPos coffin) {
    return this.dirtyIf(this.owners.assign(owner, coffin));
  }

  public Optional<GlobalPos> release(final UUID owner) {
    final Optional<GlobalPos> freed = this.owners.release(owner);
    this.dirtyIf(freed.isPresent());
    return freed;
  }

  public Optional<UUID> releaseCoffin(final GlobalPos coffin) {
    final Optional<UUID> freed = this.owners.releaseCoffin(coffin);
    this.dirtyIf(freed.isPresent());
    return freed;
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final NbtCompound entries = new NbtCompound();
    this.owners
        .entries()
        .forEach(
            (owner, coffin) ->
                GlobalPos.CODEC
                    .encodeStart(NbtOps.INSTANCE, coffin)
                    .ifSuccess(encoded -> entries.put(owner.toString(), encoded)));
    nbt.put(OWNERS_KEY, entries);
    return nbt;
  }

  public static CoffinOwnersState fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final CoffinOwnersState state = new CoffinOwnersState();
    final NbtCompound entries = nbt.getCompound(OWNERS_KEY);
    for (final String key : entries.getKeys()) {
      final Optional<UUID> owner = parseUuid(key);
      final NbtElement value = entries.get(key);
      final Optional<GlobalPos> coffin = GlobalPos.CODEC.parse(NbtOps.INSTANCE, value).result();
      if (owner.isEmpty() || coffin.isEmpty() || !state.owners.assign(owner.get(), coffin.get())) {
        Nightborn.LOGGER.warn("Skipping a damaged coffin owner entry {}: {}", key, value);
      }
    }
    return state;
  }

  private static Optional<UUID> parseUuid(final String text) {
    try {
      return Optional.of(UUID.fromString(text));
    } catch (final IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  private boolean dirtyIf(final boolean changed) {
    if (changed) {
      this.markDirty();
    }
    return changed;
  }
}
