package com.dicemc.marketplace.network;

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
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAdminToServer implements IMessage {
	public int messageIndex = -1;
	public MktPktType marketPacketType = MktPktType.NONE;
	public UUID id = Reference.NIL;
	double amount = 0D;
	boolean bool1 = true;
	int vendStock = 0;
	boolean infinite = true;
	long bidEnd = 0;

	public MessageAdminToServer() {}
	
	public MessageAdminToServer(boolean isGuildAccount, UUID owner, double balance) { //acount changes
		messageIndex = 0;
		bool1 = isGuildAccount;
		id = owner;
		amount = balance;
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
		messageIndex = 4;
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
		this.amount = price;
		this.bool1 = vendorGiveItem;
		this.vendStock = vendStock;
		this.infinite = infinite;
		this.bidEnd = bidEnd;
	}
	
	public MessageAdminToServer(MktPktType type, UUID marketItemID, boolean isExpire) {//removes item from the specified market
		messageIndex = isExpire ? 8 : 7;
		marketPacketType = type;
		id = marketItemID;
	}
	
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		messageIndex = pbuf.readInt();
		switch (messageIndex) {
		case 0: {
			bool1 = pbuf.readBoolean();
			id = pbuf.readUniqueId();
			amount = pbuf.readDouble();
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
			amount = pbuf.readDouble();
			bool1 = pbuf.readBoolean();
			vendStock = pbuf.readInt();
			infinite = pbuf.readBoolean();
			bidEnd = pbuf.readLong();
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
			pbuf.writeDouble(amount);
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
			pbuf.writeDouble(amount);
			pbuf.writeBoolean(bool1);
			pbuf.writeInt(vendStock);
			pbuf.writeBoolean(infinite);
			pbuf.writeLong(bidEnd);
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
				AcctGroup.setBalance(message.id, message.amount);
				AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
				MinecraftServer server = ctx.getServerHandler().player.getServer();
				Map<Account, String> map = new HashMap<Account, String>();
				for (int i = 0; i < AcctGroup.accountList.size(); i++) {
					String str = message.bool1 ? (AcctGroup.accountList.get(i).owner.equals(Reference.NIL) ? "Server Account":GuildSaver.get(server.getEntityWorld()).guildNamefromUUID(AcctGroup.accountList.get(i).owner)) : playerNameFromUUID(AcctGroup.accountList.get(i).owner, server);
					map.put(AcctGroup.accountList.get(i), str);
				}
				Main.NET.sendTo(new MessageAdminToGui(map), ctx.getServerHandler().player);
				break;
			}
			case 1: { //account list request
				AccountGroup AcctGroup = message.bool1 ? AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS : AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS;
				MinecraftServer server = ctx.getServerHandler().player.getServer();
				Map<Account, String> map = new HashMap<Account, String>();
				for (int i = 0; i < AcctGroup.accountList.size(); i++) {
					String str = message.bool1 ? (AcctGroup.accountList.get(i).owner.equals(Reference.NIL) ? "Server Account":GuildSaver.get(server.getEntityWorld()).guildNamefromUUID(AcctGroup.accountList.get(i).owner)) : playerNameFromUUID(AcctGroup.accountList.get(i).owner, server);
					map.put(AcctGroup.accountList.get(i), str);
				}
				Main.NET.sendTo(new MessageAdminToGui(map), ctx.getServerHandler().player);
				break;
			}
			case 2: { //account remove
				AccountGroup AcctGroup = message.bool1 ? AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS : AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).PLAYERS;
				AcctGroup.removeAccount(message.id);
				AccountSaver.get(ctx.getServerHandler().player.getEntityWorld());
				MinecraftServer server = ctx.getServerHandler().player.getServer();
				Map<Account, String> map = new HashMap<Account, String>();
				for (int i = 0; i < AcctGroup.accountList.size(); i++) {
					String str = message.bool1 ? (AcctGroup.accountList.get(i).owner.equals(Reference.NIL) ? "Server Account":GuildSaver.get(server.getEntityWorld()).guildNamefromUUID(AcctGroup.accountList.get(i).owner)) : playerNameFromUUID(AcctGroup.accountList.get(i).owner, server);
					map.put(AcctGroup.accountList.get(i), str);
				}
				Main.NET.sendTo(new MessageAdminToGui(map), ctx.getServerHandler().player);
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
				Marketplace market = null;
				switch (message.marketPacketType) {				
				case LOCAL: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal(); break; }
				case GLOBAL: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal(); break;}
				case AUCTION: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction(); break;}
				case SERVER: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getServer(); break;}
				default:
				}
				Main.NET.sendTo(new MessageAdminToGui(market.vendList, message.marketPacketType), ctx.getServerHandler().player);
				break;
			}
			case 5: { //market item details
				Marketplace market = null;
				MinecraftServer server = ctx.getServerHandler().player.getServer();
				switch (message.marketPacketType) {				
				case LOCAL: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal(); break; }
				case GLOBAL: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal(); break;}
				case AUCTION: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction(); break;}
				case SERVER: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getServer(); break;}
				default:
				}
				String str1 = market.vendList.get(message.id).vendor.equals(Reference.NIL) ? "Server" : server.getPlayerProfileCache().getProfileByUUID(market.vendList.get(message.id).vendor).getName();
				String str2 = market.vendList.get(message.id).locality.equals(Reference.NIL)? "" : GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).guildNamefromUUID(market.vendList.get(message.id).locality);
				String str3 = market.vendList.get(message.id).highestBidder.equals(Reference.NIL) ? "" : server.getPlayerProfileCache().getProfileByUUID(market.vendList.get(message.id).highestBidder).getName();
				Main.NET.sendTo(new MessageAdminToGui(str1, str2, str3), ctx.getServerHandler().player);
				break;
			}
			case 6: {
				Marketplace market = null;
				switch (message.marketPacketType) {				
				case LOCAL: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal(); break; }
				case GLOBAL: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal(); break;}
				case AUCTION: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction(); break;}
				case SERVER: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getServer(); break;}
				default:
				}
				MarketItem item = market.vendList.get(message.id);
				item.price = message.amount;
				item.vendorGiveItem = message.bool1;
				item.vendStock = message.vendStock;
				item.infinite = message.infinite;
				item.bidEnd = message.bidEnd;
				MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
				Main.NET.sendTo(new MessageAdminToGui(market.vendList, message.marketPacketType), ctx.getServerHandler().player);
				break;
			}
			case 7: {
				Marketplace market = null;
				switch (message.marketPacketType) {				
				case LOCAL: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getLocal(); break; }
				case GLOBAL: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGlobal(); break;}
				case AUCTION: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getAuction(); break;}
				case SERVER: {market = MarketSaver.get(ctx.getServerHandler().player.getEntityWorld()).getServer(); break;}
				default:
				}
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
			default:
			}
		}
	
		private String playerNameFromUUID(UUID pid, MinecraftServer server) {
			String str = "";
			if (pid.equals(Reference.NIL)) return "Server Account";
			str = server.getPlayerProfileCache().getProfileByUUID(pid) == null ? "" : server.getPlayerProfileCache().getProfileByUUID(pid).getName();
			return str;
		}
	}

}
