package com.dicemc.marketplace.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;

public class ModItems {
	public static final List<Item> ITEMS = new ArrayList<Item>();
	
	public static final Item MONEYBAG = new ItemBase("moneybag");
	public static final Item WHITELISTER = new ItemWhitelister("whitelister");
}
