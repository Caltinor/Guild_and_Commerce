package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.AccountGroup;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.core.Marketplace;
import com.dicemc.marketplace.util.MktPktType;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;
import com.dicemc.marketplace.util.datasaver.MarketSaver;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAdminToServer implements IMessage {
	public int messageIndex = -1;
	public MktPktType marketPacketType = MktPktType.NONE;
	public UUID id = Reference.NIL;
	public int i1 = 0;
	public int i2 = 0;
	double dbl1 = 0D;
	boolean bool1 = true;
	boolean bool2 = true;
	boolean bool3 = true;
	int vendStock = 0;	
	long bidEnd = 0;
	String str1 = "";
	String str2 = "";
	String str3 = "";
	String str4 = "";
	String str5 = "";
	Map<String, Integer> map1 = new HashMap<String, Integer>();

	public MessageAdminToServer() {}
	
	public MessageAdminToServer(boolean isGuildAccount, UUID owner, double balance) { //acount changes
		messageIndex = 0;
		bool1 = isGuildAccount;
		id = owner;
		dbl1 = balance;
	}
	
	public MessageAdminToServer(boolean isGuildList) { //account load
		messageIndex = 1;
		bool1 = isGuildList;
	}
	
	public MessageAdminToServer(boolean isGuildList, UUID account) { //account remove
		messageIndex = 2;
		bool1 = isGuildList;
		id = account;
	}
	
	public MessageAdminToServer(int makeMeZero) { //guild list request	
		messageIndex = 3;
	}
	
	public MessageAdminToServer(MktPktType type) { //constructor for market list requests
		messageIndex = type == MktPktType.SALE_GUI_LAUNCH ? 9 : 4;
		marketPacketType = type;
	}
	
	public MessageAdminToServer(MktPktType type, UUID marketItemID) {//grabs information for the selected item.
		messageIndex = 5;
		marketPacketType = type;
		id = marketItemID;
	}
	
	public MessageAdminToServer(MktPktType type, UUID marketItemID, double price, boolean vendorGiveItem, int vendStock, boolean infinite, long bidEnd) {
		messageIndex = 6;
		marketPacketType = type;
		id = marketItemID;
		this.dbl1 = price;
		this.bool1 = vendorGiveItem;
		this.vendStock = vendStock;
		this.bool2 = infinite;
		this.bidEnd = bidEnd;
	}
	
	public MessageAdminToServer(MktPktType type, UUID marketItemID, boolean isExpire) {//removes item from the specified market
		messageIndex = isExpire ? 8 : 7;
		marketPacketType = type;
		id = marketItemID;
	}
	
	public MessageAdminToServer(UUID guildID, String name, boolean open, double tax, String perm0, String perm1, String perm2, String perm3, Map<String, Integer> guildPerms) {
		messageIndex = 11;
		id = guildID;
		bool1 = open;
		dbl1 = tax;
		str1 = name;
		str2 = perm0;
		str3 = perm1;
		str4 = perm2;
		str5 = perm3;
		map1 = guildPerms;
	}
	
	public MessageAdminToServer(UUID guildID, int subMenu) {
		switch (subMenu) {
		case 0: {messageIndex = 10;	break;} //request for guild main info
		case 1: {messageIndex = 12;	break;} //request for guild land info
		case 2: {messageIndex = 13;	break;} //request for guild member info
		default:}
		id = guildID;
	}
	
	public MessageAdminToServer(int chunkX, int chunkZ) {
		messageIndex = 14;
		i1 = chunkX;
		i2 = chunkZ;
	}

	public MessageAdminToServer(int chunkX, int chunkZ, double value, boolean isPublic, boolean isForSale, boolean isOutpost) {
		messageIndex = 15;
		i1 = chunkX;
		i2 = chunkZ;
		dbl1 = value;
		bool1 = isPublic;
		bool2 = isForSale;
		bool3 = isOutpost;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		messageIndex = pbuf.readInt();
		switch (messageIndex) {
		case 0: {
			bool1 = pbuf.readBoolean();
			id = pbuf.readUniqueId();
			dbl1 = pbuf.readDouble();
			break;
		}
		case 1: {
			bool1 = pbuf.readBoolean();
			break;
		}
		case 2: {
			bool1 = pbuf.readBoolean();
			id = pbuf.readUniqueId();
			break;
		}
		case 4: {
			marketPacketType = MktPktType.values()[pbuf.readVarInt()];
			break;
		}
		case 5: case 7: case 8:{
			marketPacketType = MktPktType.values()[pbuf.readVarInt()];
			id = pbuf.readUniqueId();
			break;
		}
		case 6: {
			marketPacketType = MktPktType.values()[pbuf.readVarInt()];
			id = pbuf.readUniqueId();
			dbl1 = pbuf.readDouble();
			bool1 = pbuf.readBoolean();
			vendStock = pbuf.readInt();
			bool2 = pbuf.readBoolean();
			bidEnd = pbuf.readLong();
			break;
		}
		case 10: case 12: case 13:{
			id = pbuf.readUniqueId();
			break;
		}
		case 11: {
			id = pbuf.readUniqueId();
			bool1 = pbuf.readBoolean();
			dbl1 = pbuf.readDouble();
			str1 = ByteBufUtils.readUTF8String(buf);
			str2 = ByteBufUtils.readUTF8String(buf);
			str3 = ByteBufUtils.readUTF8String(buf);
			str4 = ByteBufUtils.readUTF8String(buf);
			str5 = ByteBufUtils.readUTF8String(buf);
			Map<String, Integer> map = new HashMap<String, Integer>();
			try {
			NBTTagCompound srcNBT = pbuf.readCompoundTag();
			NBTTagList list = srcNBT.getTagList("permlist", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				map.put(list.getCompoundTagAt(i).getString("key"), list.getCompoundTagAt(i).getInteger("value"));
			}} catch (IOException e) {}
			map1 = map;
			break;
		}
		case 14: {
			i1 = pbuf.readInt();
			i2 = pbuf.readInt();
			break;
		}
		case 15: {
			i1 = pbuf.readInt();
			i2 = pbuf.readInt();
			dbl1 = pbuf.readDouble();
			bool1 = pbuf.readBoolean();
			bool2 = pbuf.readBoolean();
			bool3 = pbuf.readBoolean();
			break;
		}
		default:
		}		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeInt(messageIndex);
		switch (messageIndex) {
		case 0: {
			pbuf.writeBoolean(bool1);
			pbuf.writeUniqueId(id);
			pbuf.writeDouble(dbl1);
			break;
		}
		case 1: {
			pbuf.writeBoolean(bool1);
			break;
		}
		case 2: {
			pbuf.writeBoolean(bool1);
			pbuf.writeUniqueId(id);
			break;
		}
		case 4: {
			pbuf.writeVarInt(marketPacketType.ordinal());
			break;
		}
		case 5: case 7: case 8:{
			pbuf.writeVarInt(marketPacketType.ordinal());
			pbuf.writeUniqueId(id);
			break;
		}
		case 6: {
			pbuf.writeVarInt(marketPacketType.ordinal());
			pbuf.writeUniqueId(id);
			pbuf.writeDouble(dbl1);
			pbuf.writeBoolean(bool1);
			pbuf.writeInt(vendStock);
			pbuf.writeBoolean(bool2);
			pbuf.writeLong(bidEnd);
			break;
		}
		case 10: case 12: case 13:{
			pbuf.writeUniqueId(id);
			break;
		}
		case 11: {
			pbuf.writeUniqueId(id);
			pbuf.writeBoolean(bool1);
			pbuf.writeDouble(dbl1);
			ByteBufUtils.writeUTF8String(buf, str1);
			ByteBufUtils.writeUTF8String(buf, str2);
			ByteBufUtils.writeUTF8String(buf, str3);
			ByteBufUtils.writeUTF8String(buf, str4);
			ByteBufUtils.writeUTF8String(buf, str5);
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			for (Map.Entry<String, Integer> entry : map1.entrySet()) {	
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setString("key", entry.getKey());
				snbt.setInteger("value", entry.getValue());
				list.appendTag(snbt);
			}
			nbt.setTag("permlist", list);
			pbuf.writeCompoundTag(nbt);
			break;
		}
		case 14: {
			pbuf.writeInt(i1);
			pbuf.writeInt(i2);
			break;
		}
		case 15: {
			pbuf.writeInt(i1);
			pbuf.writeInt(i2);
			pbuf.writeDouble(dbl1);
			pbuf.writeBoolean(bool1);
			pbuf.writeBoolean(bool2);
			pbuf.writeBoolean(bool3);
			break;
		}
		default:
		}		
	}
	
	public static class PacketAdminToServer implements IMessageHandler<MessageAdminToServer, IMessage> {
		@Override
		public IMessage onMessage(MessageAdminToServer message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageAdminToServer message, MessageContext ctx) {
			switch (message.messageIndex) {
			case 0: { //acct change
				AccountGroup AcctGroup = message.bool1 ? AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS : AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS;
				AcctGroup.setBalance(message.id, message.dbl1);
				AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
				sendAccountListToGui(message, ctx, AcctGroup);
				break;
			}
			case 1: { //account list request
				AccountGroup AcctGroup = message.bool1 ? AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS : AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS;
				sendAccountListToGui(message, ctx, AcctGroup);
				break;
			}
			case 2: { //account remove
				AccountGroup AcctGroup = message.bool1 ? AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS : AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS;
				AcctGroup.removeAccount(message.id);
				AccountSaver.get(ctx.getServerHandler().player.getEntityWorld());
				sendAccountListToGui(message, ctx, AcctGroup);
				break;
			}
			case 3: { //guild list request
				Map<UUID, String> map = new HashMap<UUID, String>();
				List<Guild> glist = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS;
				for (int i = 0; i < glist.size(); i++) {
					map.put(glist.get(i).guildID, glist.get(i).guildName);
				}
				Main.NET.sendTo(new MessageAdminToGui(map, true), ctx.getServerHandler().player);
				break;
			}
			case 4: { //market
				Marketplace market = marketFromType(message.marketPacketType, ctx);				
				Main.NET.sendTo(new MessageAdminToGui(market.vendList, message.marketPacketType), ctx.getServerHandler().player);
				break;
			}
			case 5: { //market item details
				Marketplace market = marketFromType(message.marketPacketType, ctx);
				MinecraftServer server = ctx.getServerHandler().player.getServer();
				String str1 = market.vendList.get(message.id).vendor.equals(Reference.NIL) ? "Server" : server.getPlayerProfileCache().getProfileByUUID(market.vendList.get(message.id).vendor).getName();
				String str2 = market.vendList.get(message.id).locality.equals(Reference.NIL)? "" : GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).guildNamefromUUID(market.vendList.get(message.id).locality);
				String str3 = market.vendList.get(message.id).highestBidder.equals(Reference.NIL) ? "" : server.getPlayerProfileCache().getProfileByUUID(market.vendList.get(message.id).highestBidder).getName();
				Main.NET.sendTo(new MessageAdminToGui(str1, str2, str3), ctx.getServerHandler().player);
				break;
			}
			case 6: {
				Marketplace market = marketFromType(message.marketPacketType, ctx);
				MarketItem item = market.vendList.get(message.id);
				item.price = message.dbl1;
				item.vendorGiveItem = message.bool1;
				item.vendStock = message.vendStock;
				item.infinite = message.bool2;
				item.bidEnd = message.bidEnd;
				MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
				Main.NET.sendTo(new MessageAdminToGui(market.vendList, message.marketPacketType), ctx.getServerHandler().player);
				break;
			}
			case 7: {
				Marketplace market = marketFromType(message.marketPacketType, ctx);
				market.vendList.remove(message.id);
				MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
				Main.NET.sendTo(new MessageAdminToGui(market.vendList, message.marketPacketType), ctx.getServerHandler().player);
				break;
			}
			case 8: {
				Marketplace market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction();
				market.vendList.get(message.id).bidEnd = System.currentTimeMillis();
				MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
				Main.NET.sendTo(new MessageAdminToGui(market.vendList, message.marketPacketType), ctx.getServerHandler().player);
				break;
			}
			case 9: {
				ctx.getServerHandler().player.openGui(Reference.MOD_ID, 0, ctx.getServerHandler().player.world, 1, 0, 0);
				break;
			}
			case 10: { //gathers selected guild information and sends it to the gui
				int gid = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).guildIndexFromUUID(message.id);
				Guild guild = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS.get(gid);
				Main.NET.sendTo(new MessageAdminToGui(guild.guildName, guild.openToJoin, guild.guildTax, guild.permLevels.getOrDefault(0, "0-loadFail"),
						guild.permLevels.getOrDefault(1, "1-loadFail"),guild.permLevels.getOrDefault(2, "2-loadFail"),guild.permLevels.getOrDefault(3, "3-loadFail"), guild.permissions), ctx.getServerHandler().player);
				break;
			}
			case 11: { //saves guild information from guild main to the actual guild object
				int gid = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).guildIndexFromUUID(message.id);
				Guild guild = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS.get(gid);
				guild.guildName = message.str1;
				guild.guildTax = message.dbl1;
				guild.openToJoin = message.bool1;
				guild.permLevels.put(0, message.str2);
				guild.permLevels.put(1, message.str3);
				guild.permLevels.put(2, message.str4);
				guild.permLevels.put(3, message.str5);
				guild.permissions = message.map1;
				GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
				break;
			}
			case 12: {		
				Map<ChunkPos, Double> chunkValues = new HashMap<ChunkPos, Double>();
				int gindex = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).guildIndexFromUUID(message.id);
				Guild guild = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS.get(gindex);
				List<ChunkPos> posCore = guild.coreLand;
				List<ChunkPos> posOutpost = guild.outpostLand; 
				for (ChunkPos c : posCore) {
					ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(c.x, c.z).getCapability(ChunkProvider.CHUNK_CAP, null);
					double price = cap.getForSale() ? -1* cap.getPrice() : cap.getPrice();
					chunkValues.put(c, price);
				}
				for (ChunkPos c : posOutpost) {
					ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(c.x, c.z).getCapability(ChunkProvider.CHUNK_CAP, null);
					double price = cap.getForSale() ? -1* cap.getPrice() : cap.getPrice();
					chunkValues.put(c, price);
				}
				Main.NET.sendTo(new MessageAdminToGui(posCore, posOutpost, chunkValues), ctx.getServerHandler().player);
				break;
			}
			case 13: {
				
				break;
			}
			case 14: {
				ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.i1, message.i2).getCapability(ChunkProvider.CHUNK_CAP, null);
				Main.NET.sendTo(new MessageAdminToGui(cap.getPrice(), cap.getPublic(), cap.getForSale(), cap.getOutpost()), ctx.getServerHandler().player);
				break;
			}
			case 15: {
				ChunkCapability cap = ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.i1, message.i2).getCapability(ChunkProvider.CHUNK_CAP, null);
				cap.setPrice(message.dbl1);
				cap.setPublic(message.bool1);
				cap.setForSale(message.bool2);
				cap.setOutpost(message.bool3);
				ctx.getServerHandler().player.getEntityWorld().getChunkFromChunkCoords(message.i1, message.i2).markDirty();
			}
			default:
			}
		}
	
		private String playerNameFromUUID(UUID pid, MinecraftServer server) {
			String str = "";
			if (pid.equals(Reference.NIL)) return "Server Account";
			str = server.getPlayerProfileCache().getProfileByUUID(pid) == null ? "" : server.getPlayerProfileCache().getProfileByUUID(pid).getName();
			return str;
		}
		
		private void sendAccountListToGui(MessageAdminToServer message, MessageContext ctx, AccountGroup AcctGroup) {
			MinecraftServer server = ctx.getServerHandler().player.getServer();
			Map<Account, String> map = new HashMap<Account, String>();
			for (int i = 0; i < AcctGroup.accountList.size(); i++) {
				String str = message.bool1 ? (AcctGroup.accountList.get(i).owner.equals(Reference.NIL) ? "Server Account":GuildSaver.get(server.getEntityWorld()).guildNamefromUUID(AcctGroup.accountList.get(i).owner)) : playerNameFromUUID(AcctGroup.accountList.get(i).owner, server);
				map.put(AcctGroup.accountList.get(i), str);
			}
			Main.NET.sendTo(new MessageAdminToGui(map), ctx.getServerHandler().player);
		}
		
		private Marketplace marketFromType(MktPktType type, MessageContext ctx) {
			switch (type) {				
			case LOCAL: {return MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal();}
			case GLOBAL: {return MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal();}
			case AUCTION: {return MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction();}
			case SERVER: {return MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getServer();}
			default: return null;
			}
		}
	}

}
