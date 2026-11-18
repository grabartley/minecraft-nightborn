package com.grahambartley.nightborn.enchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.effect.EnchantmentEffectEntry;
import net.minecraft.enchantment.effect.EnchantmentValueEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class EnchantedValuesTest {

  private static final Path DATA = Path.of("src/main/resources/data/nightborn/enchantment");
  private static final double EPSILON = 1.0E-4;

  @ParameterizedTest(name = "{0} {1} on {2}: {3}")
  @CsvSource({
    "heartseeker, 1, 10.0, 11.0",
    "heartseeker, 2, 10.0, 12.0",
    "heartseeker, 3, 10.0, 13.0",
    "consecration, 1, 8.0, 10.0",
    "consecration, 2, 8.0, 12.0",
    "consecration, 3, 8.0, 14.0",
    "exsanguinate, 1, 20.0, 25.0",
    "exsanguinate, 2, 20.0, 30.0",
    "exsanguinate, 3, 20.0, 35.0"
  })
  @DisplayName("the shipped enchantments change their number by the locked amount per level")
  void shippedValues(final String name, final int level, final float base, final float result)
      throws IOException {
    final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>> type = typeOf(name);
    final ItemStack stack = new ItemStack(Items.IRON_SWORD);
    stack.addEnchantment(RegistryEntry.of(enchantment(name, type)), level);
    assertEquals(result, EnchantedValues.apply(stack, type, base), EPSILON);
  }

  @Test
  @DisplayName("an unenchanted item leaves the number alone")
  void plain() {
    assertEquals(
        20.0F,
        EnchantedValues.apply(
            new ItemStack(Items.IRON_SWORD), EnchantedValues.FINISHER_HEALTH, 20.0F),
        EPSILON);
  }

  private static ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>> typeOf(
      final String name) {
    return switch (name) {
      case "heartseeker" -> EnchantedValues.STAKE_DAMAGE;
      case "consecration" -> EnchantedValues.COATING_HITS;
      default -> EnchantedValues.FINISHER_HEALTH;
    };
  }

  private static Enchantment enchantment(
      final String name,
      final ComponentType<List<EnchantmentEffectEntry<EnchantmentValueEffect>>> type)
      throws IOException {
    final String key =
        "nightborn:"
            + switch (name) {
              case "heartseeker" -> "stake_damage";
              case "consecration" -> "coating_hits";
              default -> "finisher_health";
            };
    final List<EnchantmentEffectEntry<EnchantmentValueEffect>> effects =
        type.getCodecOrThrow()
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(Files.readString(DATA.resolve(name + ".json")))
                    .getAsJsonObject()
                    .getAsJsonObject("effects")
                    .get(key))
            .getOrThrow();
    final Enchantment.Builder builder =
        Enchantment.builder(
            Enchantment.definition(
                RegistryEntryList.of(),
                1,
                3,
                Enchantment.constantCost(1),
                Enchantment.constantCost(1),
                1,
                AttributeModifierSlot.MAINHAND));
    effects.forEach(entry -> builder.addEffect(type, entry.effect()));
    return builder.build(Identifier.of("nightborn", name));
  }
}
