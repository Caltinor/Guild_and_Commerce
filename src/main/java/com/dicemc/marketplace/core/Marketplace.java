package com.dicemc.marketplace.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.util.ObjectType;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.MarketSaver;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;


public class Marketplace {
	public Map<UUID, MarketItem> vendList;
	public Map<UUID, List<ItemStack>> itemQueue = new HashMap<UUID, List<ItemStack>>();
	public String marketName;
	public double feeBuy, feeSell; 
	
	private final MarketSaver manager;
	
	public Marketplace (MarketSaver manager, Enum<ObjectType> type) {
		vendList = new HashMap<UUID, MarketItem>();
		this.manager = manager;
		if (type == ObjectType.LOCAL) {
			marketName = "Local Market";
			feeBuy = 0;
			feeSell = 0;
		}
		else if (type == ObjectType.GLOBAL) {
			marketName = "Global Market";
			feeBuy = Main.ModConfig.MARKET_GLOBAL_TAX_BUY;
			feeSell = Main.ModConfig.MARKET_GLOBAL_TAX_SELL;
		}
		else if (type == ObjectType.AUCTION) {
			marketName = "Auction House";
			feeBuy = 0;
			feeSell = Main.ModConfig.MARKET_AUCTION_TAX_SELL;
		}
		else if (type == ObjectType.SERVER) {
			marketName = "Server Market";
			feeBuy = 0.0;
			feeSell = 0.0;
		}
	}
	
	public static UUID unrepeatedUUID(Map<UUID, MarketItem> listIn) {
		UUID output = UUID.randomUUID();
		if (listIn.get(output) != null) return unrepeatedUUID(listIn);
		return output;
	}

	//admin-level addition of items to the list.  no account interactions.
	public void addToList (boolean giveItem, ItemStack item, UUID player, double price, boolean infinite) { 		
		vendList.put(unrepeatedUUID(vendList), new MarketItem(giveItem, item.copy(), player, price, 1, Reference.NIL, Reference.NIL, infinite, System.currentTimeMillis()+Main.ModConfig.AUCTION_OPEN_DURATION));
		manager.markDirty();
	}
	//admin-level removal of items from the list. no account interactions.
	public void removeFromList (UUID index) {
		vendList.remove(index);
		manager.markDirty();
	}
	
	public void addToQueue(UUID recipient, ItemStack item) {
		if (!recipient.equals(Reference.NIL)) {
			List<ItemStack> list = new ArrayList<ItemStack>();
			if (itemQueue.get(recipient) != null) {
				list = itemQueue.get(recipient);
			}			
			list.add(item);
			itemQueue.put(recipient, list);
		}	
	}
	
	public void takeFromQueue(EntityPlayerMP recipient, MinecraftServer server) {
		if (itemQueue.get(recipient.getUniqueID()) != null) {
			List<ItemStack> list = itemQueue.get(recipient.getUniqueID());
			for (int i = 0; i < list.size(); i++) {
				recipient.inventory.addItemStackToInventory(list.get(i));
			}
			itemQueue.remove(recipient.getUniqueID());
		}
	}
	
	public String placeBid(UUID index, UUID buyer, double offer, MinecraftServer server) {
		int acctIndex = -1;
		EntityPlayerMP bidder = server.getPlayerList().getPlayerByUUID(buyer);
		if (bidder != null && vendList.get(index).bidEnd > System.currentTimeMillis()) {
			AccountGroup acctPlayers = AccountSaver.get(server.getEntityWorld()).PLAYERS;
			for (int i = 0; i  < acctPlayers.accountList.size(); i++) {
				if (acctPlayers.accountList.get(i).owner.equals(buyer)) {acctIndex = i; break;}}
			if (acctPlayers.accountList.get(acctIndex).balance >= offer) {
				if (offer > vendList.get(index).price) {
					if (!vendList.get(index).highestBidder.equals(Reference.NIL)) acctPlayers.addBalance(vendList.get(index).highestBidder, vendList.get(index).price);
					acctPlayers.addBalance(buyer, (-1*offer));
					vendList.get(index).price = offer;
					vendList.get(index).highestBidder = buyer;
					return "You are now the highest bidder on this item.";
				}
				else return "You must offer more than the previous bid";
			}
			else return "You do not have enough funds to place your proposed bid.";
		}
		return "This bid has expired";
	}
	
