package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.gui.GuiAdmin;
import com.dicemc.marketplace.gui.GuiMarketManager;
import com.dicemc.marketplace.util.MktPktType;
import com.dicemc.marketplace.util.Reference;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAdminToGui implements IMessage{
	public int messageIndex = -1;
	public Map<UUID, MarketItem> vendList = new HashMap<UUID, MarketItem>();
	public Map<Account, String> accountList = new HashMap<Account, String> ();
	public Map<UUID, String> nameList = new HashMap<UUID, String>();
	public Map<String, Integer> guildPerms = new HashMap<String, Integer>();
	public MktPktType marketPacketType = MktPktType.NONE;
	public List<ChunkPos> posCore = new ArrayList<ChunkPos>();
	public List<ChunkPos> posOutpost = new ArrayList<ChunkPos>();
	public Map<ChunkPos, Double> chunkValues = new HashMap<ChunkPos, Double>();
	public Map<UUID, Integer> members = new HashMap<UUID, Integer>();
	public String str1 = ""; //uses: vendorName	guildname
	public String str2 = ""; //uses: locName	perm0
	public String str3 = ""; //uses: bidderName perm1
	public String str4 = ""; //uses:			perm2
	public String str5 = ""; //uses:			perm3
	public boolean bool1 = true;
	public boolean bool2 = true;
	public boolean bool3 = true;
	public double dbl1 = 0D;

	public MessageAdminToGui() {}
	
	public MessageAdminToGui(Map<Account, String> accountList) {
		messageIndex = 1;
		this.accountList = accountList;
	}
	
	public MessageAdminToGui(String name, boolean open, double tax, String perm0, String perm1, String perm2, String perm3, Map<String, Integer> guildPerms, boolean isAdmin) {
		messageIndex = 2;
		str1 = name;
		str2 = perm0;
		str3 = perm1;
		str4 = perm2;
		str5 = perm3;
		bool1 = open;
		bool2 = isAdmin;
		dbl1 = tax;
		this.guildPerms = guildPerms;
	}
	
	public MessageAdminToGui(Map<UUID, String> guildNames, boolean dummy) {
		messageIndex = 3;
		nameList = guildNames;
	}
	
	public MessageAdminToGui(Map<UUID, MarketItem> vendList, MktPktType type) {
		messageIndex = 4;
		this.vendList = vendList;
		this.marketPacketType = type;
	}
	
	public MessageAdminToGui(String vendorName, String locName, String bidderName) {
		messageIndex = 5;
		str1 = vendorName;
		str2 = locName;
		str3 = bidderName;
	}
	
	public MessageAdminToGui(List<ChunkPos> posCore, List<ChunkPos> posOutpost, Map<ChunkPos, Double> chunkValues) {
		messageIndex = 6;
		this.posCore = posCore;
		this.posOutpost = posOutpost;
		this.chunkValues = chunkValues;
	}
	
	public MessageAdminToGui(double price, boolean isPublic, boolean isForSale, boolean isOutpost) {
		messageIndex = 7;
		dbl1 = price;
		bool1 = isPublic;
		bool2 = isForSale;
		bool3 = isOutpost;
	}
	
	public MessageAdminToGui(Map<UUID, Integer> members, Map<UUID, String> nameList) {
		messageIndex = 8;
		this.members = members;
		this.nameList = nameList;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		messageIndex = pbuf.readInt();
		switch (messageIndex) {
		case 1: {			
			try {
			Map<Account, String> map = new HashMap<Account, String>();
			NBTTagCompound srcNBT = pbuf.readCompoundTag();
			NBTTagList list = srcNBT.getTagList("accountlist", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				Account acct = new Account(list.getCompoundTagAt(i).getUniqueId("UUID"), list.getCompoundTagAt(i).getDouble("balance"));
				map.put(acct, list.getCompoundTagAt(i).getString("name"));
			}
			accountList = map;	
			} catch (IOException e) {}
			break;
		}
		case 2: {
			str1 = ByteBufUtils.readUTF8String(buf);
			str2 = ByteBufUtils.readUTF8String(buf);
			str3 = ByteBufUtils.readUTF8String(buf);
			str4 = ByteBufUtils.readUTF8String(buf);
			str5 = ByteBufUtils.readUTF8String(buf);
			bool1 = pbuf.readBoolean();
			bool2 = pbuf.readBoolean();
			dbl1 = pbuf.readDouble();
			try {
			Map<String, Integer> map = new HashMap<String, Integer>();
			NBTTagCompound srcNBT = pbuf.readCompoundTag();
			NBTTagList list = srcNBT.getTagList("permlist", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				map.put(list.getCompoundTagAt(i).getString("name"), list.getCompoundTagAt(i).getInteger("value"));
			}
			guildPerms = map; } catch (IOException e) {}
			break;
		}
		case 3: {
			try {
				Map<UUID, String> map = new HashMap<UUID, String>();
				NBTTagCompound srcNBT = pbuf.readCompoundTag();
				NBTTagList list = srcNBT.getTagList("guildlist", Constants.NBT.TAG_COMPOUND);
				for (int i = 0; i < list.tagCount(); i++) {
					map.put(list.getCompoundTagAt(i).getUniqueId("UUID"), list.getCompoundTagAt(i).getString("name"));
				}
				nameList = map;	
				} catch (IOException e) {}
				break;
		}
		case 4: {
			try {vendList = readFromNBT(pbuf.readCompoundTag().getTagList("vendlist", Constants.NBT.TAG_COMPOUND));	} catch (IOException e) {}
			marketPacketType = MktPktType.values()[pbuf.readVarInt()];
			break;
		}
		case 5: {
			str1 = ByteBufUtils.readUTF8String(buf);
			str2 = ByteBufUtils.readUTF8String(buf);
			str3 = ByteBufUtils.readUTF8String(buf);
			break;
		}
		case 6: {
			try {NBTTagCompound nbt = pbuf.readCompoundTag();
			NBTTagList list = nbt.getTagList("core", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				posCore.add(new ChunkPos(list.getCompoundTagAt(i).getInteger("x"), list.getCompoundTagAt(i).getInteger("z")));
			}
			list = nbt.getTagList("outpost", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				posOutpost.add(new ChunkPos(list.getCompoundTagAt(i).getInteger("x"), list.getCompoundTagAt(i).getInteger("z")));
			}
			list = nbt.getTagList("chunkvalues", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				ChunkPos pos = new ChunkPos(list.getCompoundTagAt(i).getInteger("x"), list.getCompoundTagAt(i).getInteger("z"));
				chunkValues.put(pos, list.getCompoundTagAt(i).getDouble("value"));
			}
			} catch (IOException e) {}
			break;
		}
		case 7: {
			dbl1 = pbuf.readDouble();
			bool1 = pbuf.readBoolean();
			bool2 = pbuf.readBoolean();
			bool3 = pbuf.readBoolean();
			break;
		}
		case 8: {
			try {
			Map<UUID, Integer> map = new HashMap<UUID, Integer>();
			NBTTagCompound srcNBT = pbuf.readCompoundTag();
			NBTTagList list = srcNBT.getTagList("mbrlist", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				map.put(list.getCompoundTagAt(i).getUniqueId("UUID"), list.getCompoundTagAt(i).getInteger("rank"));
			}
			members = map;	
			Map<UUID, String> nameMap = new HashMap<UUID, String>();
			srcNBT = pbuf.readCompoundTag();
			list = srcNBT.getTagList("namelist", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); i++) {
				nameMap.put(list.getCompoundTagAt(i).getUniqueId("UUID"), list.getCompoundTagAt(i).getString("name"));
			}
			nameList = nameMap;
			} catch (IOException e) {}
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
		case 1: {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			for (Map.Entry<Account, String> entry : accountList.entrySet()) {	
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setUniqueId("UUID", entry.getKey().owner);
				snbt.setDouble("balance", entry.getKey().balance);
				snbt.setString("name", entry.getValue());
				list.appendTag(snbt);
			}
			nbt.setTag("accountlist", list);
			pbuf.writeCompoundTag(nbt);
			break;
		}
		case 2: {
			ByteBufUtils.writeUTF8String(buf, str1);
			ByteBufUtils.writeUTF8String(buf, str2);
			ByteBufUtils.writeUTF8String(buf, str3);
			ByteBufUtils.writeUTF8String(buf, str4);
			ByteBufUtils.writeUTF8String(buf, str5);
			pbuf.writeBoolean(bool1);
			pbuf.writeBoolean(bool2);
			pbuf.writeDouble(dbl1);
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			for (Map.Entry<String, Integer> entry : guildPerms.entrySet()) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setString("name", entry.getKey());
				snbt.setInteger("value", entry.getValue());
				list.appendTag(snbt);
			}
			nbt.setTag("permlist", list);
			pbuf.writeCompoundTag(nbt);
			break;
		}
		case 3: {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			for (Map.Entry<UUID, String> entry : nameList.entrySet()) {	
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setUniqueId("UUID", entry.getKey());
				snbt.setString("name", entry.getValue());
				list.appendTag(snbt);
			}
			nbt.setTag("guildlist", list);
			pbuf.writeCompoundTag(nbt);
			break;
		}
		case 4: {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			for (Map.Entry<UUID, MarketItem> entry : vendList.entrySet()) {			
				list.appendTag(writeToNBT(new NBTTagCompound(), entry.getKey()));
			}
			nbt.setTag("vendlist", list);
			pbuf.writeCompoundTag(nbt);
			pbuf.writeVarInt(marketPacketType.ordinal());
			break;
		}
		case 5: {
			ByteBufUtils.writeUTF8String(buf, str1);
			ByteBufUtils.writeUTF8String(buf, str2);
			ByteBufUtils.writeUTF8String(buf, str3);
			break;
		}
		case 6: {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			for (int i = 0; i < posCore.size(); i++) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setInteger("x", posCore.get(i).x);
				snbt.setInteger("z", posCore.get(i).z);
				list.appendTag(snbt);
			}
			nbt.setTag("core", list);
			list = new NBTTagList();
			for (int i = 0; i < posOutpost.size(); i++) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setInteger("x", posOutpost.get(i).x);
				snbt.setInteger("z", posOutpost.get(i).z);
				list.appendTag(snbt);
			}
			nbt.setTag("outpost", list);
			list = new NBTTagList();
			for (Map.Entry<ChunkPos, Double> entry : chunkValues.entrySet()) {
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setInteger("x", entry.getKey().x);
				snbt.setInteger("z", entry.getKey().z);
				snbt.setDouble("value", entry.getValue());
				list.appendTag(snbt);
			}
			nbt.setTag("chunkvalues", list);
			pbuf.writeCompoundTag(nbt);
			break;
		}
		case 7: {
			pbuf.writeDouble(dbl1);
			pbuf.writeBoolean(bool1);
			pbuf.writeBoolean(bool2);
			pbuf.writeBoolean(bool3);
			break;
		}
		case 8: {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			for (Map.Entry<UUID, Integer> entry : members.entrySet()) {	
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setUniqueId("UUID", entry.getKey());
				snbt.setInteger("rank", entry.getValue());
				list.appendTag(snbt);
			}
			nbt.setTag("mbrlist", list);
			pbuf.writeCompoundTag(nbt);
			nbt = new NBTTagCompound();
			list = new NBTTagList();
			for (Map.Entry<UUID, String> entry : nameList.entrySet()) {	
				NBTTagCompound snbt = new NBTTagCompound();
				snbt.setUniqueId("UUID", entry.getKey());
				snbt.setString("name", entry.getValue());
				list.appendTag(snbt);
			}
			nbt.setTag("namelist", list);
			pbuf.writeCompoundTag(nbt);
			break;
		}
		default:
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
	
	public Map<UUID, MarketItem> readFromNBT(NBTTagList nbt) {
		Map<UUID, MarketItem> list = new HashMap<UUID, MarketItem>();
		for (int i = 0; i < nbt.tagCount(); i++) {
			ItemStack item = new ItemStack(nbt.getCompoundTagAt(i).getCompoundTag("item"));
			list.put(nbt.getCompoundTagAt(i).getUniqueId("index"), new MarketItem(nbt.getCompoundTagAt(i).getBoolean("giveitem"), item, nbt.getCompoundTagAt(i).getUniqueId("vendor"), nbt.getCompoundTagAt(i).getDouble("price"),nbt.getCompoundTagAt(i).getInteger("stock"), nbt.getCompoundTagAt(i).getUniqueId("locality"), nbt.getCompoundTagAt(i).getUniqueId("bidder"), nbt.getCompoundTagAt(i).getBoolean("infinite"), nbt.getCompoundTagAt(i).getLong("bidend")));
		}
		return list;
	}

	public static class PacketAdminToGui implements IMessageHandler<MessageAdminToGui, IMessage> {
		@Override
		public IMessage onMessage(MessageAdminToGui message, MessageContext ctx) {
			Main.proxy.addScheduledTaskClient(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageAdminToGui message, MessageContext ctx) {
			switch (message.messageIndex) {
			case 1: {
				Main.proxy.updateAdminSyncAccounts(message.accountList);
				break;
			}
			case 2: {
				Main.proxy.updateAdminSyncGuildData(message.str1, message.bool1, message.dbl1, message.str2, message.str3, message.str4, message.str5, message.guildPerms, message.bool2);
				break;
			}
			case 3: {
				Main.proxy.updateAdminSyncGuildList(message.nameList);
				break;
			}
			case 4: {
				switch (message.marketPacketType) {
				case LOCAL: {
					Main.proxy.updateAdminSyncMarkets(GuiMarketManager.sortedMarketList(0, message.vendList, Reference.NIL, 0, true));
					break;
				}
				case GLOBAL: {
					Main.proxy.updateAdminSyncMarkets(GuiMarketManager.sortedMarketList(1, message.vendList, Reference.NIL, 0, true));
					break;
				}
				case AUCTION: {
					Main.proxy.updateAdminSyncMarkets(GuiMarketManager.sortedMarketList(2, message.vendList, Reference.NIL, 0, true));
					break;
				}
				case SERVER: {
					Main.proxy.updateAdminSyncMarkets(GuiMarketManager.sortedMarketList(4, message.vendList, Reference.NIL, 0, true));
					break;
				}
				default:
				}
			}
			case 5: {
				Main.proxy.updateAdminSyncMarketDetail(message.str1, message.str2, message.str3);
				break;
			}
			case 6: {
				Main.proxy.updateAdminSyncGuildLand(message.posCore, message.posOutpost, message.chunkValues);
				break;
			}
			case 7: {
				Main.proxy.updateAdminSyncGuildLandDetail(message.dbl1, message.bool1, message.bool2, message.bool3);
				break;
			}
			case 8: {
				Main.proxy.updateAdminSyncGuildMembers(message.members, message.nameList);
			}
			default:
			}
		}
	}
}
