package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.gui.GuiChunkManager;
import com.dicemc.marketplace.gui.GuiChunkManager.ChunkSummary;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageChunkToGui implements IMessage{
	public List<ChunkSummary> chunkSummary;
	public List<Integer> mapColors;
	public UUID playerGuildID;
	public boolean canGuildClaim, canGuildSell;
	public String response;
	public double acctPlayer, acctGuild;
	public boolean isUpdate;
	
	public MessageChunkToGui() {}
	
	public MessageChunkToGui(boolean isUpdate, List<ChunkSummary> chunkSummary, List<Integer> mapColors, UUID playerGuildID, boolean canGuildClaim, boolean canGuildSell, String response, double acctP, double acctG) {
		this.isUpdate = isUpdate;
		this.chunkSummary = chunkSummary;
		this.mapColors = mapColors;
		this.playerGuildID = playerGuildID;
		this.canGuildClaim = canGuildClaim;
		this.canGuildSell = canGuildSell;
		this.response = response;
		this.acctPlayer = acctP;
		this.acctGuild = acctG;
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
		playerGuildID = pbuf.readUniqueId();
		canGuildClaim = pbuf.readBoolean();
		canGuildSell = pbuf.readBoolean();
		response = ByteBufUtils.readUTF8String(buf);
		acctPlayer = pbuf.readDouble();
		acctGuild = pbuf.readDouble();
		isUpdate = pbuf.readBoolean();
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
		pbuf.writeUniqueId(playerGuildID);
		pbuf.writeBoolean(canGuildClaim);
		pbuf.writeBoolean(canGuildSell);
		ByteBufUtils.writeUTF8String(buf, response);
		pbuf.writeDouble(acctPlayer);
		pbuf.writeDouble(acctGuild);
		pbuf.writeBoolean(isUpdate);
	}
	public static class PacketChunkToGui implements IMessageHandler<MessageChunkToGui, IMessage> {
		@Override
		public IMessage onMessage(MessageChunkToGui message, MessageContext ctx) {
			Main.proxy.addScheduledTaskClient(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageChunkToGui message, MessageContext ctx) {
			if (!message.isUpdate) Main.proxy.openChunkGui(message.chunkSummary, message.mapColors, message.playerGuildID, message.canGuildClaim, message.canGuildSell, message.response, message.acctPlayer, message.acctGuild);
			if (message.isUpdate) GuiChunkManager.guiUpdate(message.chunkSummary, message.mapColors, message.playerGuildID, message.canGuildClaim, message.canGuildSell, message.response, message.acctPlayer, message.acctGuild);
		}
	}
}
