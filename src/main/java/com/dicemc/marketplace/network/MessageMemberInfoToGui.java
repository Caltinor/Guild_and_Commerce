package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.gui.GuiGuildMemberManager;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageMemberInfoToGui implements IMessage{
	public Guild guild;
	public Map<UUID, String> mbrNames;

	//this constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
	public MessageMemberInfoToGui() {}
	
	//unused until I need the UUID to pass for the check
	public MessageMemberInfoToGui(Guild guild, Map<UUID, String> mbrNames) {
		this.guild = guild;
		this.mbrNames = mbrNames;
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
		try {guild = new Guild(pbuf.readCompoundTag());
		mbrNames = mapFromNBT(pbuf.readCompoundTag());
		} catch (IOException e) {e.printStackTrace();}
	}
	 
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeCompoundTag(guild.toNBT());
		pbuf.writeCompoundTag(mapToNBT(mbrNames));
	}
	
	public static class PacketMemberInfoToGui implements IMessageHandler<MessageMemberInfoToGui, IMessage> {
		@Override
		public IMessage onMessage(MessageMemberInfoToGui message, MessageContext ctx) {
			Main.proxy.addScheduledTaskClient(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageMemberInfoToGui message, MessageContext ctx) {		
			GuiGuildMemberManager.syncMembers(message.guild, message.mbrNames);
		}
	}
}
