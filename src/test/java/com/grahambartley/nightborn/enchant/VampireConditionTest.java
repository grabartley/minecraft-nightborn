package com.grahambartley.nightborn.enchant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.Set;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class VampireConditionTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({"attacker, ATTACKER", "this, THIS"})
  @DisplayName("data names whose side is checked, and that entity is all the check needs")
  void reads(final String id, final LootContext.EntityTarget target) {
    final LootCondition condition =
        LootCondition.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"condition\":\"nightborn:vampire\",\"entity\":\"" + id + "\"}"))
            .getOrThrow();
    assertEquals(new VampireCondition(target), condition);
    assertEquals(Set.of(target.getParameter()), condition.getRequiredParameters());
  }
}
