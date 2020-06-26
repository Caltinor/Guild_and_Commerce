package com.dicemc.marketplace.core;

import java.util.UUID;

import com.dicemc.marketplace.util.Reference;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class MarketItem{
	public ItemStack item;
	public int vendStock;
	public UUID vendor;
	public UUID locality;  //for defining which guild's local shop this applies to.
	public UUID highestBidder;
	public long bidEnd;
	public double price;
	public boolean vendorGiveItem, infinite;
	
	public MarketItem (boolean giveItem, ItemStack item, UUID vendor, double price, int stock, UUID sourceLoc, UUID bidder) {
		highestBidder = bidder;
		locality = sourceLoc;
		vendorGiveItem = giveItem;
		this.item = item;
		this.vendor = vendor;
		this.price = price;
		vendStock = stock;
		infinite = false;
	}
	
	public MarketItem (boolean giveItem, ItemStack item, UUID vendor, double price, int stock, UUID sourceLoc, UUID bidder, boolean infinite, long bidEnd) {
		this.bidEnd = bidEnd;
		highestBidder = bidder;
		locality = sourceLoc;
		vendorGiveItem = giveItem;
		this.item = item;
		this.vendor = vendor;
		this.price = price;
		vendStock = stock;
		this.infinite = infinite;
	}
}



