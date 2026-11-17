package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GiftModifierTest {

  @ParameterizedTest(name = "{0} at strength {1}, slot {2}: {3}")
  @CsvSource({"3.0, 1.0, 0, 3.0", "3.0, 0.5, 0, 1.5", "0.2, 0.5, 1, 0.1", "12.0, 0.5, 2, 6.0"})
  @DisplayName("a gift's modifier scales with its strength and has its own id per slot")
  void modifier(final double amount, final float strength, final int index, final double given) {
    final EntityAttributeModifier modifier =
        new GiftModifier(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                amount,
                EntityAttributeModifier.Operation.ADD_VALUE)
            .modifier(index, strength);
    assertEquals(given, modifier.value(), 1.0E-6);
    assertEquals(Identifier.of("nightborn", "blood_gift/" + index), modifier.id());
    assertEquals(EntityAttributeModifier.Operation.ADD_VALUE, modifier.operation());
  }
}
