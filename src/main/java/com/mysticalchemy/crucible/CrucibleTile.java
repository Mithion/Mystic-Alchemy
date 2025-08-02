package com.mysticalchemy.crucible;

import com.mysticalchemy.api.events.CrucibleEvent;
import com.mysticalchemy.config.BrewingConfig;
import com.mysticalchemy.event.EventDispatcher;
import com.mysticalchemy.init.BlockInit;
import com.mysticalchemy.init.RecipeInit;
import com.mysticalchemy.init.TileEntityInit;
import com.mysticalchemy.recipe.PotionIngredientRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Optional;

public class CrucibleTile extends BlockEntity {

	private static final int UPDATE_RATE = 10;
	public static final float MIN_TEMP = 0f;
	public static final float MAX_TEMP = 200f;
	public static final float BOIL_POINT = 100f;
	public static final float ITEM_HEAT_LOSS = 25f;
	public static final int MAX_MAGNITUDE = 3; // TODO: configurable, per potion effect
	public static final int MAX_EFFECTS = 4;
	public static final int  MAX_DURATION = 9600; //8 minutes

	private static HashMap<Block, Float> heaters;

	static {
		heaters = new HashMap<Block, Float>();
		heaters.put(Blocks.CAMPFIRE, 1.0f);
		heaters.put(Blocks.FIRE, 2.0f);
		heaters.put(Blocks.LAVA, 5.0f);
		heaters.put(Blocks.ICE, -2.0f);
	}

	private float heat = MIN_TEMP;
	private float stir = 0;
	private boolean is_splash = false;
	private boolean is_lingering = false;
	private int duration = 600; // default 30 second duration

	private HashMap<MobEffect, Float> effectStrengths;
	private RecipeManager recipeManager;
	private Biome myBiome;

	private long targetColor = 12345L;
	private long startColor = 12345L;
	private double infusePct = 1.0f;

	public CrucibleTile(BlockPos pos, BlockState state) {
		super(TileEntityInit.CRUCIBLE_TILE_TYPE.get(), pos ,state);
		effectStrengths = new HashMap<MobEffect, Float>();
	}

	// ------------------------------------------------------------------
	// Logic / Utilities
	// ------------------------------------------------------------------
	
	public static void Tick(Level level, BlockPos pos, BlockState state, CrucibleTile blockEntity) { 
		blockEntity.tick();
	}
	
	private void tick() {		
		int waterLevel = this.getBlockState().getValue(BlockCrucible.LEVEL);
		
		//particles and color, client only
		if (level.isClientSide && waterLevel > 0) {
			spawnParticles(waterLevel);
		}
		
		// lerp color change over 100 ticks, or 5 seconds.
		infusePct = Mth.clamp(infusePct + 0.01f, 0, 1);

		//limit updates to 2/second
		if (level.getGameTime() % UPDATE_RATE != 0) {
			return;
		}
		
		//ensure biome is set
		if (myBiome == null) {
			myBiome = this.level.getBiome(worldPosition).get();
		}

		//reset condition
		if (waterLevel == 0) {
			resetPotion();
			return;
		}

		//main update logic
		if (!level.isClientSide()){
			tickHeatAndStir(waterLevel);
		}
	}

	private void tickHeatAndStir(int waterLevel) {
		Block below = level.getBlockState(getBlockPos().below()).getBlock();
		float preHeat = heat;
		if (heaters.containsKey(below)) {
			heat = Mth.clamp(heat + heaters.get(below), MIN_TEMP, MAX_TEMP);
		} else {
			heat = Mth.clamp(heat - (1 - myBiome.getBaseTemperature()) * 10, MIN_TEMP, MAX_TEMP);
		}

		if (stir > 0.25f)
			stir = Mth.clamp(stir - 0.02f, 0, 1);
		else
			stir = Mth.clamp(stir - 0.005f, 0, 1);

		if (heat >= BOIL_POINT && stir == 0 && Math.random() < 0.1f) {
			if (waterLevel == 1) {
				level.setBlockAndUpdate(getBlockPos(), BlockInit.EMPTY_CRUCIBLE.get().defaultBlockState());
			}else {
				level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(BlockCrucible.LEVEL, waterLevel - 1));
			}
		}

