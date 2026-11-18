package com.grahambartley.nightborn.mob.home;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.servant.Stance;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class HouseholdTieTest {

  private static final UUID LORD = new UUID(0, 7);
  private static final BlockPos POST = new BlockPos(4, 64, -2);

  static Stream<Arguments> ties() {
    return Stream.of(
        Arguments.of("new", (Consumer<HouseholdTie>) tie -> {}),
        Arguments.of("head", (Consumer<HouseholdTie>) HouseholdTie::becomeHead),
        Arguments.of("member", (Consumer<HouseholdTie>) tie -> tie.join(LORD)),
        Arguments.of(
            "guard",
            (Consumer<HouseholdTie>)
                tie -> {
                  tie.join(LORD);
                  tie.order(Stance.GUARD, POST);
                }),
        Arguments.of(
            "provider carrying",
            (Consumer<HouseholdTie>)
                tie -> {
                  tie.join(LORD);
                  tie.sendOut(3, true);
                  tie.fed(3, Optional.of(BloodQuality.VILLAGER), true);
                }));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("ties")
  @DisplayName("a vampire's ties to its household survive a save and load")
  void roundTrip(final String name, final Consumer<HouseholdTie> shape) {
    final HouseholdTie tie = new HouseholdTie();
    shape.accept(tie);
    final NbtCompound nbt = new NbtCompound();
    tie.write(nbt);
    final HouseholdTie loaded = new HouseholdTie();
    loaded.read(nbt);
    assertEquals(tie.head(), loaded.head());
    assertEquals(tie.lord(), loaded.lord());
    assertEquals(tie.stance(), loaded.stance());
    assertEquals(tie.post(), loaded.post());
    assertEquals(tie.provider(), loaded.provider());
    assertEquals(tie.carried(), loaded.carried());
    assertEquals(tie.onErrand(3), loaded.onErrand(3));
    assertEquals(tie.sentOutOn(3), loaded.sentOutOn(3));
  }

  @ParameterizedTest(name = "sent out {0}, fed {1}, asked about {2} -> errand {3}")
  @CsvSource({"4, , 4, true", "4, , 5, false", "4, 4, 4, false", "4, 3, 4, true", ", , 4, false"})
  @DisplayName("an errand lasts its night until the vampire has fed")
  void errand(final Long sent, final Long fed, final long night, final boolean errand) {
    final HouseholdTie tie = new HouseholdTie();
    if (sent != null) {
      tie.sendOut(sent, false);
    }
    if (fed != null) {
      tie.fed(fed, Optional.empty(), false);
    }
    assertEquals(errand, tie.onErrand(night));
  }

  @ParameterizedTest(name = "stores {0}, drawn {1}, already {2} -> {3}")
  @CsvSource({
    "true, ANIMAL, , ANIMAL",
    "false, ANIMAL, , ",
    "true, , , ",
    "true, PLAYER, VILLAGER, VILLAGER"
  })
  @DisplayName("a member brings one bottle home when the house keeps a store")
  void carries(
      final boolean stores,
      final BloodQuality drawn,
      final BloodQuality already,
      final BloodQuality carried) {
    final HouseholdTie tie = new HouseholdTie();
    if (already != null) {
      tie.fed(1, Optional.of(already), true);
    }
    tie.fed(2, Optional.ofNullable(drawn), stores);
    assertEquals(Optional.ofNullable(carried), tie.carried());
    tie.delivered();
    assertEquals(Optional.empty(), tie.carried());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"FOLLOW", "STAY", "GUARD"})
  @DisplayName("an order replaces the standing duty, and joining or leaving clears both")
  void orders(final Stance stance) {
    final HouseholdTie tie = new HouseholdTie();
    tie.becomeHead();
    tie.join(LORD);
    assertEquals(false, tie.head());
    tie.sendOut(2, true);
    tie.order(stance, POST);
    assertEquals(false, tie.provider());
    assertEquals(Optional.of(stance), tie.stance());
    assertEquals(Optional.of(POST), tie.post());
    tie.sendOut(2, false);
    assertEquals(Optional.empty(), tie.stance());
    tie.order(stance, POST);
    tie.leave();
    assertEquals(Optional.empty(), tie.lord());
    assertEquals(Optional.empty(), tie.stance());
  }
}
