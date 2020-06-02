package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.HashMap;
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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAdminToGui implements IMessage{
	public int messageIndex = -1;
	public Map<UUID, MarketItem> vendList = new HashMap<UUID, MarketItem>();
	public Map<Account, String> accountList = new HashMap<Account, String> ();
	public Map<UUID, String> guildList = new HashMap<UUID, String>();
	public MktPktType marketPacketType = MktPktType.NONE;
	public String str1 = ""; //uses: vendorName
	public String str2 = ""; //uses: locName
	public String str3 = ""; //uses: bidderName

	public MessageAdminToGui() {}
	
	public MessageAdminToGui(Map<Account, String> accountList) {
		messageIndex = 1;
		this.accountList = accountList;
	}
	
	public MessageAdminToGui(Map<UUID, String> guildNames, boolean dummy) {
		messageIndex = 3;
		guildList = guildNames;
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
		case 3: {
			try {
				Map<UUID, String> map = new HashMap<UUID, String>();
				NBTTagCompound srcNBT = pbuf.readCompoundTag();
				NBTTagList list = srcNBT.getTagList("guildlist", Constants.NBT.TAG_COMPOUND);
				for (int i = 0; i < list.tagCount(); i++) {
					map.put(list.getCompoundTagAt(i).getUniqueId("UUID"), list.getCompoundTagAt(i).getString("name"));
				}
				guildList = map;	
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
		case 3: {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			for (Map.Entry<UUID, String> entry : guildList.entrySet()) {	
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
				GuiAdmin.syncAccounts(message.accountList);
				break;
			}
			case 3: {
				GuiAdmin.syncGuildList(message.guildList);
			}
			case 4: {
				switch (message.marketPacketType) {
				case LOCAL: {
					GuiAdmin.syncMarkets(GuiMarketManager.sortedMarketList(0, message.vendList, Reference.NIL));
					break;
				}
				case GLOBAL: {
					GuiAdmin.syncMarkets(GuiMarketManager.sortedMarketList(1, message.vendList, Reference.NIL));
					break;
				}
				case AUCTION: {
					GuiAdmin.syncMarkets(GuiMarketManager.sortedMarketList(2, message.vendList, Reference.NIL));
					break;
				}
				case SERVER: {
					GuiAdmin.syncMarkets(GuiMarketManager.sortedMarketList(4, message.vendList, Reference.NIL));
					break;
				}
				default:
				}
			}
			case 5: {
				GuiAdmin.syncMarketDetail(message.str1, message.str2, message.str3);
				break;
			}
			default:
			}
		}
	}
}
