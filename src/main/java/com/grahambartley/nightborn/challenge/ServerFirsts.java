package com.grahambartley.nightborn.challenge;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.faction.FactionPlayerList;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;

public final class ServerFirsts extends PersistentState {

  static final String ID = Nightborn.MOD_ID + "_server_firsts";
  private static final String BOOK = "firsts";
  private static final PersistentState.Type<ServerFirsts> TYPE =
      new PersistentState.Type<>(ServerFirsts::new, ServerFirsts::fromNbt, null);

  private FirstsBook book = FirstsBook.EMPTY;

  public static ServerFirsts of(final MinecraftServer server) {
    return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, ID);
  }

  public FirstsBook book() {
    return book;
  }

  static void offer(final ServerPlayerEntity player, final Deed deed) {
    final MinecraftServer server = player.getServer();
    if (server == null) {
      return;
    }
    for (final ServerFirst first : ServerFirst.values()) {
      if (first.marks(deed)) {
        of(server).claim(server, player, first);
      }
    }
  }

  private void claim(
      final MinecraftServer server, final ServerPlayerEntity player, final ServerFirst first) {
    book.claim(first, new FirstsBook.Holder(player.getUuid(), player.getGameProfile().getName()))
        .ifPresent(
            next -> {
              book = next;
              markDirty();
              announce(server, player, first);
            });
  }

  private static void announce(
      final MinecraftServer server, final ServerPlayerEntity player, final ServerFirst first) {
    final Text line =
        FirstNews.line(
                first, player.getName(), Vampires.isVampire(player), FactionPlayerList.shown())
            .copy()
            .formatted(Formatting.GOLD);
    server.getPlayerManager().broadcast(line, false);
    for (final ServerPlayerEntity listener : server.getPlayerManager().getPlayerList()) {
      listener.playSoundToPlayer(
          SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.7F, 0.5F);
    }
  }

  @Override
  public NbtCompound writeNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    FirstsBook.CODEC
        .encodeStart(NbtOps.INSTANCE, book)
        .resultOrPartial(error -> Nightborn.LOGGER.error("Could not save server firsts: {}", error))
        .ifPresent(encoded -> nbt.put(BOOK, encoded));
    return nbt;
  }

  static ServerFirsts fromNbt(
      final NbtCompound nbt, final RegistryWrapper.WrapperLookup registries) {
    final ServerFirsts firsts = new ServerFirsts();
    if (nbt.contains(BOOK, NbtElement.COMPOUND_TYPE)) {
      FirstsBook.CODEC
          .parse(NbtOps.INSTANCE, nbt.get(BOOK))
          .resultOrPartial(
              error -> Nightborn.LOGGER.error("Could not read server firsts: {}", error))
          .ifPresent(read -> firsts.book = read);
    }
    return firsts;
  }
}
