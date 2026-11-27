package com.grahambartley.nightborn.faction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.mob.VampireEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.VillagerEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HuntersTest {

  @ParameterizedTest(name = "{0}: hunter {1}")
  @CsvSource({"hunter, true", "vampire, false", "villager, false"})
  @DisplayName("a hunter mob is a hunter, and other mobs are not")
  void isHunter(final String kind, final boolean hunter) {
    final Entity entity =
        switch (kind) {
          case "hunter" -> mock(HunterEntity.class);
          case "vampire" -> mock(VampireEntity.class);
          default -> mock(VillagerEntity.class);
        };
    assertEquals(hunter, Hunters.isHunter(entity));
  }
}
