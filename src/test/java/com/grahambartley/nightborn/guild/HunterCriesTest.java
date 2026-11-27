package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HunterCriesTest {

  @ParameterizedTest(name = "struck {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("a hunter grunts only at a blow that lands, and passes the result on")
  void strike(final boolean struck) {
    final GuildFighter hunter = mock(GuildFighter.class);
    assertEquals(struck, HunterCries.strike(hunter, struck));
    verify(hunter, times(struck ? 1 : 0)).speak(HunterVoice.STRIKE);
  }

  @Test
  @DisplayName("a hunter does not cheer a kill that is not a vampire")
  void noCheerForAnOrdinaryKill() {
    final GuildFighter hunter = mock(GuildFighter.class);
    final LivingEntity zombie = mock(LivingEntity.class);
    when(zombie.getType()).thenAnswer(invocation -> EntityType.ZOMBIE);
    HunterCries.killed(hunter, zombie);
    verify(hunter, never()).speak(HunterVoice.CHEER);
  }
}
