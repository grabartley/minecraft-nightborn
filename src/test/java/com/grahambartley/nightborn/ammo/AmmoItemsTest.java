package com.grahambartley.nightborn.ammo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.gear.VampireBane;
import com.grahambartley.nightborn.holy.HolyWater;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class AmmoItemsTest {

  private static final Path ASSETS = Path.of("src/main/resources/assets/nightborn");
  private static final Path DATA = Path.of("src/main/resources/data/nightborn");

  static Stream<Arguments> ammo() {
    return Stream.of(
        Arguments.of("stake_bolt", AmmoItems.STAKE_BOLT, BoltItem.class, false),
        Arguments.of("silver_bolt", AmmoItems.SILVER_BOLT, BoltItem.class, false),
        Arguments.of("holy_water_bolt", AmmoItems.HOLY_WATER_BOLT, BoltItem.class, true),
        Arguments.of("garlic_bomb", AmmoItems.GARLIC_BOMB, GarlicBombItem.class, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ammo")
  @DisplayName(
      "each piece of ammo is registered with its model, sprite, recipe and loaded crossbow")
  void registered(final String name, final Item item, final Class<?> type, final boolean holy) {
    assertEquals("nightborn:" + name, Registries.ITEM.getId(item).toString());
    assertInstanceOf(type, item);
    assertTrue(AmmoItems.ALL.contains(item));
    assertTrue(Files.exists(ASSETS.resolve("models/item/" + name + ".json")));
    assertTrue(Files.exists(ASSETS.resolve("textures/item/" + name + ".png")));
    assertTrue(Files.exists(ASSETS.resolve("models/item/crossbow_" + name + ".json")));
    assertTrue(Files.exists(DATA.resolve("recipe/" + name + ".json")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ammo")
  @DisplayName("only the holy water bolt carries holy water")
  void carriesHolyWater(
      final String name, final Item item, final Class<?> type, final boolean holy) {
    assertEquals(holy, HolyWater.isHolyWater(new ItemStack(item)));
  }

  static Stream<Arguments> bane() {
    return Stream.of(
        Arguments.of("stake_bolt", "stake_ammo"), Arguments.of("silver_bolt", "silver_ammo"));
  }

  @ParameterizedTest(name = "{0} is {1}")
  @MethodSource("bane")
  @DisplayName("stake and silver bolts are tagged so they count as stakes and silver")
  void tagged(final String name, final String tag) throws Exception {
    final String values = Files.readString(DATA.resolve("tags/item/" + tag + ".json"));
    assertTrue(values.contains("\"nightborn:" + name + "\""));
    assertEquals(
        "nightborn:" + tag,
        (tag.startsWith("stake") ? VampireBane.STAKE_AMMO : VampireBane.SILVER_AMMO)
            .id()
            .toString());
  }
}
