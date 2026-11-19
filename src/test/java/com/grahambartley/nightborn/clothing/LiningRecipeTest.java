package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.inquisitor.InquisitorSpoils;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.input.SmithingRecipeInput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class LiningRecipeTest {

  private static LiningRecipe recipe;

  @BeforeAll
  static void load() throws IOException {
    assertTrue(InquisitorSpoils.MAIL != Items.AIR);
    final JsonObject json =
        JsonParser.parseString(
                Files.readString(
                    Path.of("src/main/resources/data/nightborn/recipe/inquisitor_mail_lining.json"),
                    StandardCharsets.UTF_8))
            .getAsJsonObject();
    json.remove("type");
    recipe =
        new LiningRecipe.Serializer().codec().codec().parse(JsonOps.INSTANCE, json).getOrThrow();
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName(
      "the Inquisitor's mail lines any piece of gothic clothing to diamond, keeping its dye")
  void lines(final ClothingPiece piece) {
    final ItemStack clothing = new ItemStack(ClothingItems.of(piece));
    clothing.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(0x1F4D3A, true));
    final SmithingRecipeInput input =
        new SmithingRecipeInput(ItemStack.EMPTY, clothing, new ItemStack(InquisitorSpoils.MAIL));

    assertTrue(recipe.matches(input, null));
    final ItemStack lined = recipe.craft(input, null);
    assertTrue(Lining.lined(lined));
    assertEquals(
        clothing.get(DataComponentTypes.DYED_COLOR), lined.get(DataComponentTypes.DYED_COLOR));
    assertEquals(ArmorMaterials.DIAMOND, recipe.material());
  }

  @ParameterizedTest(name = "base {0}, addition {1}")
  @CsvSource({"true, false", "false, true", "false, false"})
  @DisplayName("only gothic clothing and the Inquisitor's mail together make a lining")
  void needsBoth(final boolean clothingBase, final boolean mailAddition) {
    final ItemStack base =
        clothingBase
            ? new ItemStack(ClothingItems.of(ClothingPiece.values()[0]))
            : new ItemStack(Items.IRON_CHESTPLATE);
    final ItemStack addition =
        mailAddition ? new ItemStack(InquisitorSpoils.MAIL) : new ItemStack(Items.DIAMOND);
    assertFalse(recipe.matches(new SmithingRecipeInput(ItemStack.EMPTY, base, addition), null));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPiece.class)
  @DisplayName("a piece already lined cannot be lined again")
  void notTwice(final ClothingPiece piece) {
    final ItemStack once =
        recipe.craft(
            new SmithingRecipeInput(
                ItemStack.EMPTY,
                new ItemStack(ClothingItems.of(piece)),
                new ItemStack(InquisitorSpoils.MAIL)),
            null);
    assertFalse(
        recipe.matches(
            new SmithingRecipeInput(ItemStack.EMPTY, once, new ItemStack(InquisitorSpoils.MAIL)),
            null));
  }
}
