package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.core.Marketplace;
import com.dicemc.marketplace.gui.ContainerSell;
import com.dicemc.marketplace.util.MktPktType;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.commands.Commands;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;
import com.dicemc.marketplace.util.datasaver.MarketSaver;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class MessageMarketsToServer implements IMessage{
	public MktPktType action;
	public int market; //0=local; 1=global; 2=auction;
	public UUID index;
	public ItemStack item;
	public double price;
	public boolean giveItem;
	
	public MessageMarketsToServer() {}
	
	public MessageMarketsToServer(MktPktType action, int market, UUID index, ItemStack item, double price, boolean giveItem) {
		this.action = action;
		this.market = market;
		this.index = index;
		this.item = item;
		this.price = price;
		this.giveItem = giveItem;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		action = MktPktType.values()[pbuf.readVarInt()];
		market = pbuf.readInt();
		index = pbuf.readUniqueId();
		price = pbuf.readDouble();
		giveItem = pbuf.readBoolean();
		try {item = new ItemStack(pbuf.readCompoundTag());} catch (IOException e) {}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeVarInt(action.ordinal());
		pbuf.writeInt(market);
		pbuf.writeUniqueId(index);
		pbuf.writeDouble(price);
		pbuf.writeBoolean(giveItem);
		pbuf.writeCompoundTag(item.serializeNBT());
	}
	
	public static class PacketMarketsToServer implements IMessageHandler<MessageMarketsToServer, IMessage> {
		@Override
		public IMessage onMessage(MessageMarketsToServer message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageMarketsToServer message, MessageContext ctx) {
			switch (message.action) {
			case LOCAL: {
				UUID locality = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(ctx.getServerHandler().player.chunkCoordX, ctx.getServerHandler().player.chunkCoordZ).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner();
				Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal();
				double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
				Main.NET.sendTo(new MessageMarketsToGui(true, 0, market.vendList, locality, market.feeBuy, market.feeSell, balP, ""), ctx.getServerHandler().player);
				break;
			}
			case GLOBAL: {
				Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal();
				double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
				Main.NET.sendTo(new MessageMarketsToGui(true, 1, market.vendList, Reference.NIL, market.feeBuy, market.feeSell, balP, ""), ctx.getServerHandler().player);
				break;
			}
			case AUCTION: {
				Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction();
				double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
				Main.NET.sendTo(new MessageMarketsToGui(true, 2, market.vendList, Reference.NIL, market.feeBuy, market.feeSell, balP, ""), ctx.getServerHandler().player);
				break;
			}
			case SERVER: {
				Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getServer();
				double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
				Main.NET.sendTo(new MessageMarketsToGui(true, 4, market.vendList, Reference.NIL, market.feeBuy, market.feeSell, balP, ""), ctx.getServerHandler().player);
				break;
			}
			case PERSONAL: {
				UUID emptyID = UUID.randomUUID();
				personalList(message, ctx, emptyID);
				break;
			}
			case BUY: {
				UUID locality = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(ctx.getServerHandler().player.chunkCoordX, ctx.getServerHandler().player.chunkCoordZ).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner();
				String resp = "";
				switch (message.market) {
				case 0: {
					Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal();
					if (market.vendList.get(message.index) != null) {
						if (!market.vendList.get(message.index).locality.equals(locality)) {
							int gIdx = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).guildIndexFromUUID(market.vendList.get(message.index).locality);
							Guild guild = (gIdx >= 0) ? GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS.get(gIdx) : null;
							String loc = guild == null ? "Unowned" : guild.guildName;
							if (gIdx >= 0) {
								ChunkPos nearest = guild.coreLand.get(0);
								ChunkPos myLoc = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(ctx.getServerHandler().player.chunkCoordX, ctx.getServerHandler().player.chunkCoordZ).getPos();
								double distance = distanceCalc(myLoc, nearest);
								for (int i = 0; i < guild.coreLand.size(); i++) {
									if (distance > distanceCalc(myLoc, guild.coreLand.get(i))) {
										nearest = guild.coreLand.get(i);
										distance = distanceCalc(myLoc, guild.coreLand.get(i));
									}
								}
								for (int i = 0; i < guild.outpostLand.size(); i++) {
									if (distance > distanceCalc(myLoc, guild.outpostLand.get(i))) {
										nearest = guild.outpostLand.get(i);
										distance = distanceCalc(myLoc, guild.outpostLand.get(i));
									}
								}
								loc += "("+String.valueOf(nearest.x)+","+String.valueOf(nearest.z)+")";
							}
							resp = "Must Be in "+loc+" territory to purchase";
							break;
						}
						else {
							resp = market.buyItem(message.index, ctx.getServerHandler().player.getUniqueID(), ctx.getServerHandler().player.getServer());
						}
					}
					else {
						resp = "Item No Longer Listed";
						break;
					}
					double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
					Main.NET.sendTo(new MessageMarketsToGui(true, 0, market.vendList, locality, market.feeBuy, market.feeSell, balP, resp), ctx.getServerHandler().player);
					break;
				}
				case 1: {
					Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal();
					if (market.vendList.get(message.index) != null) {
						resp = market.buyItem(message.index, ctx.getServerHandler().player.getUniqueID(), ctx.getServerHandler().player.getServer());
					}
					else resp = "Item No Longer Listed";
					double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
					Main.NET.sendTo(new MessageMarketsToGui(true, 1, market.vendList, locality, market.feeBuy, market.feeSell, balP, resp), ctx.getServerHandler().player);
					break;
				}
				case 2: {
					Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction();
					if (market.vendList.get(message.index) != null) {
						resp = market.placeBid(message.index, ctx.getServerHandler().player.getUniqueID(), message.price, ctx.getServerHandler().player.getServer());
					}
					else resp = "This bid has closed";
					double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
					Main.NET.sendTo(new MessageMarketsToGui(true, 2, market.vendList, locality, market.feeBuy, market.feeSell, balP, resp), ctx.getServerHandler().player);
					break;
				}
				case 4: {
					Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getServer();
					if (market.vendList.get(message.index) != null) {
						resp = market.buyItem(message.index, ctx.getServerHandler().player.getUniqueID(), ctx.getServerHandler().player.getServer());
					}
					else resp = "Item No Longer Listed";
					double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
					Main.NET.sendTo(new MessageMarketsToGui(true, 4, market.vendList, locality, market.feeBuy, market.feeSell, balP, resp), ctx.getServerHandler().player);
					break;
				}
				default:
				}
				break;
			}
			case SALE_GUI_LAUNCH: {
				ctx.getServerHandler().player.openContainer.detectAndSendChanges();
				ctx.getServerHandler().player.closeContainer();
				ctx.getServerHandler().player.closeScreen();
				ctx.getServerHandler().player.openGui(Reference.MOD_ID, 0, ctx.getServerHandler().player.world, 0, 0, 0);
				break;
			}
			case SELL: {
				String response = "";
				switch (message.market) {
				case 0: {
					if (message.index.equals(Reference.NIL)) {MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal().addToList(message.giveItem, message.item, Reference.NIL, message.price, false); break;}
					ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(ctx.getServerHandler().player.chunkCoordX, ctx.getServerHandler().player.chunkCoordZ).getCapability(ChunkProvider.CHUNK_CAP, null);
					if (cap.getOwner().equals(Reference.NIL)) {response = "Local sales only permitted in guild territory"; break;}
					else {
						List<Guild> glist = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS;
						for (int i = 0; i < glist.size(); i++) {
							if (cap.getOwner().equals(glist.get(i).guildID)) {
								response = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal().sellItem(message.giveItem, message.item, ctx.getServerHandler().player.getUniqueID(), message.price, ctx.getServerHandler().player.getServer());
								break;
							}
						}
					}
					break;
				}
				case 1: {
					if (message.index.equals(Reference.NIL)) MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal().addToList(message.giveItem, message.item, Reference.NIL, message.price, false);
					else response = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal().sellItem(message.giveItem, message.item, ctx.getServerHandler().player.getUniqueID(), message.price, ctx.getServerHandler().player.getServer());
					break;
				}
				case 2: {
					if (message.index.equals(Reference.NIL)) MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction().addToList(message.giveItem, message.item, Reference.NIL, message.price, false);
					else response = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction().sellItem(message.giveItem, message.item, ctx.getServerHandler().player.getUniqueID(), message.price, ctx.getServerHandler().player.getServer());
					break;
				}
				case 3: {
					if (message.index.equals(Reference.NIL)) MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getServer().addToList(message.giveItem, message.item, Reference.NIL, message.price, true);
					else response = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getServer().sellItem(message.giveItem, message.item, ctx.getServerHandler().player.getUniqueID(), message.price, ctx.getServerHandler().player.getServer());
					break;
				}
				default:
				}
				ctx.getServerHandler().player.openContainer.detectAndSendChanges();				
				break;
			}
			case COLLECT: {
				boolean collected = false;
				List<ItemStack> queueItems = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal().itemQueue.getOrDefault(ctx.getServerHandler().player.getUniqueID(), new ArrayList<ItemStack>());
				for (int i = 0; i < queueItems.size(); i++) {
					if (queueItems.get(i).isItemEqual(message.item)) {
						ctx.getServerHandler().player.inventory.addItemStackToInventory(message.item.copy());
						queueItems.remove(i);
						MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
						collected = true;
						break;
					}
				}
				if (!collected) {
					queueItems = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal().itemQueue.getOrDefault(ctx.getServerHandler().player.getUniqueID(), new ArrayList<ItemStack>());
					for (int i = 0; i < queueItems.size(); i++) {
						if (queueItems.get(i).isItemEqual(message.item)) {
							ctx.getServerHandler().player.inventory.addItemStackToInventory(message.item.copy());
							queueItems.remove(i);
							MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
							collected = true;
							break;
						}
					}
					if (!collected) {
						queueItems = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction().itemQueue.getOrDefault(ctx.getServerHandler().player.getUniqueID(), new ArrayList<ItemStack>());
						for (int i = 0; i < queueItems.size(); i++) {
							if (queueItems.get(i).isItemEqual(message.item)) {
								ctx.getServerHandler().player.inventory.addItemStackToInventory(message.item.copy());
								queueItems.remove(i);
								MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
								break;
							}
						}
					}
				}
				UUID emptyID = UUID.randomUUID();
				personalList(message, ctx, emptyID);
				break;
			}
			case REMOVE: {
				MarketItem srcItem = null;
				Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal();
				boolean isRemoved = false;
				if (market.vendList.get(message.index) != null) {
					srcItem = market.vendList.get(message.index);
					if (srcItem.vendorGiveItem) {
						for (int s = 0; s < market.vendList.get(message.index).vendStock; s++) {
							if (!ctx.getServerHandler().player.inventory.addItemStackToInventory(srcItem.item.copy())) ctx.getServerHandler().player.dropItem(srcItem.item.copy(), false);
						}							
						market.vendList.remove(message.index);
						MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
						isRemoved = true;
					}
					else if (!srcItem.vendorGiveItem) {
						AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.addBalance(ctx.getServerHandler().player.getUniqueID(), market.vendList.get(message.index).price * market.vendList.get(message.index).vendStock);
						market.vendList.remove(message.index);
						AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
						MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
						isRemoved = true;
					}
				}
				if (!isRemoved) {
					market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal();
					if (market.vendList.get(message.index) != null) {
						srcItem = market.vendList.get(message.index);
						if (srcItem.vendorGiveItem) {
							for (int s = 0; s < market.vendList.get(message.index).vendStock; s++) {
								if (!ctx.getServerHandler().player.inventory.addItemStackToInventory(srcItem.item.copy())) ctx.getServerHandler().player.dropItem(srcItem.item.copy(), false);
							}							
							market.vendList.remove(message.index);
							MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
							isRemoved = true;
						}
						else if (!srcItem.vendorGiveItem) {
							AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.addBalance(ctx.getServerHandler().player.getUniqueID(), market.vendList.get(message.index).price * market.vendList.get(message.index).vendStock);
							market.vendList.remove(message.index);
							AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
							MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
							isRemoved = true;
						}
					}
					if (!isRemoved) {
						market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction();
						boolean auctionStarted = true;
						if (market.vendList.get(message.index) != null) {
							auctionStarted = playerExistsOnServer(market.vendList.get(message.index).highestBidder, ctx.getServerHandler().player.getEntityWorld());
							srcItem = market.vendList.get(message.index);
						}
						if (!isRemoved && !auctionStarted) {						
							if (!ctx.getServerHandler().player.inventory.addItemStackToInventory(srcItem.item.copy())) ctx.getServerHandler().player.dropItem(srcItem.item.copy(), false);
							market.vendList.remove(message.index);
							MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
						}
					}
				}
				UUID emptyID = UUID.randomUUID();
				personalList(message, ctx, emptyID);
				break;
			}
			case RESTOCK: {
				boolean isRestocked = false;
				Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal();
				if (market.vendList.get(message.index) != null) {
				isRestocked = market.increaseStock(message.index, ctx.getServerHandler().player.getServer(), ctx.getServerHandler().player) == "Restocked";
				}
				if (!isRestocked) {
					market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal();
					if (market.vendList.get(message.index) != null) {
						isRestocked = market.increaseStock(message.index, ctx.getServerHandler().player.getServer(), ctx.getServerHandler().player) == "Restocked";
					}
				}
				UUID emptyID = UUID.randomUUID();
				personalList(message, ctx, emptyID);
				break;
			}
			default:
			}
		}
		
		public void personalList(MessageMarketsToServer message, MessageContext ctx, UUID emptyID) {
			UUID locality = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(ctx.getServerHandler().player.chunkCoordX, ctx.getServerHandler().player.chunkCoordZ).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner();
			Map<UUID, MarketItem> personalList = new HashMap<UUID, MarketItem>();
			//get the player's listings from all markets
			Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal();
			for (Map.Entry<UUID, MarketItem> item : market.vendList.entrySet()) {
				if (item.getValue().vendor.equals(ctx.getServerHandler().player.getUniqueID())) {
					MarketItem refItem = new MarketItem(item.getValue().vendorGiveItem, item.getValue().item.copy(), item.getValue().vendor, item.getValue().price, item.getValue().vendStock, item.getValue().locality, emptyID, false, item.getValue().bidEnd);
					personalList.put(item.getKey(), refItem);
				}
			}
			market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal();
			for (Map.Entry<UUID, MarketItem> item : market.vendList.entrySet()) {
				if (item.getValue().vendor.equals(ctx.getServerHandler().player.getUniqueID())) {
					MarketItem refItem = new MarketItem(item.getValue().vendorGiveItem, item.getValue().item.copy(), item.getValue().vendor, item.getValue().price, item.getValue().vendStock, emptyID, emptyID, false, item.getValue().bidEnd);
					personalList.put(item.getKey(), refItem);
				}
			}
			market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction();
			for (Map.Entry<UUID, MarketItem> item : market.vendList.entrySet()) {
				if (item.getValue().vendor.equals(ctx.getServerHandler().player.getUniqueID())) {
					MarketItem refItem = new MarketItem(item.getValue().vendorGiveItem, item.getValue().item.copy(), item.getValue().vendor, item.getValue().price, item.getValue().vendStock, emptyID, item.getValue().highestBidder, false, item.getValue().bidEnd);
					personalList.put(item.getKey(), refItem);
				}
			}
			//Map<UUID, List<ItemStack>> itemQueue
			List<ItemStack> queueItems = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal().itemQueue.getOrDefault(ctx.getServerHandler().player.getUniqueID(), new ArrayList<ItemStack>());
			for (int i = 0; i < queueItems.size(); i++) {
				personalList.put(Marketplace.unrepeatedUUID(personalList), new MarketItem(true, queueItems.get(i), ctx.getServerHandler().player.getUniqueID(), -1, 1, Reference.NIL, Reference.NIL));
			}
			queueItems = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal().itemQueue.getOrDefault(ctx.getServerHandler().player.getUniqueID(), new ArrayList<ItemStack>());
			for (int i = 0; i < queueItems.size(); i++) {
				personalList.put(Marketplace.unrepeatedUUID(personalList),new MarketItem(true, queueItems.get(i), ctx.getServerHandler().player.getUniqueID(), -1, 1, Reference.NIL, Reference.NIL));
			}
			queueItems = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction().itemQueue.getOrDefault(ctx.getServerHandler().player.getUniqueID(), new ArrayList<ItemStack>());
			for (int i = 0; i < queueItems.size(); i++) {
				personalList.put(Marketplace.unrepeatedUUID(personalList),new MarketItem(true, queueItems.get(i), ctx.getServerHandler().player.getUniqueID(), -1, 1, Reference.NIL, Reference.NIL));
			}
			double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS.getBalance(ctx.getServerHandler().player.getUniqueID());
			Main.NET.sendTo(new MessageMarketsToGui(true, 3, personalList, locality, 0D, 0D, balP, emptyID.toString()), ctx.getServerHandler().player);
		}
		
		public boolean playerExistsOnServer(UUID player, World world) {
			List<Account> plist = AccountSaver.get(world).PLAYERS.accountList;
			for (int i = 1; i < plist.size(); i++) {
				if (plist.get(i).owner.equals(player)) return true;
			}
			return false;
		}
		
		private double distanceCalc(ChunkPos a, ChunkPos b) {
			double dist = Math.pow(a.x - b.x, 2) + Math.pow(a.z - b.z, 2);
			return Math.sqrt(dist);
		}
	}
	
	
}
