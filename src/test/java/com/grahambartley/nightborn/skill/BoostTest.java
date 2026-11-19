package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.slayer.DownBlow;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BoostTest {

  @ParameterizedTest(name = "ends {0}, kill adds {1}, most {2}: ends {3}")
  @CsvSource({"200, 40, 400, 240", "380, 40, 400, 400", "400, 40, 400, 400", "200, 0, 400, 200"})
  @DisplayName("each kill stretches a boost, never past its longest")
  void kills(final long endsAt, final int killTicks, final int mostTicks, final long after) {
    assertEquals(
        after,
        new Boost(0L, endsAt, killTicks, mostTicks, List.of(), Optional.empty()).onKill().endsAt());
  }

  @ParameterizedTest(name = "now {0}, ends {1}: over {2}")
  @CsvSource({"199, 200, false", "200, 200, true", "250, 200, true"})
  @DisplayName("a boost is over once its end is reached")
  void over(final long now, final long endsAt, final boolean over) {
    assertEquals(over, new Boost(0L, endsAt, 0, 0, List.of(), Optional.empty()).over(now));
  }

  @ParameterizedTest(name = "now {0}, {1} ticks, kill {2}, most {3}")
  @CsvSource({"100, 200, 40, 400, 300, 400", "0, 200, 0, 100, 200, 200"})
  @DisplayName("a boost from a power starts now and is never shorter than its first span")
  void fromPower(
      final long now,
      final int duration,
      final int kill,
      final int most,
      final long endsAt,
      final int longest) {
    final Boost boost =
        Boost.of(
            now,
            new BoostPower(duration, List.of(), kill, most, Optional.empty(), Optional.empty()));
    assertEquals(endsAt, boost.endsAt());
    assertEquals(longest, boost.mostTicks());
  }

  @ParameterizedTest(name = "downs below {0}: carried {1}")
  @CsvSource({"0.3, true", "0, false"})
  @DisplayName("a boost keeps the downing blow its power gives, through a kill")
  void downs(final float below, final boolean carried) {
    final Optional<DownBlow> blow =
        carried
            ? Optional.of(new DownBlow(below, Identifier.of("nightborn:elder")))
            : Optional.empty();
    final Boost boost =
        Boost.of(100L, new BoostPower(120, List.of(), 40, 400, Optional.empty(), blow));
    assertEquals(blow, boost.downs());
    assertEquals(blow, boost.onKill().downs());
  }
}
