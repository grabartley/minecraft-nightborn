package com.grahambartley.nightborn.tools.structures.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.List;
import org.junit.jupiter.api.Test;

class VampireStructuresTest {

  @Test
  void buildsEveryVampirePieceOnce() {
    assertEquals(
        List.of(
            "vampire/crypt/undercroft",
            "vampire/crypt/ossuary",
            "vampire/crypt/church_tower",
            "vampire/crypt/church_bellcote",
            "vampire/lair/den",
            "vampire/lair/grotto"),
        VampireStructures.build().stream().map(Template::path).toList());
  }
}
