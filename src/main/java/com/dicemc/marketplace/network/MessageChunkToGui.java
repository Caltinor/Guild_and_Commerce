package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.gui.GuiChunkManager.ChunkSummary;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageChunkToGui implements IMessage{
	public Guild guild;
	public List<ChunkSummary> chunkSummary;
	public List<Integer> mapColors;
	public String response;
	public double acctPlayer, acctGuild, tcr;
	public boolean isUpdate;
	
	public MessageChunkToGui() {}
	
	public MessageChunkToGui(boolean isUpdate, Guild guild, List<ChunkSummary> chunkSummary, List<Integer> mapColors, String response, double acctP, double acctG, double tempClaimRate) {
		this.guild = guild;
		this.isUpdate = isUpdate;
		this.chunkSummary = chunkSummary;
		this.mapColors = mapColors;
		this.response = response;
		this.acctPlayer = acctP;
		this.acctGuild = acctG;
		this.tcr = tempClaimRate;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		int size = pbuf.readInt();
		chunkSummary = new ArrayList<ChunkSummary>();
		for (int i = 0; i < size; i++) {
			try { chunkSummary.add(ChunkSummary.fromNBT(pbuf.readCompoundTag())); } catch (IOException e) {}
		}
		size = pbuf.readInt();
		mapColors = new ArrayList<Integer>();
		for (int i = 0; i < size; i++) {
			mapColors.add(pbuf.readInt());
		}
		response = ByteBufUtils.readUTF8String(buf);
		acctPlayer = pbuf.readDouble();
		acctGuild = pbuf.readDouble();
		tcr = pbuf.readDouble();
		isUpdate = pbuf.readBoolean();
		try { guild = new Guild(pbuf.readCompoundTag()); } catch (IOException e) {}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeInt(chunkSummary.size());
		for (int i = 0; i < chunkSummary.size(); i++) {
			pbuf.writeCompoundTag(ChunkSummary.toNBT(chunkSummary.get(i)));
		}
		pbuf.writeInt(mapColors.size());
		for (int i = 0; i < mapColors.size(); i++) {
			pbuf.writeInt(mapColors.get(i));
		}
		ByteBufUtils.writeUTF8String(buf, response);
		pbuf.writeDouble(acctPlayer);
		pbuf.writeDouble(acctGuild);
		pbuf.writeDouble(tcr);
		pbuf.writeBoolean(isUpdate);
		pbuf.writeCompoundTag(guild.toNBT());
	}
	public static class PacketChunkToGui implements IMessageHandler<MessageChunkToGui, IMessage> {
		@Override
		public IMessage onMessage(MessageChunkToGui message, MessageContext ctx) {
			Main.proxy.addScheduledTaskClient(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageChunkToGui message, MessageContext ctx) {
			if (!message.isUpdate) Main.proxy.openChunkGui(message.guild, message.chunkSummary, message.mapColors, message.response, message.acctPlayer, message.acctGuild, message.tcr);
			if (message.isUpdate) Main.proxy.updateChunkGui(message.guild, message.chunkSummary, message.mapColors, message.response, message.acctPlayer, message.acctGuild, message.tcr);
		}
	}
}
