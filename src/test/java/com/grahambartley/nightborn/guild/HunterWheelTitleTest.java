package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HunterWheelTitleTest {

  @ParameterizedTest(name = "{0} with {1} -> {2}")
  @CsvSource({
    "Aldric, 'slayer, faithful', 'skill_branch.nightborn.slayer, skill_branch.nightborn.faithful'",
    "Maud, 'seeker, trapper', 'skill_branch.nightborn.seeker, skill_branch.nightborn.trapper'",
    "Ivo, trapper, skill_branch.nightborn.trapper"
  })
  @DisplayName("a hunter's wheel is headed with its name and its paths, in the order it took them")
  void namesThePaths(final String name, final String paths, final String keys) {
    final Text title =
        HunterWheelTitle.of(
            Text.literal(name),
            Arrays.stream(paths.split(", "))
                .map(path -> Identifier.of("nightborn", path))
                .toList());
    final TranslatableTextContent content = (TranslatableTextContent) title.getContent();
    assertEquals("nightborn.hunter.wheel_title", content.getKey());
    assertEquals(name, ((Text) content.getArgs()[0]).getString());
    assertEquals(
        Arrays.asList(keys.split(", ")),
        ((Text) content.getArgs()[1])
            .getSiblings().stream()
                .map(Text::getContent)
                .filter(TranslatableTextContent.class::isInstance)
                .map(part -> ((TranslatableTextContent) part).getKey())
                .toList());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"Aldric", "Maud"})
  @DisplayName("a hunter with no paths yet is headed with its name alone")
  void withoutPaths(final String name) {
    assertEquals(name, HunterWheelTitle.of(Text.literal(name), List.of()).getString());
  }
}