	public String buyItem (UUID index, UUID buyer, MinecraftServer server) {		
		EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(buyer);
		if (player != null) {
			double fee = vendList.get(index).price * feeBuy;		
			String itemStr = vendList.get(index).item.getDisplayName();
			int acctIndex = -1;
			AccountGroup acctPlayers = AccountSaver.get(server.getEntityWorld()).PLAYERS;
			for (int i = 0; i  < acctPlayers.accountList.size(); i++) {
				if (acctPlayers.accountList.get(i).owner.equals(buyer)) {acctIndex = i; break;}}
			if (vendList.get(index).vendorGiveItem) {
				if (acctPlayers.accountList.get(acctIndex).balance >= (fee + vendList.get(index).price)) {
					acctPlayers.accountList.get(acctIndex).balance -= (fee + vendList.get(index).price);
					acctPlayers.addBalance(vendList.get(index).vendor, vendList.get(index).price);
					player.addItemStackToInventory(vendList.get(index).item.copy());
					double printPrice = vendList.get(index).price;
					if (!vendList.get(index).infinite) {
						if (vendList.get(index).vendStock <= 1)	vendList.remove(index);
						else if (vendList.get(index).vendStock > 1) vendList.get(index).vendStock--;
					}
					manager.markDirty();
					return "Purchased " + itemStr + " for $" + String.valueOf(fee + printPrice) + " (fee of $" +String.valueOf(fee)+")";
				}
				else return "Insufficient Funds";
			}
			else if (!vendList.get(index).vendorGiveItem) {
				//sweep player inventory for the item at the amount
				boolean hasItemStock = false;
				int stockCount = vendList.get(index).item.getCount();
				for (int i = 0; i < player.inventoryContainer.inventorySlots.size(); i++) {
					if (player.inventoryContainer.getSlot(i).getStack().getItem().equals(vendList.get(index).item.getItem())) {
						stockCount -= player.inventoryContainer.getSlot(i).getStack().getCount();
					}
					if (stockCount <= 0) {hasItemStock = true; break;}
				}
				if (hasItemStock) {
					stockCount = vendList.get(index).item.getCount();
					for (int i = 0; i < player.inventoryContainer.inventorySlots.size(); i++) {
						if (player.inventoryContainer.getSlot(i).getStack().getItem().equals(vendList.get(index).item.getItem())) {
							int initCount = player.inventoryContainer.getSlot(i).getStack().getCount();
							if (initCount >= stockCount) {
								player.inventoryContainer.getSlot(i).getStack().setCount(initCount - stockCount);
								break;
							}
							else if (player.inventoryContainer.getSlot(i).getStack().getCount() < stockCount) {
								stockCount -= player.inventoryContainer.getSlot(i).getStack().getCount();
								player.inventoryContainer.getSlot(i).putStack(ItemStack.EMPTY);
							}
						}
					}
					if (acctPlayers.accountList.get(acctIndex).balance >= fee) {
						acctPlayers.accountList.get(acctIndex).balance -= fee;
						acctPlayers.accountList.get(acctIndex).balance += vendList.get(index).price;
						addToQueue(vendList.get(index).vendor, vendList.get(index).item);
						double price = vendList.get(index).price;
						if (!vendList.get(index).infinite) vendList.get(index).vendStock -= 1;
						if (vendList.get(index).vendStock <= 0) vendList.remove(index);
						manager.markDirty();
						return "Received $" + String.valueOf(price - fee) + " (fee of $" +String.valueOf(Math.abs(fee)+")");
					}
					else return "Insufficient Funds to complete transaction.";
				}
				else if (!hasItemStock) return "You do not have enough of this item to trade.";
			}
		}	
		else if (player == null) {	
			System.out.println("Purchase attempt failed.["+String.valueOf(index)+"] Buyer was not a player. "+buyer.toString());
		}
		return "";
	}
	public String sellItem (boolean sellerGiveItem, ItemStack item, UUID seller, double price, MinecraftServer server) {
		EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(seller);
		ChunkCapability cap = player.world.getChunkFromChunkCoords(player.chunkCoordX, player.chunkCoordZ).getCapabilities().getCapability(ChunkProvider.CHUNK_CAP, null);
		double cost = price * feeSell;
		int acctIndex = -1;
		if (player != null) {	
			AccountGroup acctPlayers = AccountSaver.get(server.getEntityWorld()).PLAYERS;
			for (int i = 0; i  < acctPlayers.accountList.size(); i++) {
				if (acctPlayers.accountList.get(i).owner.equals(seller)) {acctIndex = i; break;}}
			if (sellerGiveItem) { //this condition means the seller is giving an item such that they receive price in return
				if (acctPlayers.accountList.get(acctIndex).balance >= cost) {
					acctPlayers.accountList.get(acctIndex).balance -= cost;
					vendList.put(unrepeatedUUID(vendList), new MarketItem(sellerGiveItem, item.copy(), seller, price, 1, cap.getOwner(), Reference.NIL, false, System.currentTimeMillis()+Main.ModConfig.AUCTION_OPEN_DURATION));
					player.openContainer.inventorySlots.get(0).putStack(ItemStack.EMPTY);
					manager.markDirty();
					return "Placed " + item.getDisplayName() + " on "+marketName+" for $" + String.valueOf(price) + ". A fee of $"+ String.valueOf(cost) + " was applied.";
				}
				else return "Insufficient Funds to post this sale.  Amount required = $"+String.valueOf(cost);
			}
			else if (!sellerGiveItem) {
				if (acctPlayers.accountList.get(acctIndex).balance >= (cost + price)) {
					acctPlayers.accountList.get(acctIndex).balance -= (cost + price);
					vendList.put(unrepeatedUUID(vendList), new MarketItem(sellerGiveItem, item.copy(), seller, price, 1, cap.getOwner(), Reference.NIL));
					manager.markDirty();
					return "Requested " + item.getDisplayName() + " on "+marketName+" for $" + String.valueOf(price) + ". A fee of $"+ String.valueOf(cost) + " was applied.";
				}
				else return "Insufficient Funds to post this sale.  Amount required = $"+String.valueOf(cost);
			
			}
		}
		return "";
	}
	
