package com.grahambartley.nightborn.house.crest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.DyeColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CrestBannerItemTest {

  @ParameterizedTest(name = "{0}, {2} on {1}")
  @CsvSource({
    "RAVEN, purple, yellow, nightborn.crest.charge.raven",
    "CROWN, black, black, nightborn.crest.charge.crown"
  })
  @DisplayName("a crested banner names its charge, the charge's colour and the field's")
  void describes(
      final CrestCharge charge, final String field, final String tincture, final String chargeKey) {
    final Text described =
        CrestBannerItem.describe(
            Optional.of(
                new Crest(charge, DyeColor.byName(field, null), DyeColor.byName(tincture, null))));
    final TranslatableTextContent content = (TranslatableTextContent) described.getContent();
    assertEquals(CrestBannerItem.DESCRIBED, content.getKey());
    assertEquals(chargeKey, key(content.getArgs()[0]));
    assertEquals("color.minecraft." + tincture, key(content.getArgs()[1]));
    assertEquals("color.minecraft." + field, key(content.getArgs()[2]));
  }

  @Test
  @DisplayName("a blank banner says it takes the crest of whoever hangs it")
  void blank() {
    assertEquals(
        CrestBannerItem.BLANK,
        ((TranslatableTextContent) CrestBannerItem.describe(Optional.empty()).getContent())
            .getKey());
  }

  private static String key(final Object arg) {
    return ((TranslatableTextContent) ((Text) arg).getContent()).getKey();
  }
}
