package com.dicemc.marketplace.item;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.util.IHasModel;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemWhitelistStick extends Item implements IHasModel{

	public ItemWhitelistStick(String name) {
		setUnlocalizedName(name);
		setRegistryName(name);
		setCreativeTab(CreativeTabs.MATERIALS);
		ModItems.ITEMS.add(this);
	}

	@Override
	public void registerModels() { Main.proxy.registerItemRenderer(this, 0, "inventory"); }
	
}
