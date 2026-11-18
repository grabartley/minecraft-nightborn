package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.ammo.AmmoItems;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.holy.HolyWater;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
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

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"SPLASH_HOLY, true", "LINGERING_HOLY, false", "SPLASH_WATER, false", "GLASS, false"})
  @DisplayName("a pocket that names a potion takes only that potion in the bottles it names")
  void acceptsANamedPotion(final String offered, final boolean accepted) {
    final AccessoryHold flasks =
        new AccessoryHold(List.of(Items.SPLASH_POTION), Optional.of(HolyWater.POTION), 4);
    assertEquals(accepted, flasks.accepts(stack(offered)));
  }

  @ParameterizedTest(name = "{0}: pocket {1}")
  @CsvSource({"BOLT, 0", "SPLASH_HOLY, 1", "GLASS, -1"})
  @DisplayName("a piece with two pockets puts each thing in the first pocket that takes it")
  void fitting(final String offered, final int pocket) {
    final List<AccessoryHold> bandolier = bandolier();
    assertEquals(
        pocket < 0 ? Optional.empty() : Optional.of(bandolier.get(pocket)),
        AccessoryHold.fitting(bandolier, stack(offered)));
  }

  @ParameterizedTest(name = "{0} and {1}: {2}")
  @CsvSource({"128, 4, 132", "6, 0, 6", "1, 0, 1"})
  @DisplayName("a piece holds as much as all its pockets together")
  void capacity(final int first, final int second, final int total) {
    final List<AccessoryHold> holds =
        second == 0
            ? List.of(new AccessoryHold(List.of(Items.ARROW), first))
            : List.of(
                new AccessoryHold(List.of(Items.ARROW), first),
                new AccessoryHold(List.of(Items.SPLASH_POTION), second));
    assertEquals(total, AccessoryHold.capacity(holds));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "'{\"items\":[\"minecraft:splash_potion\"],\"potion\":\"nightborn:holy_water\",\"count\":4}', true",
    "'{\"items\":[\"nightborn:stake_bolt\"],\"count\":128}', true",
    "'{\"items\":[\"nightborn:stake_bolt\"],\"count\":257}', false"
  })
  @DisplayName("data can name a potion and hold more than a stack")
  void readsPockets(final String json, final boolean reads) {
    assertEquals(
        reads,
        AccessoryHold.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  static List<AccessoryHold> bandolier() {
    return List.of(
        new AccessoryHold(List.of(AmmoItems.STAKE_BOLT, AmmoItems.SILVER_BOLT), 128),
        new AccessoryHold(List.of(Items.SPLASH_POTION), Optional.of(HolyWater.POTION), 4));
  }

  static ItemStack stack(final String name) {
    return switch (name) {
      case "BOLT" -> new ItemStack(AmmoItems.STAKE_BOLT);
      case "SPLASH_HOLY" ->
          PotionContentsComponent.createStack(Items.SPLASH_POTION, HolyWater.POTION);
      case "LINGERING_HOLY" ->
          PotionContentsComponent.createStack(Items.LINGERING_POTION, HolyWater.POTION);
      case "SPLASH_WATER" ->
          PotionContentsComponent.createStack(Items.SPLASH_POTION, Potions.WATER);
      default -> new ItemStack(Items.GLASS_BOTTLE);
    };
  }
}
