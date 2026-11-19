package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.entity.EquipmentSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessorySlotTest {

  @ParameterizedTest(name = "{0}: {1}, worn over {2}")
  @CsvSource({
    "RING, hand/ring, CHEST",
    "SECOND_RING, offhand/ring, CHEST",
    "NECKLACE, chest/necklace, CHEST",
    "BACK, chest/back, CHEST",
    "GLOVES, hand/glove, CHEST",
    "BELT, legs/belt, LEGS",
    "FACE, head/face, HEAD",
    "BROOCH, chest/brooch, CHEST"
  })
  @DisplayName("each slot has its id, its item tag and the body part it is drawn on")
  void facts(final AccessorySlot slot, final String id, final EquipmentSlot body) {
    assertEquals(id, slot.id());
    assertEquals("trinkets:" + id, slot.tag().id().toString());
    assertEquals(body, slot.body());
    assertEquals(Optional.of(slot), AccessorySlot.byId(id));
  }

  @ParameterizedTest(name = "{0}: art drawn on {1}")
  @CsvSource({
    "RING, CHEST",
    "SECOND_RING, CHEST",
    "NECKLACE, CHEST",
    "BACK, CHEST",
    "GLOVES, CHEST",
    "BELT, CHEST",
    "FACE, HEAD",
    "BROOCH, CHEST"
  })
  @DisplayName(
      "each slot's art is drawn on the body part whose bones carry it, a belt on the torso")
  void drawnOn(final AccessorySlot slot, final EquipmentSlot body) {
    assertEquals(body, slot.drawnOn());
  }

  @ParameterizedTest(name = "{0}: drawn {1}")
  @CsvSource({
    "hand/ring, false",
    "offhand/ring, false",
    "chest/back, true",
    "head/face, true",
    "hand/glove, true",
    "chest/cape, false"
  })
  @DisplayName("every piece but a ring is drawn, and only in the mod's own slots")
  void drawnIn(final String slot, final boolean drawn) {
    assertEquals(drawn, AccessorySlot.byId(slot).filter(AccessorySlot::drawn).isPresent());
  }

  @Test
  @DisplayName("a slot group with no body part fails loudly")
  void unknownGroup() {
    assertThrows(IllegalArgumentException.class, () -> AccessorySlot.bodyOf("charm"));
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "head, HEAD",
    "chest, CHEST",
    "legs, LEGS",
    "feet, FEET",
    "hand, CHEST",
    "offhand, CHEST"
  })
  @DisplayName("a slot group is drawn over the body part of the same name, hands over the arms")
  void bodyOfGroup(final String group, final EquipmentSlot body) {
    assertEquals(body, AccessorySlot.bodyOf(group));
  }

  @Test
  @DisplayName("a player has eight accessory slots, two of them rings")
  void eightSlots() {
    assertEquals(8, AccessorySlot.values().length);
    assertEquals(
        2, Arrays.stream(AccessorySlot.values()).filter(s -> s.slotName().equals("ring")).count());
    assertEquals(Optional.empty(), AccessorySlot.byId("chest/cape"));
  }
}