		if (heat != preHeat)
			level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
	}

	private void resetPotion() {
		heat = MIN_TEMP;
		stir = 1.0f;
		is_splash = false;
		is_lingering = false;
		duration = 600;
		effectStrengths.clear();
		infusePct = 1.0f;
		targetColor = 12345L;
	}

	private void spawnParticles(int waterLevel) {
		if (this.getHeat() > BOIL_POINT) {
			int numBubbles = (int) Math.ceil(5f * ((this.getHeat() - BOIL_POINT) / (this.getMaxHeat() - BOIL_POINT)));
	
			for (int i = 0; i < numBubbles; ++i)
				level.addParticle(ParticleTypes.BUBBLE_POP, getBlockPos().getX() + 0.5 - 0.3 + Math.random() * 0.6,
						getBlockPos().getY() + 0.2 + (0.25f * waterLevel), getBlockPos().getZ() + 0.5 - 0.3 + Math.random() * 0.6, 0,
						is_splash ? 0.125f : 0, 0);
		
		
			if (stir < 0.25f) {
				level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getBlockPos().getX() + 0.5 - 0.3 + Math.random() * 0.6,
						getBlockPos().getY() + 0.2 + (0.25f * waterLevel), getBlockPos().getZ() + 0.5 - 0.3 + Math.random() * 0.6, 0,
						0.01f + (0.25 - stir) * 0.25f, 0);
			}
		}
	}

	public boolean tryAddIngredient(ItemStack stack) {
		if (infusePct != 1.0f || heat < BOIL_POINT)
			return false;
		
		if (recipeManager == null) {
			recipeManager = level.getRecipeManager();
		}
		if (recipeManager == null) {
			return false;
		}

		Optional<PotionIngredientRecipe> recipe = recipeManager
				.getRecipesFor(RecipeInit.POTION_RECIPE_TYPE.get(), createDummyCraftingInventory(stack), level).stream()
				.findFirst();

		if (recipe.isEmpty()) {
			return false;
		}

		PotionIngredientRecipe resolved_recipe = recipe.get();
		CrucibleEvent.AddIngredient event = EventDispatcher.DispatchCrucibleAddIngredientEvent(this.getAllEffects(), resolved_recipe, stack);
		if (event.getResult() == Event.Result.DENY) {
			return false;
		}

		if (!canMerge(recipe.get(), stack.getCount()) && event.getResult() != Event.Result.ALLOW) {
			return false;
		}

		//event may have modified the recipe
		resolved_recipe = event.getRecipe();

		//handle various properties
		if (resolved_recipe.getMakesLingering())
			is_lingering = true;
		if (resolved_recipe.getMakesSplash())
			is_splash = true;
		if (resolved_recipe.getDurationAdded() > 0)
			duration += resolved_recipe.getDurationAdded() * stack.getCount();

		heat = Mth.clamp(heat - ITEM_HEAT_LOSS * stack.getCount(), MIN_TEMP, MAX_TEMP);

		//effects
		mergeEffects(resolved_recipe.getEffects(), stack.getCount());
		recalculatePotionColor();
		level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
		return true;
	}

	private void recalculatePotionColor() {
		HashMap<MobEffect, Float> prominents = getProminentEffects();
		if (prominents.size() == 0) {
			targetColor = 12345L;
			infusePct = 1.0f;
			return;
		}

		long color = 0;
		for (MobEffect e : prominents.keySet()) {
			color += e.getColor();
		}

		color /= prominents.size();
		if (targetColor != color) {
			startColor = getPotionColor();
			targetColor = color;
			infusePct = 0.0f;
		}
	}

	private boolean canMerge(PotionIngredientRecipe recipe, int quantity) {
		HashMap<MobEffect, Float> prominent = getProminentEffects();
		
		for (MobEffect e : recipe.getEffects().keySet()) {
			if (e == null) continue;
			
			//max effects
			if (prominent.size() >= MAX_EFFECTS) {
				float newMagnitude = effectStrengths.containsKey(e) ? effectStrengths.get(e) : 0;
				newMagnitude += recipe.getEffects().get(e) * quantity;
				//will the new effect make a new prominent?
				if (!prominent.containsKey(e) && newMagnitude >= 1.0f) {
					return false;
				}
			}
			
			//magnitude conditions
			if (effectStrengths.containsKey(e) && (effectStrengths.get(e) + recipe.getEffects().get(e) * quantity) > MAX_MAGNITUDE)
				return false;
			else if (recipe.getEffects().get(e) * quantity > MAX_MAGNITUDE)
				return false;
		}
		
		//duration condition
		if (recipe.getDurationAdded() * quantity + duration > MAX_DURATION)
			return false;

		return true;
	}

	private void mergeEffects(HashMap<MobEffect, Float> effectList, int quantity) {
		effectList.forEach((e, f) -> {
			if (e != null)
			{			
				ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(e);
				if (key != null && BrewingConfig.isEffectDisabled(key)) return;
				
				if (effectStrengths.containsKey(e))
					effectStrengths.put(e, Math.min(effectStrengths.get(e) + (f * quantity), MAX_MAGNITUDE));
				else
					effectStrengths.put(e, Math.min((f * quantity), MAX_MAGNITUDE));
			}
		});
	}

	private CraftingContainer createDummyCraftingInventory(ItemStack stack) {
		CraftingContainer craftinginventory = new TransientCraftingContainer(new AbstractContainerMenu((MenuType<?>) null, -1) {
			@Override
			public boolean stillValid(Player playerIn) {
				return false;
			}

			@Override
			public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
				return ItemStack.EMPTY;
			}
		}, 1, 1);
		
		craftinginventory.setItem(0, stack);

		return craftinginventory;
	}

	public HashMap<MobEffect, Float> getProminentEffects() {
		HashMap<MobEffect, Float> effects = new HashMap<MobEffect, Float>();
		effectStrengths.forEach((e, f) -> {
			if (f >= 1 && !BrewingConfig.isEffectDisabled(ForgeRegistries.MOB_EFFECTS.getKey(e)))
				effects.put(e, f);
		});

		return effects;
	}

	@SuppressWarnings("unchecked")
	public HashMap<MobEffect, Float> getAllEffects(){
		return (HashMap<MobEffect, Float>) effectStrengths.clone();
	}
	
	// ------------------------------------------------------------------
	// Save / Load
	// ------------------------------------------------------------------	
	
	@Override
	protected void saveAdditional(CompoundTag compound) {		
		super.saveAdditional(compound);
		
		compound.putFloat("heat", this.heat);
		compound.putFloat("stir", this.stir);
		compound.putBoolean("splash", this.is_splash);
		compound.putBoolean("lingering", this.is_lingering);
		compound.putInt("duration", this.duration);
		compound.putInt("numEffects", this.effectStrengths.size());
		int count = 0;
		for (MobEffect e : this.effectStrengths.keySet()) {
			compound.putString("effect" + count, ForgeRegistries.MOB_EFFECTS.getKey(e).toString());
			compound.putFloat("effectstr" + count, this.effectStrengths.get(e));
			count++;
		}
	}
	
	@Override
	public void load(CompoundTag data) {
		super.load(data);

		if (data.contains("heat"))
			this.heat = data.getFloat("heat");

		if (data.contains("stir"))
			this.stir = data.getFloat("stir");

		if (data.contains("splash"))
			this.is_splash = data.getBoolean("splash");

		if (data.contains("lingering"))
			this.is_lingering = data.getBoolean("lingering");

		if (data.contains("duration"))
			this.duration = data.getInt("duration");

		if (data.contains("numEffects")) {
			int count = data.getInt("numEffects");
			for (int i = 0; i < count; ++i) {
				if (data.contains("effect" + i) && data.contains("effectstr" + i)) {
					MobEffect e = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(data.getString("effect" + i)));
					if (e != null) {
						effectStrengths.put(e, data.getFloat("effectstr" + i));
					}
				}
			}
		}
		
		recalculatePotionColor();
	}

	// ------------------------------------------------------------------
	// Getters / Setters
	// ------------------------------------------------------------------

	public float getHeat() {
		return heat;
	}

	public float getMaxHeat() {
		return MAX_TEMP;
	}

	public void stir() {
		stir = 1.0f;
	}

	public float getStir() {
		return stir;
	}

	public boolean isSplash() {
		return is_splash;
	}

	public void setSplash(boolean splash) {
		this.is_splash = splash;
	}

	public boolean isLingering() {
		return is_lingering;
	}

	public void setLingering(boolean lingering) {
		this.is_lingering = lingering;
	}

	public int getDuration() {
		return this.duration;
	}

	public void addDuration(int duration) {
		this.duration += duration;
	}

	public boolean isPotion() {
		for (MobEffect e : effectStrengths.keySet()) {
			if (effectStrengths.get(e) >= 1.0f)
				return true;
		}

		return false;
	}

	public long getPotionColor() {
		if (infusePct == 1.0f)
			return targetColor;
		
		int[] rgb_start = new int[] {
			(int) (startColor >> 16 & 0xff),
			(int) (startColor >> 8 & 0xff),
			(int) (startColor & 0xff)
		};
		
		int[] rgb_target = new int[] {
			(int) (targetColor >> 16 & 0xff),
			(int) (targetColor >> 8 & 0xff),
			(int) (targetColor & 0xff)
		};
		
		int[] lerp_color = new int[3];
		for (int i = 0; i < 3; ++i)
			lerp_color[i] = rgb_start[i] + (int)((rgb_target[i] - rgb_start[i]) * infusePct);
		
		long outputColor = 0;
		outputColor += lerp_color[0] << 16;
		outputColor += lerp_color[1] << 8;
		outputColor += lerp_color[2];
		
		return outputColor;
	}
	
	// ------------------------------------------------------------------
	// Network
	// ------------------------------------------------------------------

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		saveAdditional(tag);
		return tag;
	}	

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		load(pkt.getTag());
	}
}
