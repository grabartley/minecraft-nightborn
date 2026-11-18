package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class TreeRevisionTest {

  static final Identifier TRACKING = Identifier.of("nightborn", "hunter/tracking");
  static final Identifier WARD = Identifier.of("nightborn", "hunter/bite_ward");
  static final Identifier SLAYER = Identifier.of("nightborn", "slayer");

  static TreeProgress bought() {
    return new TreeProgress(9, 0.5F, List.of(), List.of())
        .buy(TRACKING, 1, null)
        .buy(WARD, 2, SLAYER);
  }

  static Stream<Arguments> upgrades() {
    return Stream.of(
        Arguments.of("an old hunter tree", SkillTree.HUNTER, 0, true),
        Arguments.of("a current hunter tree", SkillTree.HUNTER, 1, false),
        Arguments.of("a newer hunter tree", SkillTree.HUNTER, 2, false),
        Arguments.of("a vampire tree", SkillTree.VAMPIRE, 0, false));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("upgrades")
  @DisplayName("a tree saved before its last revision is refunded, keeping every point")
  void upgrade(
      final String name, final SkillTree tree, final int revision, final boolean refunded) {
    final TreeProgress progress = bought();
    final TreeProgress upgraded = TreeRevision.upgrade(progress, revision, tree);
    assertEquals(refunded ? progress.refundAll() : progress, upgraded);
    assertEquals(9, upgraded.earned());
    assertEquals(0.5F, upgraded.experience());
  }

  static Stream<Arguments> saves() {
    return Stream.of(
        Arguments.of(
            "a hunter save from before paths",
            SkillTree.HUNTER,
            "{\"earned\":6,\"bought\":[{\"skill\":\"nightborn:hunter/steeled_blood\",\"paid\":3}]}",
            0,
            6),
        Arguments.of(
            "a hunter save with paths",
            SkillTree.HUNTER,
            "{\"earned\":6,\"bought\":[{\"skill\":\"nightborn:hunter/tracking\",\"paid\":1}],"
                + "\"revision\":1}",
            1,
            5),
        Arguments.of(
            "a vampire save",
            SkillTree.VAMPIRE,
            "{\"earned\":6,\"bought\":[{\"skill\":\"nightborn:core/thick_blood\",\"paid\":1}]}",
            1,
            5));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("saves")
  @DisplayName("loading a save refunds only a tree from before its revision")
  void load(
      final String name,
      final SkillTree tree,
      final String json,
      final int bought,
      final int free) {
    final TreeProgress loaded =
        TreeRevision.codec(tree).parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(bought, loaded.bought().size());
    assertEquals(free, loaded.free());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(SkillTree.class)
  @DisplayName("a saved tree loads back unchanged, stamped with its revision")
  void roundTrip(final SkillTree tree) {
    final JsonElement saved =
        TreeRevision.codec(tree).encodeStart(JsonOps.INSTANCE, bought()).getOrThrow();
    assertEquals(
        tree.revision(),
        saved.getAsJsonObject().has("revision")
            ? saved.getAsJsonObject().get("revision").getAsInt()
            : 0);
    assertEquals(bought(), TreeRevision.codec(tree).parse(JsonOps.INSTANCE, saved).getOrThrow());
  }
}
