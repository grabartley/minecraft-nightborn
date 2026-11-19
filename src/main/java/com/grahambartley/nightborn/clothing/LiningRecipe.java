package com.grahambartley.nightborn.clothing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.recipe.input.SmithingRecipeInput;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

public record LiningRecipe(
    Ingredient template,
    Ingredient addition,
    RegistryEntry<ArmorMaterial> material,
    int durabilityMultiplier)
    implements SmithingRecipe {

  public static final int MOST_DURABILITY = 100;

  @Override
  public boolean matches(final SmithingRecipeInput input, final World world) {
    return testTemplate(input.template())
        && testBase(input.base())
        && testAddition(input.addition());
  }

  @Override
  public ItemStack craft(
      final SmithingRecipeInput input, final RegistryWrapper.WrapperLookup lookup) {
    return testBase(input.base())
        ? Lining.line(input.base(), material.value(), durabilityMultiplier)
        : ItemStack.EMPTY;
  }

  @Override
  public ItemStack getResult(final RegistryWrapper.WrapperLookup lookup) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean testTemplate(final ItemStack stack) {
    return template.test(stack);
  }

  @Override
  public boolean testBase(final ItemStack stack) {
    return Lining.lineable(stack);
  }

  @Override
  public boolean testAddition(final ItemStack stack) {
    return addition.test(stack);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Lining.RECIPE;
  }

  public static final class Serializer implements RecipeSerializer<LiningRecipe> {

    private static final MapCodec<LiningRecipe> CODEC =
        RecordCodecBuilder.mapCodec(
            instance ->
                instance
                    .group(
                        Ingredient.ALLOW_EMPTY_CODEC
                            .fieldOf("template")
                            .forGetter(LiningRecipe::template),
                        Ingredient.DISALLOW_EMPTY_CODEC
                            .fieldOf("addition")
                            .forGetter(LiningRecipe::addition),
                        ArmorMaterial.CODEC.fieldOf("material").forGetter(LiningRecipe::material),
                        Codec.intRange(1, MOST_DURABILITY)
                            .fieldOf("durability_multiplier")
                            .forGetter(LiningRecipe::durabilityMultiplier))
                    .apply(instance, LiningRecipe::new));

    private static final PacketCodec<RegistryByteBuf, LiningRecipe> PACKET_CODEC =
        PacketCodec.tuple(
            Ingredient.PACKET_CODEC,
            LiningRecipe::template,
            Ingredient.PACKET_CODEC,
            LiningRecipe::addition,
            PacketCodecs.registryEntry(RegistryKeys.ARMOR_MATERIAL),
            LiningRecipe::material,
            PacketCodecs.VAR_INT,
            LiningRecipe::durabilityMultiplier,
            LiningRecipe::new);

    @Override
    public MapCodec<LiningRecipe> codec() {
      return CODEC;
    }

    @Override
    public PacketCodec<RegistryByteBuf, LiningRecipe> packetCodec() {
      return PACKET_CODEC;
    }
  }
}