	public String increaseStock(UUID index, MinecraftServer server, EntityPlayerMP player) {
		double cost = vendList.get(index).price * feeSell;
		if (vendList.get(index).vendorGiveItem) {
			boolean hasItemStock = false;
			int stockCount = vendList.get(index).item.getCount();
			ItemStack invStack = ItemStack.EMPTY;
			ItemStack refStack = vendList.get(index).item.copy();
			for (int i = 0; i < player.inventoryContainer.inventorySlots.size(); i++) {
				invStack = player.inventoryContainer.getSlot(i).getStack().copy();
				if (invStack.getItem().equals(refStack.getItem())&& ((!refStack.getHasSubtypes() && !invStack.getHasSubtypes()) || refStack.getMetadata() == invStack.getMetadata()) && ItemStack.areItemStackTagsEqual(refStack, invStack)) {
					stockCount -= player.inventoryContainer.getSlot(i).getStack().getCount();
				}
				if (stockCount <= 0) {hasItemStock = true; break;}
			}
			if (hasItemStock && AccountSaver.get(server.getEntityWorld()).PLAYERS.getBalance(player.getUniqueID()) >= cost) {
				stockCount = vendList.get(index).item.getCount();
				for (int i = 0; i < player.inventoryContainer.inventorySlots.size(); i++) {
					invStack = player.inventoryContainer.getSlot(i).getStack().copy();
					if (invStack.getItem().equals(refStack.getItem())&& ((!refStack.getHasSubtypes() && !invStack.getHasSubtypes()) || refStack.getMetadata() == invStack.getMetadata()) && ItemStack.areItemStackTagsEqual(refStack, invStack)) {
						int initCount = player.inventoryContainer.getSlot(i).getStack().getCount();
						if (initCount > stockCount) {
							player.inventoryContainer.getSlot(i).getStack().setCount(initCount - stockCount);
							break;
						}
						else if (player.inventoryContainer.getSlot(i).getStack().getCount() <= stockCount) {
							stockCount -= player.inventoryContainer.getSlot(i).getStack().getCount();
							player.inventoryContainer.getSlot(i).putStack(ItemStack.EMPTY);
						}
					}
				}
				vendList.get(index).vendStock += 1;
				manager.markDirty();
				AccountSaver.get(server.getEntityWorld()).PLAYERS.addBalance(player.getUniqueID(), (-1 * Math.abs(cost)));
				AccountSaver.get(server.getEntityWorld()).markDirty();
				return "Restocked";
			}
			else if (!hasItemStock) return "You do not have enough of this item to add stock.";
			else if (AccountSaver.get(server.getEntityWorld()).PLAYERS.getBalance(player.getUniqueID()) < cost) return "You do not have enough funds.";
		}
		if (!vendList.get(index).vendorGiveItem && AccountSaver.get(server.getEntityWorld()).PLAYERS.getBalance(player.getUniqueID()) >= (cost + vendList.get(index).price)) {
			AccountSaver.get(server.getEntityWorld()).PLAYERS.addBalance(player.getUniqueID(), (-1 * (Math.abs(cost)+vendList.get(index).price)));
			vendList.get(index).vendStock += 1;
			manager.markDirty();
			AccountSaver.get(server.getEntityWorld()).markDirty();
			return "Restocked";
		}
		else return "";
	}
	
