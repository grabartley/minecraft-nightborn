package com.grahambartley.nightborn.house;

import com.grahambartley.nightborn.Nightborn;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.PersistentState;

public final class Houses extends PersistentState {

  static final String ID = Nightborn.MOD_ID + "_houses";
  static final String HOUSES_KEY = "houses";

  private final HouseBook book = new HouseBook();

  public static Houses get(final MinecraftServer server) {
    return server
        .getOverworld()
        .getPersistentStateManager()
        .getOrCreate(new PersistentState.Type<>(Houses::new, Houses::fromNbt, null), ID);
  }

  public Optional<House> ledBy(final UUID lord) {
    return book.ledBy(lord);
  }

  public Optional<House> of(final UUID vampire) {
    return book.of(vampire);
  }

  public Optional<House> holding(final GlobalPos seat) {
    return book.holding(seat);
  }

  public boolean seatNear(final GlobalPos pos, final int reach) {
    return book.seatNear(pos, reach);
  }

  public Collection<House> all() {
    return book.all();
  }

  public void put(final House house) {
    if (!book.ledBy(house.lord()).filter(house::equals).isPresent()) {
      book.put(house);
      markDirty();
    }
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final NbtList list = new NbtList();
    for (final House house : book.all()) {
      House.CODEC.encodeStart(NbtOps.INSTANCE, house).ifSuccess(list::add);
    }
    nbt.put(HOUSES_KEY, list);
    return nbt;
  }

  public static Houses fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final Houses houses = new Houses();
    for (final NbtElement entry : nbt.getList(HOUSES_KEY, NbtElement.COMPOUND_TYPE)) {
      House.CODEC
          .parse(NbtOps.INSTANCE, entry)
          .resultOrPartial(error -> Nightborn.LOGGER.warn("Skipping a damaged house: {}", error))
          .ifPresent(houses.book::put);
    }
    return houses;
  }
}
