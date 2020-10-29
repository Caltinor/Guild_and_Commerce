package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCreateInfoToGui implements IMessage{
	public Map<UUID, String> list;
	public double balance, guildPrice;
	
	public MessageCreateInfoToGui() {}
	
	public MessageCreateInfoToGui(Map<UUID, String> list, double balance, double guildPrice) {
		this.list = list;
		this.balance = balance;
		this.guildPrice = guildPrice;
	}
	
	public NBTTagCompound mapToNBT(Map<UUID, String> map) {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (Map.Entry<UUID, String> entry : map.entrySet()) {
			NBTTagCompound snbt = new NBTTagCompound();
			snbt.setUniqueId("key", entry.getKey());
			snbt.setString("value", entry.getValue());
			list.appendTag(snbt);
		}
		compound.setTag("discriminators", list);
		return compound;
	}
	
	public Map<UUID, String> mapFromNBT(NBTTagCompound nbt) {
		Map<UUID, String> map = new HashMap<UUID, String>();
		NBTTagList list = nbt.getTagList("discriminators", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			map.put(list.getCompoundTagAt(i).getUniqueId("key"), list.getCompoundTagAt(i).getString("value"));
		}
		return map;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		try {list = mapFromNBT(pbuf.readCompoundTag());
		balance = pbuf.readDouble();
		guildPrice = pbuf.readDouble();} catch (IOException e) {}		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeCompoundTag(mapToNBT(list));
		pbuf.writeDouble(balance);
		pbuf.writeDouble(guildPrice);
	}
	public static class PacketCreateInfoToGui implements IMessageHandler<MessageCreateInfoToGui, IMessage> {
		@Override
		public IMessage onMessage(MessageCreateInfoToGui message, MessageContext ctx) {
			Main.proxy.addScheduledTaskClient(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageCreateInfoToGui message, MessageContext ctx) {		
			Main.proxy.openCreateGui(message.list, message.balance, message.guildPrice);
		}
	}
}
