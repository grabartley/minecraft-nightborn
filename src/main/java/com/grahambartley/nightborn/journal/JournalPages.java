package com.grahambartley.nightborn.journal;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class JournalPages {

  private static final Map<Journal, List<Optional<Identifier>>> GATES =
      new EnumMap<>(Journal.class);

  private JournalPages() {}

  public static void later(final ServerPlayerEntity player) {
    final MinecraftServer server = player.getServer();
    if (server != null) {
      server.send(new ServerTask(server.getTicks(), () -> check(player)));
    }
  }

  static void check(final ServerPlayerEntity player) {
    final MinecraftServer server = player.getServer();
    if (server == null || player.isRemoved()) {
      return;
    }
    for (final Journal journal : Journal.values()) {
      final int share =
          share(
              gates(journal),
              id -> counted(server.getAdvancementLoader().get(id)),
              id -> earned(player, server.getAdvancementLoader().get(id)));
      Deeds.record(
          player,
          Deed.of(DeedKinds.JOURNAL_PAGES)
              .with(DeedKinds.JOURNAL, journal.asString())
              .with(DeedKinds.SHARE, share));
    }
  }

  public static int share(
      final List<Optional<Identifier>> gates,
      final Predicate<Identifier> counted,
      final Predicate<Identifier> earned) {
    final List<Optional<Identifier>> pages =
        gates.stream().filter(gate -> gate.map(counted::test).orElse(true)).toList();
    if (pages.isEmpty()) {
      return 0;
    }
    final long open = pages.stream().filter(gate -> gate.map(earned::test).orElse(true)).count();
    return (int) (open * 100 / pages.size());
  }

  public static boolean counted(
      final boolean hidden, final AdvancementFrame frame, final int steps) {
    return !hidden && !(frame == AdvancementFrame.CHALLENGE && steps > 1);
  }

  private static boolean counted(final AdvancementEntry entry) {
    return entry != null
        && entry
            .value()
            .display()
            .map(
                display ->
                    counted(
                        display.isHidden(),
                        display.getFrame(),
                        entry.value().requirements().getLength()))
            .orElse(false);
  }

  private static boolean earned(final ServerPlayerEntity player, final AdvancementEntry entry) {
    return entry != null && player.getAdvancementTracker().getProgress(entry).isDone();
  }

  static synchronized List<Optional<Identifier>> gates(final Journal journal) {
    return GATES.computeIfAbsent(journal, JournalPages::read);
  }

  private static List<Optional<Identifier>> read(final Journal journal) {
    return FabricLoader.getInstance()
        .getModContainer(Nightborn.MOD_ID)
        .flatMap(
            mod ->
                mod.findPath(
                    "assets/"
                        + Nightborn.MOD_ID
                        + "/patchouli_books/"
                        + journal.id().getPath()
                        + "/en_us/entries"))
        .map(JournalPages::walk)
        .orElse(List.of());
  }

  private static List<Optional<Identifier>> walk(final Path entries) {
    try (Stream<Path> files = Files.walk(entries)) {
      return files
          .filter(file -> file.toString().endsWith(".json"))
          .sorted()
          .map(JournalPages::gate)
          .toList();
    } catch (final IOException unreadable) {
      throw new UncheckedIOException(unreadable);
    }
  }

  private static Optional<Identifier> gate(final Path entry) {
    try (Reader reader = Files.newBufferedReader(entry)) {
      final JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
      return json.has("advancement")
          ? Optional.of(Identifier.of(json.get("advancement").getAsString()))
          : Optional.empty();
    } catch (final IOException unreadable) {
      throw new UncheckedIOException(unreadable);
    }
  }
}
