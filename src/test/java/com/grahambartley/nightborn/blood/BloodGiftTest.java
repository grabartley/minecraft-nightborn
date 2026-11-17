package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.vampire.VampireAttributes;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BloodGiftTest {

  private static final Path GIFTS =
      Path.of("src/main/resources/data/nightborn/nightborn/blood_gift");

  private static BloodGift load(final String name) throws IOException {
    VampireAttributes.register();
    return BloodGift.CODEC
        .parse(
            RegistryOps.of(
                JsonOps.INSTANCE,
                RegistryWrapper.WrapperLookup.of(
                    Stream.of(
                        Registries.ENTITY_TYPE.getReadOnlyWrapper(),
                        Registries.ATTRIBUTE.getReadOnlyWrapper()))),
            JsonParser.parseString(Files.readString(GIFTS.resolve(name + ".json"))))
        .getOrThrow();
  }

  static Stream<Arguments> prey() {
    return Stream.of(
        Arguments.of("horse", EntityType.HORSE, "minecraft:generic.movement_speed", 0.2),
        Arguments.of("horse", EntityType.DONKEY, "minecraft:generic.movement_speed", 0.2),
        Arguments.of("horse", EntityType.MULE, "minecraft:generic.movement_speed", 0.2),
        Arguments.of("wolf", EntityType.WOLF, "nightborn:prey_sense", 12.0),
        Arguments.of("villager", EntityType.VILLAGER, "nightborn:drain_resistance", 1.0),
        Arguments.of("witch", EntityType.WITCH, "nightborn:potion_resistance", 0.5));
  }

  @ParameterizedTest(name = "{0} feeds {1} with {2} {3}")
  @MethodSource("prey")
  @DisplayName("each shipped gift names its prey, lasts a minute and gives its locked default")
  void shippedGifts(
      final String name, final EntityType<?> prey, final String attribute, final double amount)
      throws IOException {
    final BloodGift gift = load(name);
    assertTrue(gift.feeds(prey));
    assertEquals(60, gift.seconds());
    assertEquals(1200, gift.ticks());
    assertEquals("nightborn.blood_gift." + name, gift.name());
    final GiftModifier modifier = gift.modifiers().getFirst();
    assertEquals(
        Identifier.of(attribute), Registries.ATTRIBUTE.getId(modifier.attribute().value()));
    assertEquals(amount, modifier.amount(), 1.0E-9);
  }

  @ParameterizedTest(name = "fresh {0}: strength {1}")
  @CsvSource({"true, 1.0", "false, 0.5"})
  @DisplayName("a bottle gives the gift at half strength, never more than fresh")
  void strength(final boolean fresh, final float strength) {
    assertEquals(strength, BloodGift.strength(fresh));
    assertTrue(BloodGift.strength(false) < BloodGift.strength(true));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"horse", "wolf", "villager", "witch"})
  @DisplayName("no shipped gift feeds a cow")
  void cowsGiveNothing(final String name) throws IOException {
    assertEquals(false, load(name).feeds(EntityType.COW));
  }

  @ParameterizedTest(name = "{0} names {1}")
  @CsvSource({"illager, #minecraft:illager"})
  @DisplayName("the illager gift follows the vanilla illager tag and gives 3 attack damage")
  void illagerGift(final String name, final String prey) throws IOException {
    final var json =
        JsonParser.parseString(Files.readString(GIFTS.resolve(name + ".json"))).getAsJsonObject();
    assertEquals(prey, json.get("prey").getAsString());
    assertEquals(
        3.0, json.getAsJsonArray("modifiers").get(0).getAsJsonObject().get("amount").getAsDouble());
  }
}
