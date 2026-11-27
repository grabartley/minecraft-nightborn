package com.grahambartley.nightborn.formview;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FormViewsTest {

  private static FormView view(final String form, final int priority) {
    return new FormView(
        Identifier.of("nightborn", form),
        priority,
        5,
        5,
        1.0F,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());
  }

  private static final List<FormView> VIEWS =
      List.of(view("bat", 0), view("winged", 1), view("mist", 1));

  @ParameterizedTest(name = "forms on [{0}] show {1}")
  @CsvSource({
    "'', none",
    "bat, bat",
    "'bat,winged', winged",
    "winged, winged",
    "mist, mist",
    "other, none"
  })
  @DisplayName("the highest ranked view whose form is on is the one drawn")
  void picksHighestOn(final String on, final String expected) {
    final Set<Identifier> forms =
        Stream.of(on.split(","))
            .filter(name -> !name.isEmpty())
            .map(name -> Identifier.of("nightborn", name))
            .collect(Collectors.toSet());
    assertEquals(
        "none".equals(expected)
            ? Optional.empty()
            : Optional.of(Identifier.of("nightborn", expected)),
        FormViews.pick(VIEWS.stream(), forms::contains).map(FormView::form));
  }

  @Test
  @DisplayName("with no view data loaded there are no views")
  void emptyRegistry() {
    assertEquals(0, FormViews.all(DynamicRegistryManager.EMPTY).count());
  }
}
