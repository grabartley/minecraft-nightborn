package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.blood.BloodItems;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AccessoryHoldTest {

  private static final AccessoryHold VIAL = new AccessoryHold(List.of(BloodItems.BLOOD_BOTTLE), 1);

  @Test
  @DisplayName("a store takes only what its data names")
  void accepts() {
    assertTrue(VIAL.accepts(new ItemStack(BloodItems.BLOOD_BOTTLE)));
    assertFalse(VIAL.accepts(new ItemStack(Items.GLASS_BOTTLE)));
    assertFalse(VIAL.accepts(ItemStack.EMPTY));
  }

  @ParameterizedTest(name = "holds {0} of {1}: room for {2}")
  @CsvSource({"0, 1, 1", "1, 1, 0", "2, 1, 0", "1, 3, 2"})
  @DisplayName("a store has room up to its count")
  void room(final int held, final int count, final int room) {
    assertEquals(room, new AccessoryHold(List.of(BloodItems.BLOOD_BOTTLE), count).room(held));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "'{\"items\":[\"nightborn:blood_bottle\"],\"count\":1}', true",
    "'{\"items\":[\"nightborn:blood_bottle\"],\"count\":0}', false",
    "'{\"items\":[\"nightborn:blood_bottle\"]}', false"
  })
  @DisplayName("data names what a store holds and how many")
  void reads(final String json, final boolean reads) {
    assertEquals(
        reads,
        AccessoryHold.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
