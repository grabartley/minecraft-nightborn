package com.grahambartley.nightborn.vomit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MealsTest {

  @ParameterizedTest(name = "{0}, garlic in the other hand {1}")
  @CsvSource({"minecraft:bread, true", "minecraft:bread, false", "minecraft:apple, true"})
  void aMealNamesTheFoodAndTheOtherHand(final String food, final boolean garlic) {
    final Deed deed = Meals.deed(Identifier.of(food), garlic);
    assertEquals(DeedKinds.ATE, deed.kind());
    assertEquals(Optional.of(food), deed.fact(DeedKinds.FOOD));
    assertEquals(Optional.of(Boolean.toString(garlic)), deed.fact(DeedKinds.GARLIC_IN_OTHER_HAND));
  }
}
