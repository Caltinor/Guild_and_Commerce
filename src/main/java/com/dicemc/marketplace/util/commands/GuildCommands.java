package com.dicemc.marketplace.util.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;

public class GuildCommands extends CommandBase{
	TextComponentTranslation tctGetUsage = new TextComponentTranslation("cmd.guild.getusage");

	@Override
	public String getName() { return "guild"; }

	@Override
	public String getUsage(ICommandSender sender) { return tctGetUsage.getFormattedText();}

	private void message(ITextComponent str, ICommandSender sender) {sender.sendMessage(str);}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		List<Guild> glist = GuildSaver.get(sender.getEntityWorld()).GUILDS;
		int gindex = -1;
		if (args.length == 0) {
			message(new TextComponentTranslation("cmd.guild.help1"), sender);
			message(new TextComponentTranslation("cmd.guild.help2"), sender);
			return;
		}
		switch(args[0]) {
			case "info": {
				int gid = -1;
				if (args.length >= 2) {
				String gname = args[2];
				for (int i = 2; i < args.length; i++) gname += " "+ args[i];				
				for (int i = 0; i < glist.size(); i++) {
					if (glist.get(i).guildName.equalsIgnoreCase(gname)) gid = i; }
				}
				else {
					UUID landID = sender.getEntityWorld().getChunkFromChunkCoords(sender.getCommandSenderEntity().chunkCoordX, sender.getCommandSenderEntity().chunkCoordZ).getCapability(ChunkProvider.CHUNK_CAP, null).getOwner();
					for (int i = 0; i < glist.size(); i++) {
						if (glist.get(i).guildID.equals(landID)) gid = i; }
				}
				if (gid >= 0) {
					Style style = new Style();
					message(new TextComponentString(TextFormatting.GOLD+"==="+glist.get(gid).guildName+"==="), sender);
					int membercount = 0;
					for (Map.Entry<UUID, Integer> entry : glist.get(gid).members.entrySet()) membercount += entry.getValue() != -1 ? 1: 0;
					message(new TextComponentTranslation("cmd.guild.info.line1", String.valueOf(glist.get(gid).guildWorth(sender.getEntityWorld()))), sender);
					message(new TextComponentTranslation("cmd.guild.info.line1", String.valueOf(glist.get(gid).taxableWorth(sender.getEntityWorld()))), sender);
					TextComponentTranslation msg = new TextComponentTranslation("cmd.guild.info.line3");
					TextComponentString a1 = new TextComponentString(TextFormatting.WHITE+String.valueOf(glist.get(gid).coreLand.size()));
					TextComponentString a2 = new TextComponentString(TextFormatting.WHITE+String.valueOf(glist.get(gid).outpostLand.size()));
					msg.appendSibling(new TextComponentTranslation("cmd.guild.info.line3a").setStyle(style.setColor(TextFormatting.BLUE)).appendSibling(a1));
					msg.appendSibling(new TextComponentTranslation("cmd.guild.info.line3b").setStyle(style.setColor(TextFormatting.RED)).appendSibling(a2));
					message(msg, sender);
					message(new TextComponentTranslation("cmd.guild.info.line4", String.valueOf(glist.get(gid).guildTax*100), String.valueOf(glist.get(gid).openToJoin)), sender);
					message(new TextComponentTranslation("cmd.guild.info.line5", String.valueOf(AccountSaver.get(sender.getEntityWorld()).getGuilds().getBalance(glist.get(gid).guildID))).setStyle(style.setColor(TextFormatting.DARK_GREEN)), sender);
					message(new TextComponentTranslation("cmd.guild.info.line6", String.valueOf(membercount)), sender);
				}
				else message(new TextComponentTranslation("cmd.guild.info.error"), sender);
				break;
			}
			case "land": {
				int cX = sender.getCommandSenderEntity().chunkCoordX;
				int cZ = sender.getCommandSenderEntity().chunkCoordZ;
				ChunkCapability cap = sender.getCommandSenderEntity().getEntityWorld().getChunkFromChunkCoords(cX, cZ).getCapability(ChunkProvider.CHUNK_CAP, null);
				switch (args[1]) {
				case "add": {
					cap.includePlayer(server.getPlayerProfileCache().getGameProfileForUsername(args[2]).getId());
					break;
				}
				case "remove": {
					cap.removePlayer(server.getPlayerProfileCache().getGameProfileForUsername(args[2]).getId());
					break;
				}
				case "setlevel": {
					cap.setPermMin(Integer.valueOf(args[2]));
					break;
				}
				default:
				}
				break;
			}
			case "leave": {
				for (int i = 0; i < glist.size(); i++) {
					if (glist.get(i).members.containsKey(sender.getCommandSenderEntity().getUniqueID())) {
						if (glist.get(i).members.get(sender.getCommandSenderEntity().getUniqueID()) >= 0) gindex = i;
						break;
					}
				}
				if (gindex == -1) {message(new TextComponentTranslation("cmd.guild.leave.fail"), sender); break;}
				for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
					if (glist.get(gindex).members.getOrDefault(player.getUniqueID(), -1) >=0) {
						player.sendMessage(new TextComponentTranslation("cmd.guild.leave.notify", sender.getName()));
					}
				}
				String nameholder = glist.get(gindex).guildName;
				UUID uuidHolder = glist.get(gindex).guildID;
				GuildSaver.get(sender.getEntityWorld()).GUILDS.get(gindex).members.remove(sender.getCommandSenderEntity().getUniqueID());
				if (glist.get(gindex).listMembers(0, server).size() <= 1) GuildSaver.get(sender.getEntityWorld()).GUILDS.get(gindex).members.replaceAll((key, old) -> old == 1 ? 0 : old);
				if (glist.get(gindex).listMembers(0, server).size() <= 1) GuildSaver.get(sender.getEntityWorld()).GUILDS.get(gindex).members.replaceAll((key, old) -> old == 2 ? 0 : old);
				if (glist.get(gindex).listMembers(0, server).size() <= 1) GuildSaver.get(sender.getEntityWorld()).GUILDS.get(gindex).members.replaceAll((key, old) -> old == 3 ? 0 : old);
				if (glist.get(gindex).members.size() == 0 && !glist.get(gindex).isAdmin) {
					GuildSaver.get(sender.getEntityWorld()).GUILDS.remove(gindex);
					AccountSaver.get(sender.getEntityWorld()).getGuilds().removeAccount(uuidHolder);
				}
				GuildSaver.get(sender.getEntityWorld()).markDirty();
				message(new TextComponentTranslation("cmd.guild.leave.success", nameholder), sender);
				break;
			}
			default: message(new TextComponentTranslation("cmd.admin.error"), sender);
			break;
		}
	}
}
