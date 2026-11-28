package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BodyRuleDataTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({"zombie", "player", "villager"})
  @DisplayName("a mob with no body rule loaded keeps its body and cuts only with blades")
  void fallsBack(final String id) {
    assertEquals(
        BodyRule.DEFAULT,
        BodyRuleData.rule(
            DynamicRegistryManager.EMPTY, EntityType.get("minecraft:" + id).orElseThrow()));
  }

  @ParameterizedTest(name = "rules [{0}], known [{1}]: unknown [{2}]")
  @CsvSource(
      delimiter = '|',
      value = {
        "nightborn:vampire nightborn:eldest | nightborn:vampire nightborn:eldest | ''",
        "nightborn:vampyre nightborn:eldest | nightborn:vampire nightborn:eldest | nightborn:vampyre",
        "nightborn:b nightborn:a | '' | nightborn:a nightborn:b",
        "'' | nightborn:vampire | ''"
      })
  @DisplayName("a body rule that names no entity type is reported")
  void unknown(final String rules, final String known, final String expected) {
    final Set<Identifier> types = Set.copyOf(ids(known));
    assertEquals(ids(expected), BodyRuleData.unknown(ids(rules), types::contains));
  }

  private static List<Identifier> ids(final String list) {
    return list.isBlank() ? List.of() : Arrays.stream(list.split(" ")).map(Identifier::of).toList();
  }
}
