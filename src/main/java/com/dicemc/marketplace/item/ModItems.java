package com.dicemc.marketplace.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;

public class ModItems {
	public static final List<Item> ITEMS = new ArrayList<Item>();
	
	public static final Item MONEYBAG = new ItemMoneybag("moneybag");
	public static final Item WHITELISTER = new ItemWhitelister("whitelister");
	public static final Item WL_STICK_GREEN = new ItemWhitelistStick("wl_stick_green");
	public static final Item WL_STICK_RED = new ItemWhitelistStick("wl_stick_red");
}
