package com.grahambartley.nightborn.house;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.servant.Stance;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MemberOrderTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "FOLLOW, nightborn:wheel/follow, FOLLOW",
    "STAY, nightborn:wheel/stay, STAY",
    "GUARD, nightborn:wheel/guard, GUARD",
    "FEED, nightborn:wheel/feed, ",
    "BRING_BLOOD, nightborn:wheel/bring_blood, ",
    "LEAVE, nightborn:wheel/leave, "
  })
  @DisplayName("each order has its own icon, label and cue, and the stances map to theirs")
  void wheel(final MemberOrder order, final String icon, final Stance stance) {
    assertEquals(Identifier.of(icon), order.icon());
    assertEquals(Optional.ofNullable(stance), order.stance());
    final String name = order.name().toLowerCase(java.util.Locale.ROOT);
    assertEquals("nightborn.house.order." + name, order.labelKey());
    assertEquals("nightborn.house.ordered." + name, order.cueKey());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"0, FOLLOW", "4, BRING_BLOOD", "5, LEAVE", "-1, ", "6, "})
  @DisplayName("a wheel choice maps to its order, and anything outside the orders to none")
  void byIndex(final int index, final MemberOrder expected) {
    assertEquals(Optional.ofNullable(expected), MemberOrder.byIndex(index));
  }

  @ParameterizedTest(name = "{0}, held {1}, providing {2} -> {3}")
  @CsvSource({
    "FOLLOW, FOLLOW, false, true",
    "STAY, FOLLOW, false, false",
    "GUARD, , false, false",
    "BRING_BLOOD, , true, true",
    "BRING_BLOOD, , false, false",
    "FEED, , true, false",
    "LEAVE, , false, false"
  })
  @DisplayName("the wheel marks the order a member is carrying out")
  void current(
      final MemberOrder order, final Stance held, final boolean providing, final boolean current) {
    assertEquals(current, order.current(Optional.ofNullable(held), providing));
  }
}
