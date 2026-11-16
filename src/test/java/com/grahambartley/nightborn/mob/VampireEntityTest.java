package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mind.MindAttributes;
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
class VampireEntityTest {

  static Stream<Arguments> attributes() {
    return Stream.of(
        Arguments.of(EntityAttributes.GENERIC_MAX_HEALTH, 30.0),
        Arguments.of(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3),
        Arguments.of(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0),
        Arguments.of(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0),
        Arguments.of(MindAttributes.CHARM, 0.0),
        Arguments.of(MindAttributes.THRALL_LIMIT, 0.0));
  }

  @ParameterizedTest(name = "{0} = {1}")
  @MethodSource("attributes")
  @DisplayName("a vampire mob starts with its body and with room for skills to raise its mind")
  void startsWith(final RegistryEntry<EntityAttribute> attribute, final double value) {
    final DefaultAttributeContainer vampires = VampireEntity.createVampireAttributes().build();
    assertEquals(value, vampires.getBaseValue(attribute), 1.0E-9);
  }
}
