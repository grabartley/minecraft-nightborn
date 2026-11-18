package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.accessory.AccessorySlot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class VampireLooksTest {

  private static final Path DATA = Path.of("src/main/resources/data/nightborn/nightborn");
  private static final Path ASSETS = Path.of("src/main/resources/assets");

  @Test
  @DisplayName("the shipped looks, outfits and builds read cleanly and pass the data check")
  void shippedDataIsClean() throws IOException {
    assertEquals(
        java.util.List.of(),
        LookDataCheck.problems(
            read("vampire_look", VampireLook.CODEC),
            read("vampire_outfit", VampireOutfit.CODEC),
            read("vampire_build", VampireBuild.CODEC),
            id -> VampireLooks.slotOf(id).or(() -> accessorySlot(id))));
  }

  private static java.util.Optional<net.minecraft.entity.EquipmentSlot> accessorySlot(
      final Identifier item) {
    return Stream.of(AccessorySlot.values())
        .filter(slot -> taggedIn(slot, item))
        .findFirst()
        .map(AccessorySlot::body);
  }

  private static boolean taggedIn(final AccessorySlot slot, final Identifier item) {
    final Path tag = Path.of("src/main/resources/data/trinkets/tags/item/" + slot.id() + ".json");
    try {
      return Files.exists(tag)
          && JsonParser.parseString(Files.readString(tag))
              .getAsJsonObject()
              .getAsJsonArray("values")
              .asList()
              .stream()
              .anyMatch(value -> value.getAsString().equals(item.toString()));
    } catch (final IOException e) {
      throw new java.io.UncheckedIOException(e);
    }
  }

  @Test
  @DisplayName("there are at least ten looks across both rigs, sized from 1.5 to 2 blocks")
  void tenLooksAndFullHeightRange() throws IOException {
    final Map<Identifier, VampireLook> looks = read("vampire_look", VampireLook.CODEC);
    assertTrue(looks.size() >= 10, "looks: " + looks.size());
    final Set<VampireRig> rigs = new HashSet<>();
    looks.values().forEach(look -> rigs.add(look.rig()));
    assertEquals(Set.of(VampireRig.values()), rigs);
    final Map<Identifier, VampireBuild> builds = read("vampire_build", VampireBuild.CODEC);
    assertEquals(
        1.5F, builds.values().stream().map(b -> b.minHeight()).min(Float::compare).orElseThrow());
    assertEquals(
        2.0F, builds.values().stream().map(b -> b.maxHeight()).max(Float::compare).orElseThrow());
  }

  static Stream<VampireLook> looks() throws IOException {
    return read("vampire_look", VampireLook.CODEC).values().stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("looks")
  @DisplayName("every look's texture ships with the mod")
  void textureShips(final VampireLook look) {
    final Path texture =
        ASSETS.resolve(look.texture().getNamespace()).resolve(look.texture().getPath());
    assertTrue(Files.exists(texture), texture.toString());
  }

  private static <T> Map<Identifier, T> read(final String folder, final Codec<T> codec)
      throws IOException {
    try (Stream<Path> files = Files.list(DATA.resolve(folder))) {
      return files.collect(
          Collectors.toMap(
              file ->
                  Identifier.of("nightborn", file.getFileName().toString().replace(".json", "")),
              file -> {
                try {
                  return codec
                      .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(file)))
                      .getOrThrow();
                } catch (final IOException e) {
                  throw new IllegalStateException(e);
                }
              },
              (a, b) -> a,
              TreeMap::new));
    }
  }
}
