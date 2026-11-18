package com.grahambartley.nightborn.enchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BloodlettingEffectTest {

  @ParameterizedTest(name = "level {0}: one blood every {1} blows")
  @CsvSource({"1, 4", "2, 3", "3, 2"})
  @DisplayName("the shipped Bloodletting gives a blood point every 4, 3, then 2 blows")
  void shippedBlows(final int level, final int blows) throws IOException {
    final EnchantmentEntityEffect effect =
        EnchantmentEntityEffect.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                        Files.readString(
                            Path.of(
                                "src/main/resources/data/nightborn/enchantment/bloodletting.json")))
                    .getAsJsonObject()
                    .getAsJsonObject("effects")
                    .getAsJsonArray("minecraft:post_attack")
                    .get(0)
                    .getAsJsonObject()
                    .get("effect"))
            .getOrThrow();
    assertEquals(
        BloodlettingEffect.ID, Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE.getId(effect.getCodec()));
    assertEquals(blows, Math.round(((BloodlettingEffect) effect).blows().getValue(level)));
  }
}
