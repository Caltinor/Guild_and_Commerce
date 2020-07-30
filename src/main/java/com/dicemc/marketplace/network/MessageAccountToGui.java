package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.events.GuiEventHandler;
import com.dicemc.marketplace.gui.GuiGuildManager;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAccountToGui implements IMessage{
	public static Account acctG;
	public static double balP;

	public MessageAccountToGui() {	}
	
	public MessageAccountToGui(Account acctG, double balP) {
		this.acctG = acctG;
		this.balP = balP;
	}
	 
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		UUID owner = pbuf.readUniqueId();
		double balance = pbuf.readDouble();
		acctG = new Account(owner, balance);
		balP = pbuf.readDouble();
	}
	 
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeUniqueId(acctG.owner);
		pbuf.writeDouble(acctG.balance);
		pbuf.writeDouble(balP);
	}
	
	public static class PacketAccountToGui implements IMessageHandler<MessageAccountToGui, IMessage> {
		@Override
		public IMessage onMessage(MessageAccountToGui message, MessageContext ctx) {
			Main.proxy.addScheduledTaskClient(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageAccountToGui message, MessageContext ctx) {
			Main.proxy.updateGuildGuiAccounts(message.acctG, message.balP);
		}
	}
}
