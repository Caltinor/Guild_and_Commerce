package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.core.Marketplace;
import com.dicemc.marketplace.gui.GuiMarketManager;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageMarketsToGui implements IMessage{
	public Map<UUID, MarketItem> vendList;
	public UUID locality;
	public double feeBuy, feeSell, balP;
	public int listType;
	public String response;
	public boolean isUpdate;
	
	public MessageMarketsToGui() {}
	
	public MessageMarketsToGui(boolean isUpdate, int listType, Map<UUID, MarketItem> vendList, UUID locality, double feeBuy, double feeSell, double balP, String response) {
		this.isUpdate = isUpdate;
		this.vendList = vendList;
		this.locality = locality;
		this.feeBuy = feeBuy;
		this.feeSell = feeSell;
		this.balP = balP;
		this.listType = listType;
		this.response = response;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		try {vendList = readFromNBT(pbuf.readCompoundTag().getTagList("vendlist", Constants.NBT.TAG_COMPOUND));	} catch (IOException e) {}	
		locality = pbuf.readUniqueId();
		feeBuy = pbuf.readDouble();
		feeSell = pbuf.readDouble();
		balP = pbuf.readDouble();
		listType = pbuf.readInt();
		isUpdate = pbuf.readBoolean();
		response = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (Map.Entry<UUID, MarketItem> entry : vendList.entrySet()) {			
			list.appendTag(writeToNBT(new NBTTagCompound(), entry.getKey()));
		}
		nbt.setTag("vendlist", list);
		pbuf.writeCompoundTag(nbt);
		pbuf.writeUniqueId(locality);
		pbuf.writeDouble(feeBuy);
		pbuf.writeDouble(feeSell);
		pbuf.writeDouble(balP);
		pbuf.writeInt(listType);
		pbuf.writeBoolean(isUpdate);
		ByteBufUtils.writeUTF8String(buf, response);
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
	
	public static class PacketMarketsToGui implements IMessageHandler<MessageMarketsToGui, IMessage> {
		@Override
		public IMessage onMessage(MessageMarketsToGui message, MessageContext ctx) {
			Main.proxy.addScheduledTaskClient(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageMarketsToGui message, MessageContext ctx) {
			if (!message.isUpdate) Main.proxy.openMarketGui(message.listType, message.vendList, message.feeBuy, message.feeSell, message.balP, message.response, message.locality);
			else if (message.isUpdate) Main.proxy.updateMarketGui(message.listType, message.vendList, message.feeBuy, message.feeSell, message.balP, message.response);
		}
	}
}