	public NBTTagList writeQueueToNBT (NBTTagList nbt) {
		for (Map.Entry<UUID, List<ItemStack>> entry : itemQueue.entrySet()) {
			List<ItemStack> list = entry.getValue();
			NBTTagList lnbt = new NBTTagList();
			for (int i = 0; i < list.size(); i++) {
				lnbt.appendTag(list.get(i).serializeNBT());
			}
			NBTTagCompound snbt = new NBTTagCompound();
			snbt.setUniqueId("UUID", entry.getKey());
			snbt.setTag("itemlist", lnbt);
			nbt.appendTag(snbt);
		}
		return nbt;
	}
	
	public void readQueueFromNBT (NBTTagList nbt) {
		for (int i = 0; i < nbt.tagCount(); i ++) {
			UUID id = nbt.getCompoundTagAt(i).getUniqueId("UUID");
			List<ItemStack> list = new ArrayList<ItemStack>();
			NBTTagList lnbt = nbt.getCompoundTagAt(i).getTagList("itemlist", Constants.NBT.TAG_COMPOUND);
			for (int x = 0; x < lnbt.tagCount(); x++) {
				list.add(new ItemStack(lnbt.getCompoundTagAt(x)));
			}
			itemQueue.put(id, list);
		}
	}
	
	public NBTTagCompound writeToNBT (NBTTagCompound nbt, UUID index) {
		nbt.setUniqueId("index", index);
		nbt.setUniqueId("vendor", vendList.get(index).vendor);
		nbt.setDouble("price", vendList.get(index).price);
		nbt.setBoolean("infinite", vendList.get(index).infinite);
		nbt.setBoolean("giveitem", vendList.get(index).vendorGiveItem);
		nbt.setInteger("stock", vendList.get(index).vendStock);
		nbt.setTag("item", vendList.get(index).item.serializeNBT());
		nbt.setUniqueId("bidder", vendList.get(index).highestBidder);
		nbt.setUniqueId("locality", vendList.get(index).locality);
		nbt.setLong("bidend", vendList.get(index).bidEnd);
		return nbt;
	}
	
	public void readFromNBT(NBTTagList nbt) {
		vendList.clear();
		for (int i = 0; i < nbt.tagCount(); i++) {
			ItemStack item = new ItemStack(nbt.getCompoundTagAt(i).getCompoundTag("item"));
			vendList.put(nbt.getCompoundTagAt(i).getUniqueId("index"), new MarketItem(nbt.getCompoundTagAt(i).getBoolean("giveitem"), item, nbt.getCompoundTagAt(i).getUniqueId("vendor"), nbt.getCompoundTagAt(i).getDouble("price"),nbt.getCompoundTagAt(i).getInteger("stock"), nbt.getCompoundTagAt(i).getUniqueId("locality"), nbt.getCompoundTagAt(i).getUniqueId("bidder"), nbt.getCompoundTagAt(i).getBoolean("infinite"), nbt.getCompoundTagAt(i).getLong("bidend")));
		}
	}
}
