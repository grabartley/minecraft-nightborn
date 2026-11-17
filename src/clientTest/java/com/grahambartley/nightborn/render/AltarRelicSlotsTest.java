package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AltarRelicSlotsTest {

  @ParameterizedTest(name = "{0} relics")
  @ValueSource(ints = {1, 2, 3, 4, 6})
  @DisplayName("relics sit on the altar top, apart from each other and clear of the seal")
  void spread(final int count) {
    final List<Vec3d> slots = new ArrayList<>();
    for (int slot = 0; slot < count; slot++) {
      final Vec3d at = AltarRelicSlots.offset(slot, count);
      assertEquals(AltarRelicSlots.TOP, at.y, 1.0e-9);
      assertEquals(AltarRelicSlots.RADIUS, Math.hypot(at.x, at.z), 1.0e-9);
      assertTrue(Math.abs(at.x) < 0.5 && Math.abs(at.z) < 0.5);
      slots.forEach(other -> assertTrue(other.distanceTo(at) > 0.2));
      slots.add(at);
    }
  }
}
