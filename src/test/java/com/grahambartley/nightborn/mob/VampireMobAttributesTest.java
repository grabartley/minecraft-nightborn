package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.clothing.WornAttributes;
import com.grahambartley.nightborn.mind.MindAttributes;
import com.grahambartley.nightborn.occult.OccultAttributes;
import java.util.stream.Stream;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class VampireMobAttributesTest {

  static Stream<Arguments> attributes() {
    return Stream.of(
        Arguments.of(EntityAttributes.GENERIC_MAX_HEALTH, 30.0),
        Arguments.of(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3),
        Arguments.of(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0),
        Arguments.of(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0),
        Arguments.of(MindAttributes.CHARM, 0.0),
        Arguments.of(MindAttributes.THRALL_LIMIT, 0.0),
        Arguments.of(OccultAttributes.FAMILIAR_LIMIT, 0.0),
        Arguments.of(WornAttributes.BLOOD_DRAIN, 1.0),
        Arguments.of(WornAttributes.SUN_DAMAGE, 1.0),
        Arguments.of(WornAttributes.WARD_RESISTANCE, 0.0));
  }

  @ParameterizedTest(name = "{0} = {1}")
  @MethodSource("attributes")
  @DisplayName("a vampire mob starts with its body, room for skills and what its clothes change")
  void startsWith(final RegistryEntry<EntityAttribute> attribute, final double value) {
    final DefaultAttributeContainer vampires = VampireMobAttributes.create().build();
    assertEquals(value, vampires.getBaseValue(attribute), 1.0E-9);
  }
}
