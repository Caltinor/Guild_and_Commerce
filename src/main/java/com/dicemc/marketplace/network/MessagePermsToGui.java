package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.gui.GuiGuildPerms;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessagePermsToGui implements IMessage{
	public Guild guild;
	public UUID player;
	
	public MessagePermsToGui() {}
	
	public MessagePermsToGui(Guild guild, UUID player) {this.guild = guild; this.player = player;}
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		try {guild = new Guild(pbuf.readCompoundTag());
		player = pbuf.readUniqueId();} catch (IOException e) {}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeCompoundTag(guild.toNBT());
		pbuf.writeUniqueId(player);
	}

	public static class PacketPermsToGui implements IMessageHandler<MessagePermsToGui, IMessage> {
		@Override
		public IMessage onMessage(MessagePermsToGui message, MessageContext ctx) {
			Main.proxy.addScheduledTaskClient(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessagePermsToGui message, MessageContext ctx) {
			Main.proxy.updatePermsGui(message.guild);
		}
	}
}
