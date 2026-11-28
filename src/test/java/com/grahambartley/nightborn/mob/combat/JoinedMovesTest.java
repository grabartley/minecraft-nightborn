package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.mob.brain.ActionReadiness;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JoinedMovesTest {

  private static final Identifier HOLY_WATER = Identifier.of("nightborn", "holy_water");
  private static final Identifier CHAIN = Identifier.of("nightborn", "silver_chain");

  private static final class Recorded implements MobFighting.Moves {

    private final Identifier owns;
    private final boolean busy;
    private final List<String> log;
    private final String name;

    Recorded(final String name, final Identifier owns, final boolean busy, final List<String> log) {
      this.name = name;
      this.owns = owns;
      this.busy = busy;
      this.log = log;
    }

    @Override
    public void tick() {
      log.add(name + " tick");
    }

    @Override
    public void rest() {
      log.add(name + " rest");
    }

    @Override
    public List<ActionReadiness> actions() {
      return List.of(ActionReadiness.of(owns, true, 0));
    }

    @Override
    public boolean act(final Identifier action) {
      log.add(name + " asked " + action.getPath());
      return owns.equals(action);
    }

    @Override
    public boolean busy() {
      return busy;
    }
  }

  @ParameterizedTest(name = "acts {0}: done {1}, asked {2}")
  @CsvSource({
    "holy_water, true, base asked holy_water",
    "silver_chain, true, base asked silver_chain|extra asked silver_chain",
    "leap, false, base asked leap|extra asked leap"
  })
  @DisplayName(
      "joined moves offer both sets of actions, and a chosen one goes to the moves that own it,"
          + " the base first")
  void offersBothAndRoutesEachChoice(final String action, final boolean done, final String asked) {
    final List<String> log = new ArrayList<>();
    final JoinedMoves joined =
        new JoinedMoves(
            new Recorded("base", HOLY_WATER, false, log),
            () -> new Recorded("extra", CHAIN, false, log));
    assertEquals(
        List.of(HOLY_WATER, CHAIN), joined.actions().stream().map(ActionReadiness::id).toList());
    assertEquals(done, joined.act(Identifier.of("nightborn", action)));
    assertEquals(List.of(asked.split("\\|")), log);
  }

  @ParameterizedTest(name = "base busy {0}, extra busy {1}: busy {2}")
  @CsvSource({"false, false, false", "true, false, true", "false, true, true", "true, true, true"})
  @DisplayName("joined moves are busy while either part is, and tick and rest both parts in turn")
  void busyWhileEitherIs(final boolean base, final boolean extra, final boolean busy) {
    final List<String> log = new ArrayList<>();
    final JoinedMoves joined =
        new JoinedMoves(
            new Recorded("base", HOLY_WATER, base, log),
            () -> new Recorded("extra", CHAIN, extra, log));
    assertEquals(busy, joined.busy());
    joined.tick();
    joined.rest();
    assertEquals(List.of("base tick", "extra tick", "base rest", "extra rest"), log);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"holy_water"})
  @DisplayName("with no extra moves the base moves are offered alone")
  void noExtra(final String action) {
    final List<String> log = new ArrayList<>();
    final JoinedMoves joined =
        new JoinedMoves(new Recorded("base", HOLY_WATER, false, log), () -> MobFighting.Moves.NONE);
    assertEquals(List.of(HOLY_WATER), joined.actions().stream().map(ActionReadiness::id).toList());
    assertEquals(true, joined.act(Identifier.of("nightborn", action)));
  }
}
