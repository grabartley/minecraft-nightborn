package com.grahambartley.nightborn.wheel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.skill.SkillTree;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WheelSubjectTest {

  private record Fixed(String path, boolean allowed) implements WheelAction<Entity> {

    @Override
    public Identifier id() {
      return Identifier.of("nightborn", path);
    }

    @Override
    public boolean allows(final ServerPlayerEntity player, final Entity target) {
      return allowed;
    }

    @Override
    public List<WheelEntry> entries(final ServerPlayerEntity player, final Entity target) {
      return List.of(WheelEntry.of(id(), Identifier.ofVanilla("stick"), Text.literal(path), false));
    }

    @Override
    public void perform(
        final ServerPlayerEntity player, final Entity target, final WheelEntry chosen) {}
  }

  @ParameterizedTest(name = "first allowed {0}, second allowed {1} -> {2} entries")
  @CsvSource({"true, true, 2", "true, false, 1", "false, true, 1", "false, false, 0"})
  @DisplayName("the wheel shows only the actions the player may take")
  void entries(final boolean first, final boolean second, final int shown) {
    final WheelSubject<Entity> subject =
        new WheelSubject<>(Entity.class, SkillTree.VAMPIRE)
            .add(new Fixed("first", first))
            .add(new Fixed("second", second));
    assertEquals(shown, subject.entries(null, null).size());
  }

  @ParameterizedTest(name = "titled {0}")
  @CsvSource({"Brother Aldric (Slayer, Faithful)", "Sister Maud"})
  @DisplayName("a subject may head its wheel with more than the target's name")
  void title(final String heading) {
    final WheelSubject<Entity> subject =
        new WheelSubject<>(Entity.class, SkillTree.HUNTER).titled(target -> Text.literal(heading));
    assertEquals(heading, subject.title(null).getString());
  }

  @ParameterizedTest(name = "allowed {0} -> performed {0}")
  @CsvSource({"true", "false"})
  @DisplayName("a chosen action runs only while the player may still take it")
  void perform(final boolean allowed) {
    final Fixed action = new Fixed("only", allowed);
    final WheelSubject<Entity> subject =
        new WheelSubject<>(Entity.class, SkillTree.HUNTER).add(action);
    assertEquals(allowed, subject.perform(null, null, action.entries(null, null).get(0)));
  }

  @Test
  @DisplayName("an action can be added once, so later features cannot shadow one another")
  void addsOnce() {
    final WheelSubject<Entity> subject =
        new WheelSubject<>(Entity.class, SkillTree.VAMPIRE).add(new Fixed("twice", true));
    assertThrows(IllegalArgumentException.class, () -> subject.add(new Fixed("twice", false)));
    assertTrue(subject.has(Identifier.of("nightborn", "twice")));
  }

  @Test
  @DisplayName("a subject takes only its own kind of entity")
  void cast() {
    final WheelSubject<LivingEntity> subject =
        new WheelSubject<>(LivingEntity.class, SkillTree.HUNTER);
    assertTrue(subject.cast(null).isEmpty());
    assertEquals(SkillTree.HUNTER, subject.theme());
  }
}
