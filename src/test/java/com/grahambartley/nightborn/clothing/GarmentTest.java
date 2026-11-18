package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.accessory.AccessoryPiece;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class GarmentTest {

  static Stream<Garment> garments() {
    return Stream.concat(
        Arrays.stream(ClothingPiece.values()), Arrays.stream(AccessoryPiece.values()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("garments")
  @DisplayName("each garment's id is its lower-case name, as its art and data files use")
  void idIsLowerCaseName(final Garment garment) {
    assertEquals(((Enum<?>) garment).name().toLowerCase(Locale.ROOT), garment.id());
  }

  @Test
  @DisplayName("no clothing piece and accessory share an id")
  void idsAreUnique() {
    final List<String> ids = garments().map(Garment::id).toList();
    assertEquals(ids.size(), ids.stream().distinct().count());
    assertTrue(ids.contains("mourning_veil") && ids.contains("wide_brimmed_hat"));
  }
}
