package com.grahambartley.nightborn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModIconAssetsTest {

  private static final Path MANIFEST = Path.of("src/main/resources/fabric.mod.json");
  private static final Path RESOURCE_ROOT = Path.of("src/main/resources");
  private static final String DECLARED_FIELD = "icon";
  private static final int SHIPPED_CANVAS = 512;

  private static Path declaredIcon;
  private static BufferedImage shipped;

  @BeforeAll
  static void loadIcon() throws IOException {
    JsonObject manifest =
        new Gson().fromJson(Files.readString(MANIFEST, StandardCharsets.UTF_8), JsonObject.class);
    assertTrue(manifest.has(DECLARED_FIELD), MANIFEST + " declares no " + DECLARED_FIELD);
    declaredIcon = RESOURCE_ROOT.resolve(manifest.get(DECLARED_FIELD).getAsString());
    if (Files.isRegularFile(declaredIcon)) {
      shipped = ImageIO.read(declaredIcon.toFile());
    }
  }

  @Test
  @DisplayName("the manifest points at an icon file that actually ships")
  void manifestDeclaresAnIconThatExists() {
    assertTrue(Files.isRegularFile(declaredIcon), declaredIcon + " is declared but has no file");
  }

  @Test
  @DisplayName("the shipped icon decodes at the size every mod in this family ships")
  void shippedIconIsDecodableAtTheSizeTheFamilyShips() {
    assertNotNull(shipped, declaredIcon + " could not be decoded as an image");
    assertEquals(SHIPPED_CANVAS, shipped.getWidth(), declaredIcon + " has an unexpected width");
    assertEquals(SHIPPED_CANVAS, shipped.getHeight(), declaredIcon + " has an unexpected height");
  }
}
