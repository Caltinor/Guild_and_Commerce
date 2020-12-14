package com.dicemc.marketplace.network;

import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.AccountGroup;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAccountInfoToServer implements IMessage{
	public Account acctG; 
	public UUID player;
	public double amount;
	public boolean isDeposit;

	//this constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
	public MessageAccountInfoToServer() {}
	
	//unused until I need the UUID to pass for the check
	public MessageAccountInfoToServer(Account acctG, UUID player, double amount, boolean isDeposit) {
		this.acctG = acctG;
		this.player = player;
		this.amount = amount;
		this.isDeposit = isDeposit;
	}
	 
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		UUID owner = pbuf.readUniqueId();
		double balance = pbuf.readDouble();
		acctG = new Account(owner, balance);
		player = pbuf.readUniqueId();
		isDeposit = pbuf.readBoolean();
		amount = pbuf.readDouble();
	}
	 
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeUniqueId(acctG.owner);
		pbuf.writeDouble(acctG.balance);
		pbuf.writeUniqueId(player);
		pbuf.writeBoolean(isDeposit);
		pbuf.writeDouble(amount);
	}
	
	public static class PacketAccountInfoToServer implements IMessageHandler<MessageAccountInfoToServer, IMessage> {
		@Override
		public IMessage onMessage(MessageAccountInfoToServer message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageAccountInfoToServer message, MessageContext ctx) {
			if (message.isDeposit) {
				if (AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getPlayers().getBalance(message.player) >= message.amount) {
					AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getPlayers().addBalance(message.player, (-1 *message.amount));
					AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGuilds().addBalance(message.acctG.owner, message.amount);
				}
			}
			else if (!message.isDeposit) {
				if (AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGuilds().getBalance(message.acctG.owner) >= message.amount) {
					AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGuilds().addBalance(message.acctG.owner, (-1 *message.amount));
					AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getPlayers().addBalance(message.player, message.amount);
				}
			}
			AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
			AccountGroup acctGlist = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getGuilds();		
			double balP = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getPlayers().getBalance(ctx.getServerHandler().player.getUniqueID());
			Main.NET.sendTo(new MessageAccountToGui(0, new Account(message.acctG.owner, acctGlist.getBalance(message.acctG.owner)), balP), ctx.getServerHandler().player);
		}
	}
}
