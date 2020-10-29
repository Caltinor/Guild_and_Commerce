package com.dicemc.marketplace.network;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCreateInfoToServer implements IMessage{
	public UUID guildID;
	public String name;
	public int action;
	
	public MessageCreateInfoToServer() {}
	
	public MessageCreateInfoToServer(int action, UUID guild, String name) {
		this.action = action;
		this.guildID = guild;
		this.name = name;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		guildID = pbuf.readUniqueId();
		action = pbuf.readInt();
		name = pbuf.readString(32);
		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer pbuf = new PacketBuffer(buf);
		pbuf.writeUniqueId(guildID);
		pbuf.writeInt(action);
		pbuf.writeString(name);
		
	}

	public static class PacketCreateInfoToServer implements IMessageHandler<MessageCreateInfoToServer, IMessage> {
		@Override
		public IMessage onMessage(MessageCreateInfoToServer message, MessageContext ctx) {
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}  
		
		private void handle(MessageCreateInfoToServer message, MessageContext ctx) {
			switch (message.action) {
			case 0: {// create Guild
				ctx.getServerHandler().player.sendMessage(new TextComponentString(CoreUtils.createGuild(ctx.getServerHandler().player.getUniqueID(), message.name)));
				break;
			}
			case 1: {// join guild
				ctx.getServerHandler().player.sendMessage(new TextComponentString(CoreUtils.joinGuild(message.guildID, ctx.getServerHandler().player.getUniqueID())));
				break;
			}
			case 2: {// reject guild
				List<Guild> glist = GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).GUILDS;
				for (int i = 0; i < glist.size(); i++) {
					if (glist.get(i).guildID.equals(message.guildID)) {
						glist.get(i).members.remove(ctx.getServerHandler().player.getUniqueID());
						break;
					}
				}
				GuildSaver.get(ctx.getServerHandler().player.getEntityWorld()).markDirty();
				Map<UUID, String> list = new HashMap<UUID, String>();
				for (int i = 0; i < glist.size(); i++) {
					if (glist.get(i).members.getOrDefault(ctx.getServerHandler().player.getUniqueID(), 4) == -1) list.put(glist.get(i).guildID, TextFormatting.RED+
							new TextComponentTranslation("gui.members.INVITE").getFormattedText()+glist.get(i).guildName);
					else if (glist.get(i).openToJoin) list.put(glist.get(i).guildID, TextFormatting.BLUE+
							new TextComponentTranslation("gui.members.OPEN").getFormattedText()+glist.get(i).guildName);
				}
				double balance = AccountSaver.get(ctx.getServerHandler().player.getEntityWorld()).getPlayers().getBalance(ctx.getServerHandler().player.getUniqueID());
				Main.NET.sendTo(new MessageCreateInfoToGui(list, balance, Main.ModConfig.GUILD_CREATE_COST), ctx.getServerHandler().player);
				break;
			}
			default:
			}
		}
	}
}
