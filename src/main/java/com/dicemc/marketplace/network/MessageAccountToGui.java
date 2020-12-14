package com.dicemc.marketplace.network;

import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAccountToGui implements IMessage{
	public int type;
	public Account acctG;
	public double balP;

	public MessageAccountToGui() {	}
	
	public MessageAccountToGui(int type, Account acctG, double balP) {
		this.type = type;
		this.acctG = acctG;
		this.balP = balP;
	}
	 
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		type = pbuf.readInt();
		UUID owner = pbuf.readUniqueId();
		double balance = pbuf.readDouble();
		acctG = new Account(owner, balance);
		balP = pbuf.readDouble();
	}
	 
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeInt(type);
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
			if (message.type == 0) Main.proxy.updateGuildGuiAccounts(message.acctG, message.balP);
			if (message.type == 1) Main.proxy.openAccountGui(message.balP);
 		}
	}
}
