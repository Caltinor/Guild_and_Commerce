package com.dicemc.marketplace.network;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.util.MktPktType;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAdminToServer implements IMessage {
	public int messageIndex;

	public MessageAdminToServer() {}
	
	public MessageAdminToServer(boolean isGuildAccount, UUID owner, double balance) { //placeholder for account changes
		messageIndex = 0;
	}
	
	public MessageAdminToServer(Guild guild) { //placeholder for guild_main changes	
		messageIndex = 1;
	}
	
	public MessageAdminToServer(UUID guildID, List<ChunkPos> coreLand, List<ChunkPos> outpostLand) { //placeholder for guild_Land changes
		messageIndex = 2;
	}
	
	public MessageAdminToServer(UUID guildID, Map<UUID, Integer> members) { //placeholder for guild_member changes	
		messageIndex = 3;
	}
	
	public MessageAdminToServer(MktPktType type) { //placeholder for market changes.  this needs probably a lot more parameters
		messageIndex = 4;
	}
	
	
	@Override
	public void fromBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		
	}
	
	public static class PacketAdminToServer implements IMessageHandler<MessageAdminToServer, IMessage> {
		@Override
		public IMessage onMessage(MessageAdminToServer message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageAdminToServer message, MessageContext ctx) {
			switch (message.messageIndex) {
			case 0: { //acct
				break;
			}
			case 1: { //guild_main
				break;
			}
			case 2: { //guild_land
				break;
			}
			case 3: { //guild_member
				break;
			}
			case 4: { //market
				break;
			}
			default:
			}
		}
	}

}
