package com.dicemc.marketplace.network;

import java.util.UUID;

import com.dicemc.marketplace.core.CoreUtils;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageAutoTempClaim implements IMessage{
	public int x, z;
	
	public MessageAutoTempClaim() {}
	
	public MessageAutoTempClaim(int x, int z) {
		this.x = x;
		this.z = z;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		z = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(z);
	}
	
	public static class PacketAutoTempClaim implements IMessageHandler<MessageAutoTempClaim, IMessage> {
		@Override
		public IMessage onMessage(MessageAutoTempClaim message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		private void handle(MessageAutoTempClaim message, MessageContext ctx) {
			UUID owner = ctx.getServerHandler().player.getUniqueID();
			ctx.getServerHandler().player.sendStatusMessage(new TextComponentString(CoreUtils.tempClaim(owner, message.x, message.z, true)), true);
		}
		
	}
}
