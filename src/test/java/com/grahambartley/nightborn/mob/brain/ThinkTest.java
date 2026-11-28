package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ThinkTest {

  private static PowerChoice.Scored scored(
      final String path, final double score, final boolean affordable) {
    final MobPowerUse use =
        new MobPowerUse(0.0F, 8.0F, 5, Identifier.of("minecraft", "x"), "tell", false);
    return new PowerChoice.Scored(
        new PowerChoice.Option(Identifier.of("nightborn", path), PowerUse.Verdict.READY, 0, use),
        use.effectiveUses().getFirst(),
        score,
        affordable);
  }

  @ParameterizedTest(name = "first affordable {0}, threshold {1}: {2}")
  @CsvSource({"true, 0, first", "false, 0, second", "false, 1.5, ''", "true, 3, ''"})
  @DisplayName("a think chooses the best affordable use above its threshold")
  void chosen(final boolean firstAffordable, final float threshold, final String chosen) {
    final Think think =
        new Think(
            Intent.FIGHT,
            40L,
            List.of(scored("first", 2.0, firstAffordable), scored("second", 1.0, true)),
            new IntentBudget(0, 0, threshold),
            0.0);
    assertEquals(
        chosen.isEmpty() ? Optional.empty() : Optional.of(Identifier.of("nightborn", chosen)),
        think.chosen().map(s -> s.option().id()));
  }
}
