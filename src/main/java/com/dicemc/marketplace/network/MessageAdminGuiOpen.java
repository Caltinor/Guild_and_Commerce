package com.dicemc.marketplace.network;

import com.dicemc.marketplace.Main;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAdminGuiOpen implements IMessage{

	public MessageAdminGuiOpen() {}
	 
	@Override
	public void fromBytes(ByteBuf buf) {}
	 
	@Override
	public void toBytes(ByteBuf buf) {}
	
	public static class PacketAdminGuiOpen implements IMessageHandler<MessageAdminGuiOpen, IMessage> {
		@Override
		public IMessage onMessage(MessageAdminGuiOpen message, MessageContext ctx) {
			Main.proxy.addScheduledTaskClient(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageAdminGuiOpen message, MessageContext ctx) { Main.proxy.openAdminGui();}
	}
}
