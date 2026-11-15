package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;

@ExtendWith(MinecraftBootstrapExtension.class)
class ClothingRenderProvidersTest {

  @AfterEach
  void reset() {
    ClothingRenderProviders.use(item -> GeoRenderProvider.DEFAULT);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("each piece is drawn by the provider the client installs for it")
  void usesTheInstalledProvider(final ClothingPiece piece) {
    final ClothingItem item = ClothingItems.of(piece);
    final GeoRenderProvider installed = new GeoRenderProvider() {};
    ClothingRenderProviders.use(given -> given == item ? installed : GeoRenderProvider.DEFAULT);
    assertSame(installed, ClothingRenderProviders.forItem(item));
  }
}
