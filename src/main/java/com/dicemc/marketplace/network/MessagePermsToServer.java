package com.dicemc.marketplace.network;

import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePermsToServer implements IMessage {
	public UUID guildID;
	public int permValue;
	public String permID;
	
	public MessagePermsToServer() {}
	
	public MessagePermsToServer(UUID guildID, String permID, int permValue) {
		this.guildID = guildID;
		this.permID = permID;
		this.permValue = permValue;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		guildID = pbuf.readUniqueId();
		permValue = pbuf.readInt();
		permID = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeUniqueId(guildID);
		pbuf.writeInt(permValue);
		ByteBufUtils.writeUTF8String(buf, permID);
	}

	public static class PacketPermsToServer implements IMessageHandler<MessagePermsToServer, IMessage> {
		@Override
		public IMessage onMessage(MessagePermsToServer message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessagePermsToServer message, MessageContext ctx) {			
			Guild guild = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS.get(GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).guildIndexFromUUID(message.guildID));
			guild.permissions.put(message.permID, message.permValue);
			GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
			Main.NET.sendTo(new MessagePermsToGui(guild, ctx.getServerHandler().player.getUniqueID()), ctx.getServerHandler().player);
		}
	}
}
