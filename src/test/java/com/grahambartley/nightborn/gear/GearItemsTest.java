package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GearItemsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");

  private static final Map<String, double[][]> REVERSE_GRIP =
      Map.of(
          "thirdperson_righthand",
          new double[][] {{0, -90, 155}, {0, -4.2, -6.5}, {0.85, 0.85, 0.85}},
          "thirdperson_lefthand",
          new double[][] {{0, 90, -155}, {0, -4.2, -6.5}, {0.85, 0.85, 0.85}},
          "firstperson_righthand",
          new double[][] {{0, -90, -153}, {1.13, -1.2, -1.71}, {0.68, 0.68, 0.68}},
          "firstperson_lefthand",
          new double[][] {{0, 90, 153}, {1.13, -1.2, -1.71}, {0.68, 0.68, 0.68}});

  static Stream<Arguments> items() {
    return Stream.of(
        Arguments.of("wooden_stake", GearItems.WOODEN_STAKE, StakeItem.class),
        Arguments.of("silver_stake", GearItems.SILVER_STAKE, StakeItem.class),
        Arguments.of("silver_sword", GearItems.SILVER_SWORD, SwordItem.class),
        Arguments.of("silver_axe", GearItems.SILVER_AXE, AxeItem.class),
        Arguments.of("silver_ingot", GearItems.SILVER_INGOT, Item.class),
        Arguments.of("silver_nugget", GearItems.SILVER_NUGGET, Item.class),
        Arguments.of("raw_silver", GearItems.RAW_SILVER, Item.class),
        Arguments.of("garlic", GearItems.GARLIC, AliasedBlockItem.class),
        Arguments.of("gold_crucifix", GearItems.GOLD_CRUCIFIX, CrucifixItem.class),
        Arguments.of("silver_crucifix", GearItems.SILVER_CRUCIFIX, CrucifixItem.class));
  }

  static Stream<Arguments> crucifixes() {
    return Stream.of(
        Arguments.of("gold_crucifix", GearItems.GOLD_CRUCIFIX),
        Arguments.of("silver_crucifix", GearItems.SILVER_CRUCIFIX));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("crucifixes")
  @DisplayName("a crucifix is held one at a time")
  void crucifixDoesNotStack(final String name, final Item crucifix) {
    assertEquals(1, crucifix.getMaxCount());
  }

  @Test
  @DisplayName("garlic is planted as the garlic crop")
  void garlicPlantsTheCrop() {
    assertEquals(GearBlocks.GARLIC, ((AliasedBlockItem) GearItems.GARLIC).getBlock());
    assertEquals(GearItems.GARLIC, GearBlocks.GARLIC.asItem());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("items")
  @DisplayName("each item is registered at the id its art was made for")
  void registered(final String name, final Item item, final Class<? extends Item> type) {
    assertEquals("nightborn:" + name, Registries.ITEM.getId(item).toString());
    assertInstanceOf(type, item);
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + name + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("textures/item/" + name + ".png")));
  }

  @Test
  @DisplayName("garlic is a small meal that still plants its crop")
  void garlicIsFoodAndSeed() {
    final FoodComponent food = GearItems.GARLIC.getDefaultStack().get(DataComponentTypes.FOOD);
    assertEquals(GearItems.GARLIC_FOOD, food);
    assertEquals(2, food.nutrition());
    assertTrue(food.saturation() > 0);
    assertEquals(
        GearBlocks.GARLIC, assertInstanceOf(AliasedBlockItem.class, GearItems.GARLIC).getBlock());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"wooden_stake", "silver_stake", "ashen_silver_stake"})
  @DisplayName(
      "a stake is held point down with its butt in the fist, at vanilla's scale, and keeps"
          + " vanilla's GUI, ground, head and frame views")
  void stakesHeldPointDown(final String name) throws IOException {
    final JsonObject model =
        JsonParser.parseString(Files.readString(ASSETS.resolve("models/item/" + name + ".json")))
            .getAsJsonObject();
    assertEquals("minecraft:item/handheld", model.get("parent").getAsString());
    final JsonObject display = model.getAsJsonObject("display");
    assertEquals(Set.of(REVERSE_GRIP.keySet().toArray(String[]::new)), display.keySet());
    REVERSE_GRIP.forEach(
        (hand, transform) -> {
          final JsonObject view = display.getAsJsonObject(hand);
          for (int i = 0; i < 3; i++) {
            assertEquals(transform[0][i], view.getAsJsonArray("rotation").get(i).getAsDouble());
            assertEquals(transform[1][i], view.getAsJsonArray("translation").get(i).getAsDouble());
            assertEquals(transform[2][i], view.getAsJsonArray("scale").get(i).getAsDouble());
          }
        });
  }
}
