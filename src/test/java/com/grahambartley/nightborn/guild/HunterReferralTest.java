package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.guild.HunterReferral.Pointed;
import com.grahambartley.nightborn.servant.job.Bearing;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HunterReferralTest {

  @ParameterizedTest(name = "alive {0}, refers {1}, vampire {2}, hunted {3} -> {4}")
  @CsvSource({
    "true, true, false, false, true",
    "true, true, true, false, false",
    "true, false, false, false, false",
    "false, true, false, false, false",
    "true, true, false, true, false"
  })
  @DisplayName("a living hunter answers a human it is not hunting, never a vampire or the boss")
  void answers(
      final boolean alive,
      final boolean refers,
      final boolean vampire,
      final boolean hunted,
      final boolean expected) {
    assertEquals(expected, HunterReferral.answers(alive, refers, vampire, hunted));
  }

  @ParameterizedTest(name = "spoke at {1}, now {0} -> {2}")
  @CsvSource({"100, 60, true", "100, 61, false", "100, 99, false", "40, -40, true"})
  @DisplayName("a hunter gives directions at most once every two seconds")
  void rested(final long now, final long last, final boolean rested) {
    assertEquals(rested, HunterReferral.rested(now, last));
  }

  @ParameterizedTest(name = "master at ({0}, {1}) -> {2}")
  @CsvSource({"0, -5, NORTH", "6, 0, EAST", "-3, 3, SOUTH_WEST", "0, 2, SOUTH"})
  @DisplayName("the hunter names their Guild Master and which way he stands")
  void pointsToMaster(final double dx, final double dz, final Bearing bearing) {
    final TranslatableTextContent line =
        (TranslatableTextContent)
            HunterReferral.line(
                    Text.literal("Jadwiga"),
                    Optional.of(new Pointed(Text.literal("Micah"), dx, dz)))
                .getContent();
    assertEquals("nightborn.hunter.referral", line.getKey());
    assertEquals(Text.literal("Jadwiga"), line.getArgs()[0]);
    assertEquals(Text.literal("Micah"), line.getArgs()[1]);
    assertEquals(Text.translatable(bearing.translationKey()), line.getArgs()[2]);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"Jadwiga", "Tomasz"})
  @DisplayName("with no Guild Master near, the hunter sends the player to a camp")
  void noMaster(final String name) {
    final TranslatableTextContent line =
        (TranslatableTextContent)
            HunterReferral.line(Text.literal(name), Optional.empty()).getContent();
    assertEquals("nightborn.hunter.referral.none", line.getKey());
    assertEquals(Text.literal(name), line.getArgs()[0]);
  }
}
