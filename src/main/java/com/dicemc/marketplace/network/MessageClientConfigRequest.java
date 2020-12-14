package com.dicemc.marketplace.network;

import com.dicemc.marketplace.Main;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageClientConfigRequest implements IMessage{
	public int x, z, type;
	
	public MessageClientConfigRequest() {}
	public MessageClientConfigRequest(int type, int x, int z) {
		this.type = type;
		this.x = x;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		type = buf.readInt();
		x = buf.readInt();
		z = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(type);
		buf.writeInt(x);
		buf.writeInt(z);
	}
	
	public static class PacketClientConfigRequest implements IMessageHandler<MessageClientConfigRequest, IMessage> {
		@Override
		public IMessage onMessage(MessageClientConfigRequest message, MessageContext ctx) {
			Main.proxy.addScheduledTaskClient(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(MessageClientConfigRequest message, MessageContext ctx) {
			if (Main.ModConfig.AUTO_TEMP_CLAIM) {
				Main.NET.sendToServer(new MessageAutoTempClaim(message.x, message.z));
			}
		}
	}
}