package com.dicemc.marketplace.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;


public class MessageGuildToGui implements IMessage {
	public int guiType;
	public Guild guild;
	public Account acctG;
	public double worthT, worthG, balP;
	public Map<UUID, String> memberNames;
	public Map<ChunkPos, Double> chunkValues;
	public UUID player;

	public MessageGuildToGui() {}
	
	public MessageGuildToGui(int guiType, Guild guild, Account acctG, double worthT, double worthG, Map<UUID, String> mbrNames, Map<ChunkPos, Double> chunkValues, double balP, UUID player) {
		this.guiType = guiType;
		this.guild = guild;
		this.acctG = acctG;
		this.worthG = worthG;
		this.worthT = worthT;
		this.memberNames = mbrNames;
		this.chunkValues = chunkValues;
		this.balP = balP;
		this.player = player;
	}
	
	public NBTTagCompound mapToNBT(Map<UUID, String> map) {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (Map.Entry<UUID, String> entry : map.entrySet()) {
			NBTTagCompound snbt = new NBTTagCompound();
			snbt.setUniqueId("key", entry.getKey());
			snbt.setString("value", entry.getValue());
			list.appendTag(snbt);
		}
		compound.setTag("discriminators", list);
		return compound;
	}
	
	public Map<UUID, String> mapFromNBT(NBTTagCompound nbt) {
		Map<UUID, String> map = new HashMap<UUID, String>();
		NBTTagList list = nbt.getTagList("discriminators", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			map.put(list.getCompoundTagAt(i).getUniqueId("key"), list.getCompoundTagAt(i).getString("value"));
		}
		return map;
	}
	
	@SuppressWarnings("static-access")
	public NBTTagCompound chunkMapToNBT(Map<ChunkPos, Double> chunkValues) {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (Map.Entry<ChunkPos, Double> entry : chunkValues.entrySet()) {
			NBTTagCompound snbt = new NBTTagCompound();
			snbt.setLong("chunkpos", entry.getKey().asLong(entry.getKey().x, entry.getKey().z));
			snbt.setDouble("price", entry.getValue());
			list.appendTag(snbt);
		}
		compound.setTag("chunks", list);
		return compound;
	}
	
	public Map<ChunkPos, Double> chunkMapFromNBT(NBTTagCompound nbt) {
		Map<ChunkPos, Double> map = new HashMap<ChunkPos, Double>();
		NBTTagList list = nbt.getTagList("chunks", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			ChunkPos pos = GuildSaver.chunkFromLong(list.getCompoundTagAt(i).getLong("chunkpos"));
			map.put(pos, list.getCompoundTagAt(i).getDouble("price"));
		}
		return map;
	}
	 
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		try {guild = new Guild(pbuf.readCompoundTag());
		UUID owner = pbuf.readUniqueId();
		double balance = pbuf.readDouble();
		acctG = new Account(owner, balance);
		worthG = pbuf.readDouble();
		worthT = pbuf.readDouble();
		memberNames = mapFromNBT(pbuf.readCompoundTag());
		chunkValues = chunkMapFromNBT(pbuf.readCompoundTag());
		guiType = pbuf.readInt();
		balP = pbuf.readDouble();
		player = pbuf.readUniqueId();
		} catch (IOException e) {e.printStackTrace();}
	}
	 
	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeCompoundTag(guild.toNBT());
		pbuf.writeUniqueId(acctG.owner);
		pbuf.writeDouble(acctG.balance);
		pbuf.writeDouble(worthG);
		pbuf.writeDouble(worthT);
		pbuf.writeCompoundTag(mapToNBT(memberNames));
		pbuf.writeCompoundTag(chunkMapToNBT(chunkValues));
		pbuf.writeInt(guiType);
		pbuf.writeDouble(balP);
		pbuf.writeUniqueId(player);
	}
	
	public static class PacketGuildToGui implements IMessageHandler<MessageGuildToGui, IMessage> {
		@Override
		public IMessage onMessage(MessageGuildToGui message, MessageContext ctx) {
			Main.proxy.addScheduledTaskClient(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageGuildToGui message, MessageContext ctx) {
			switch (message.guiType) {
			case 1: {
				Main.proxy.openGuildGui(message.guild, message.acctG, message.worthT, message.worthG, message.chunkValues, message.balP);
				break;
			}
			case 2: {
				Main.proxy.openMemberGui(message.guild, message.memberNames);
				break;
			}
			case 3: {
				Main.proxy.openPermsGui(message.guild);
				break;
			}
			default:
			}
		}
	}
}
