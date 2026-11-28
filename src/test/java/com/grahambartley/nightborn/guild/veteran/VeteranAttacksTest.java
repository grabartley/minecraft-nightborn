package com.grahambartley.nightborn.guild.veteran;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.mob.brain.ActionReadiness;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VeteranAttacksTest {

  private static final Identifier CHAIN = Identifier.of("nightborn", "silver_chain");
  private static final Identifier VOLLEY = Identifier.of("nightborn", "bolt_volley");

  private static VeteranAttack attack(final VeteranAttack.Kind kind, final int cooldown) {
    return new VeteranAttack(
        kind,
        "clip",
        Identifier.ofVanilla("a"),
        Optional.empty(),
        20,
        cooldown,
        0.0,
        12.0,
        1,
        0.0,
        0,
        0.0F,
        Optional.empty(),
        Optional.empty(),
        false);
  }

  private static Map<Identifier, VeteranAttack> known() {
    final Map<Identifier, VeteranAttack> known = new LinkedHashMap<>();
    known.put(CHAIN, attack(VeteranAttack.Kind.SILVER_CHAIN, 300));
    known.put(VOLLEY, attack(VeteranAttack.Kind.BOLT_VOLLEY, 240));
    return known;
  }

  @ParameterizedTest(name = "now {0}, chain ready at {1}: chain {2}, volley {3}")
  @CsvSource({
    "100, 0, READY, READY",
    "100, 100, READY, READY",
    "100, 101, COOLING, READY",
    "500, 400, READY, READY"
  })
  @DisplayName(
      "each attack the veteran knows is offered to the brain, ready once its cooldown has run,"
          + " with its cooldown for the brain to weigh")
  void offersEachAttack(
      final long now,
      final long chainReady,
      final PowerUse.Verdict chain,
      final PowerUse.Verdict volley) {
    final List<ActionReadiness> offered =
        VeteranAttacks.readiness(known(), Map.of(CHAIN, chainReady), now, true, true);
    assertEquals(
        List.of(
            new ActionReadiness(CHAIN, chain, 300, false),
            new ActionReadiness(VOLLEY, volley, 240, false)),
        offered);
  }

  @ParameterizedTest(name = "free {0}, hunting {1}")
  @CsvSource({"false, true", "true, false", "false, false"})
  @DisplayName(
      "while an attack winds up, or with no living foe, the veteran offers no attack at all, so"
          + " never two at once")
  void offersNothingWhileBusyOrIdle(final boolean free, final boolean hunting) {
    assertEquals(List.of(), VeteranAttacks.readiness(known(), Map.of(), 100, free, hunting));
  }
}
