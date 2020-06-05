package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.gui.GuiGuildMemberManager;
import com.dicemc.marketplace.util.commands.Commands;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageMemberInfoToServer implements IMessage{
	public String name;
	public UUID guildID;
	public boolean isAdmin;

	//this constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
	public MessageMemberInfoToServer() {}
	
	//unused until I need the UUID to pass for the check
	public MessageMemberInfoToServer(String name, UUID guildID, boolean isAdmin) {
		this.name = name;
		this.guildID = guildID;
		this.isAdmin = isAdmin;
	}
	 
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		name = ByteBufUtils.readUTF8String(buf);
		guildID = pbuf.readUniqueId();
		isAdmin = pbuf.readBoolean();
	}
	
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		ByteBufUtils.writeUTF8String(buf, name);
		pbuf.writeUniqueId(guildID);
		pbuf.writeBoolean(isAdmin);
	}
	
	public static class PacketMemberInfoToServer implements IMessageHandler<MessageMemberInfoToServer, IMessage> {
		@Override
		public IMessage onMessage(MessageMemberInfoToServer message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageMemberInfoToServer message, MessageContext ctx) {
			int gindex = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).guildIndexFromUUID(message.guildID);
			List<Guild> glist = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS;
			if (gindex >= 0) {
				glist.get(gindex).addMember(Commands.playerUUIDfromString(ctx.getServerHandler().player.getServer(), message.name), -1);
				Map<UUID, String> mbrNames = new HashMap<UUID, String>();
				for (UUID u : glist.get(gindex).members.keySet()) {
					mbrNames.put(u, ctx.getServerHandler().player.getServer().getPlayerProfileCache().getProfileByUUID(u).getName());
				}
				if (!message.isAdmin) Main.NET.sendTo(new MessageMemberInfoToGui(glist.get(gindex), mbrNames), ctx.getServerHandler().player);
				if (message.isAdmin) Main.NET.sendTo(new MessageAdminToGui(glist.get(gindex).members, mbrNames), ctx.getServerHandler().player);
			}
			
		}
	}
}
