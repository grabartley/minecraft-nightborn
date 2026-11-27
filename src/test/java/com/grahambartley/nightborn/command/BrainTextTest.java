package com.grahambartley.nightborn.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.brain.Intent;
import com.grahambartley.nightborn.mob.brain.IntentBudget;
import com.grahambartley.nightborn.mob.brain.PowerChoice;
import com.grahambartley.nightborn.mob.brain.Think;
import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BrainTextTest {

  private static final Text NAME = Text.literal("Ilse");

  private static PowerChoice.Scored scored(final int n, final double score, final boolean paid) {
    final MobPowerUse use =
        new MobPowerUse(0.0F, 8.0F, 5, Identifier.of("minecraft", "x"), "tell", false);
    return new PowerChoice.Scored(
        new PowerChoice.Option(
            Identifier.of("nightborn", "test/p" + n), PowerUse.Verdict.READY, 0, use),
        use.effectiveUses().getFirst(),
        score,
        paid);
  }

  @ParameterizedTest(name = "{0} ranked, first affordable {1}")
  @CsvSource({"1, true, chosen", "2, false, costly chosen", "5, true, chosen use use"})
  @DisplayName(
      "the brain command shows the intent, then the top three uses with the chosen one marked")
  void lines(final int count, final boolean firstPaid, final String kinds) {
    final List<PowerChoice.Scored> ranked = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      ranked.add(scored(i, 5.0 - i, i > 0 || firstPaid));
    }
    final List<Text> expected = new ArrayList<>();
    expected.add(Text.translatable("nightborn.command.brain.think", NAME, 15L, "close_in"));
    final String[] each = kinds.split(" ");
    for (int i = 0; i < each.length; i++) {
      expected.add(
          Text.translatable(
              "nightborn.command.brain." + each[i],
              "nightborn:test/p" + i,
              String.format(Locale.ROOT, "%.2f", 5.0 - i)));
    }
    assertEquals(
        expected,
        BrainText.lines(
            NAME, new Think(Intent.CLOSE_IN, 100L, ranked, IntentBudget.FREE, 0.0), 115L));
  }
}
