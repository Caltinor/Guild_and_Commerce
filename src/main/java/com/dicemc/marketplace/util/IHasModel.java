package com.dicemc.marketplace.util;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IHasModel {
	public void registerModels();

	void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn);
}
