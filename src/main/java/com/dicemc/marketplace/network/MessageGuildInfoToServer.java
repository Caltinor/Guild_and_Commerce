package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageGuildInfoToServer implements IMessage{
	public static Guild guild;
	public static Map<String, Boolean> discriminator;

	//this constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
	public MessageGuildInfoToServer() {}
	
	public MessageGuildInfoToServer(Guild guild, Map<String, Boolean> discriminator) {
		this.guild = guild;
		this.discriminator = discriminator;
	}
	
	public NBTTagCompound discriminatorToNBT(Map<String, Boolean> discriminator) {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (Map.Entry<String, Boolean> entry : discriminator.entrySet()) {
			NBTTagCompound snbt = new NBTTagCompound();
			snbt.setString("key", entry.getKey());
			snbt.setBoolean("value", entry.getValue());
			list.appendTag(snbt);
		}
		compound.setTag("discriminators", list);
		return compound;
	}
	
	public Map<String, Boolean> discriminatorFromNBT(NBTTagCompound nbt) {
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		NBTTagList list = nbt.getTagList("discriminators", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			map.put(list.getCompoundTagAt(i).getString("key"), list.getCompoundTagAt(i).getBoolean("value"));
		}
		return map;
	}
	 
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		try {guild = new Guild(pbuf.readCompoundTag());
		discriminator = discriminatorFromNBT(pbuf.readCompoundTag());
		} catch (IOException e) {e.printStackTrace();}
	}
	 
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeCompoundTag(guild.toNBT());
		pbuf.writeCompoundTag(discriminatorToNBT(discriminator));
	}
	
	public static class PacketGuildInfoToServer implements IMessageHandler<MessageGuildInfoToServer, IMessage> {
		@Override
		public IMessage onMessage(MessageGuildInfoToServer message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageGuildInfoToServer message, MessageContext ctx) {
			int gindex = -1;
			List<Guild> glist = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS;
			for (int i = 0; i < glist.size(); i++) {
				if (glist.get(i).guildID.equals(guild.guildID)) gindex = i;
			}
			//discriminators used by GuiGuildManager 
			if (discriminator.get("name")) glist.get(gindex).guildName = guild.guildName;
			if (discriminator.get("open")) glist.get(gindex).openToJoin = guild.openToJoin;
			if (discriminator.get("tax")) glist.get(gindex).guildTax = guild.guildTax;
			if (discriminator.get("permnames")) glist.get(gindex).permLevels = guild.permLevels;
			if (discriminator.get("permvalues")) glist.get(gindex).permissions = guild.permissions;
			//discriminators used by GuiGuildMemberManager
			if (discriminator.get("members")) glist.get(gindex).members = guild.members;
			GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
		}
	}
}
