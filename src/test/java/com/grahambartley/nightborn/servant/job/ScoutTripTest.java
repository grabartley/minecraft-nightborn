package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ScoutTripTest {

  private static final Identifier CRYPT = Identifier.of("nightborn", "crypt");

  static Stream<ScoutTrip> trips() {
    final ScoutTrip out = ScoutTrip.to(new BlockPos(300, 0, -120), CRYPT);
    return Stream.of(out, out.reported(new ScoutReport(1, 2, 3, 4)), out.lostTheWay());
  }

  @ParameterizedTest
  @MethodSource("trips")
  @DisplayName("a trip survives a save and load at every stage")
  void codec(final ScoutTrip trip) {
    assertEquals(
        trip,
        ScoutTrip.CODEC
            .parse(NbtOps.INSTANCE, ScoutTrip.CODEC.encodeStart(NbtOps.INSTANCE, trip).getOrThrow())
            .getOrThrow());
  }

  @Test
  @DisplayName("a scout heads home once it has seen the place or lost its way")
  void returning() {
    final ScoutTrip out = ScoutTrip.to(BlockPos.ORIGIN, CRYPT);
    assertFalse(out.returning());
    assertTrue(out.reported(new ScoutReport(0, 0, 0, 0)).returning());
    assertTrue(out.lostTheWay().returning());
    assertEquals(Optional.empty(), out.lostTheWay().report());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "nightborn:crypt, nightborn.scout.place.nightborn.crypt",
    "minecraft:village_plains, nightborn.scout.place.minecraft.village_plains"
  })
  @DisplayName("each place has a name key, falling back to its own path")
  void placeName(final String place, final String key) {
    final Identifier id = Identifier.of(place);
    assertEquals(key, ScoutTrip.placeKey(id));
    assertEquals(Text.translatableWithFallback(key, id.getPath()), ScoutTrip.placeName(id));
  }
}
