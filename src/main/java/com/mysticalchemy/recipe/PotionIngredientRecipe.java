package com.mysticalchemy.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.init.RecipeInit;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.ArrayList;
import java.util.HashMap;

public class PotionIngredientRecipe extends CustomRecipe {

	private ArrayList<Item> matchItems;
	private HashMap<MobEffect, Float> effects;
	private boolean makesSplash = false;
	private boolean makesLingering = false;
	private int durationAdded = 0;
	
	private ResourceLocation tagResource = null;
	
	public PotionIngredientRecipe(ResourceLocation idIn) {
		super(idIn, CraftingBookCategory.MISC);
		matchItems = new ArrayList<Item>();
		effects = new HashMap<MobEffect, Float>();
	}
	
	@Override
	public boolean matches(CraftingContainer inv, Level worldIn) {
		if (inv.getContainerSize() == 1) {
			return getMatchItems().contains(inv.getItem(0).getItem());
		}
		return false;
	}

	@Override
	public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
		return new ItemStack(Items.POTION);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width == 1 && height == 1;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeInit.POTION_INGREDIENT_SERIALIZER.get();
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeInit.POTION_RECIPE_TYPE.get();
	}
	
	public HashMap<MobEffect,Float> getEffects(){		
		return effects;
	}

	public void setEffects(HashMap<MobEffect,Float> effects) {
		this.effects = effects;
	}
	
	public ArrayList<Item> getMatchItems(){
		if (tagResource != null) {
			ITag<Item> tag = ForgeRegistries.ITEMS.tags().getTag(ForgeRegistries.ITEMS.tags().createTagKey(tagResource));
			if (tag != null) {
				tag.iterator().forEachRemaining(i -> {
					matchItems.add(i);
				});
			}
			tagResource = null;			
		}
		
		return matchItems;
	}
	
	public boolean getMakesSplash() {
		return makesSplash;
	}
	
	public boolean getMakesLingering() {
		return makesLingering;
	}
	
	public int getDurationAdded() {
		return durationAdded;
	}
	
	public static class Serializer implements RecipeSerializer<PotionIngredientRecipe>
	{		
		@Override
		public PotionIngredientRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
			PotionIngredientRecipe recipe = new PotionIngredientRecipe(recipeId);
			
			if (json.has("item")) {
				recipe.matchItems.add( ForgeRegistries.ITEMS.getValue(new ResourceLocation(json.get("item").getAsString())) );
			}else if (json.has("tag")) {
				recipe.tagResource = new ResourceLocation(json.get("tag").getAsString());
			}else {
				MysticAlchemy.LOGGER.error("Potion Ingredient Recipe is missing item or tag field");
			}
			
			if (json.has("effects")) {
				JsonElement effectsElem = json.get("effects");
				if (effectsElem.isJsonArray()) {
					JsonArray elements = effectsElem.getAsJsonArray();
					elements.forEach(e -> {
						if (e.isJsonObject()) {
							JsonObject eObj = e.getAsJsonObject();
							if (eObj.has("effect") && eObj.has("strength")) {
								MobEffect eff = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(eObj.get("effect").getAsString()));
								if (eff != null)
									recipe.effects.put(eff, eObj.get("strength").getAsFloat());
								else
									MysticAlchemy.LOGGER.warn("Failed to resolve potion effect " + eObj.get("effect").getAsString() + " in " + recipeId.toString());
							}else {
								MysticAlchemy.LOGGER.error("Potion Ingredient Recipe effects element is missing effect or strength property");
							}
						}else {
							MysticAlchemy.LOGGER.error("Potion Ingredient Recipe effects element is not a json object");
						}
					});
				}else {
					MysticAlchemy.LOGGER.error("Potion Ingredient Recipe effects field is not an array");
				}
			}else {
				MysticAlchemy.LOGGER.error("Potion Ingredient Recipe is missing effects field");
			}
			
			if (json.has("duration_added"))
				recipe.durationAdded = json.get("duration_added").getAsInt();
			
			if (json.has("splash_catalyst"))
				recipe.makesSplash = json.get("splash_catalyst").getAsBoolean();
			
			if (json.has("lingering_catalyst"))
				recipe.makesLingering = json.get("lingering_catalyst").getAsBoolean();
			
			return recipe;
		}
		
		@Override
		public PotionIngredientRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
			PotionIngredientRecipe recipe = new PotionIngredientRecipe(recipeId);
			
			recipe.makesLingering = buffer.readBoolean();
			recipe.makesSplash = buffer.readBoolean();
			recipe.durationAdded = buffer.readInt();
			
			boolean hasTagResource = buffer.readBoolean();
			if (hasTagResource)
				recipe.tagResource = buffer.readResourceLocation();			
			
			int numItems = buffer.readInt();
			for (int i = 0; i < numItems; ++i)
				recipe.matchItems.add(ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation()));
			
			int numEffects = buffer.readInt();
			for (int i = 0; i < numEffects; ++i)
				recipe.effects.put(ForgeRegistries.MOB_EFFECTS.getValue(buffer.readResourceLocation()), buffer.readFloat());
			
			return recipe;
		}
		
		@Override
		public void toNetwork(FriendlyByteBuf buffer, PotionIngredientRecipe recipe) {
			buffer.writeBoolean(recipe.makesLingering);
			buffer.writeBoolean(recipe.makesSplash);
			buffer.writeInt(recipe.durationAdded);
			
			buffer.writeBoolean(recipe.tagResource != null);
			if (recipe.tagResource != null)
				buffer.writeResourceLocation(recipe.tagResource);
			
			buffer.writeInt(recipe.matchItems.size());
			recipe.matchItems.forEach(i -> buffer.writeResourceLocation(ForgeRegistries.ITEMS.getKey(i)));
			
			buffer.writeInt(recipe.effects.size());
			recipe.effects.forEach((e,f) -> {
				buffer.writeResourceLocation(ForgeRegistries.MOB_EFFECTS.getKey(e));
				buffer.writeFloat(f);
			});
		}

	}
}
