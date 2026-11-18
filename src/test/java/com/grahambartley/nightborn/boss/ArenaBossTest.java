package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.lord.BossPresence;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ArenaBossTest {

  @ParameterizedTest(name = "clip {0}")
  @ValueSource(strings = {"", "hiss"})
  @DisplayName("a boss swears its called allies to nobody unless it says otherwise")
  void swornToNobody(final String clip) {
    final ArenaBoss boss =
        new ArenaBoss() {
          @Override
          public BossPresence presence() {
            return null;
          }

          @Override
          public boolean attacksOnSight(final LivingEntity target) {
            return false;
          }

          @Override
          public void showPower(final String shown) {}
        };
    boss.showPower(clip);
    assertEquals(Set.of(), boss.sworn());
  }
}
