package com.dicemc.marketplace.item;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class MoneybagCombineRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe{

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		int bagcount = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++) {			
			ItemStack current = inv.getStackInSlot(i);
	        if (current.getCount() <= 0) { continue;}
	        Item item = current.getItem();
			if (item instanceof ItemBase) {bagcount++; continue;}	
			else {return false;}
		}
		return bagcount > 0;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		double valueSum = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			if (inv.getStackInSlot(i).getItem() instanceof ItemBase) valueSum += inv.getStackInSlot(i).getTagCompound().getDouble("value");
		}
		ItemStack result = new ItemStack(ModItems.MONEYBAG);
		result.setTagInfo("value", new NBTTagDouble(valueSum));
		return result;
	}

	@Override
	public boolean canFit(int width, int height) {
		return (width * height) >= 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}
	
	@Override
    public boolean isDynamic()
    {
        return true;
    }
	
}
