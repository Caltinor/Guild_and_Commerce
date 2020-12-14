package com.dicemc.marketplace.util.commands;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.capabilities.ChunkCapability;
import com.dicemc.marketplace.util.capabilities.ChunkProvider;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class TempClaimCommands extends CommandBase{
	TextComponentTranslation tctGetUsage = new TextComponentTranslation("cmd.temp.getusage");

	@Override
	public String getName() {return "tempclaim";}
	
	@Override
	public List<String> getAliases() {
		List<String> al = new ArrayList<String>();
		al.add("tempclaim");
		al.add("temp");
		al.add("tc");
		return al;
	}
	
	@Override
	public String getUsage(ICommandSender sender) {return tctGetUsage.getFormattedText();	}
	
	private void message(ITextComponent str, ICommandSender sender) {sender.sendMessage(str);}
	
	//TODO: Simplify and move this function
	//it should only return the owner, not interact with the chunk
	//MessageGuiRequest and the update to GuiChunkManager should interact with chunk properties.
	public static String ownerName(UUID owner, ChunkCapability cap, MinecraftServer server) {
		List<Guild> glist = GuildSaver.get(server.getEntityWorld()).GUILDS;
		String str = "Unowned";
		if (!cap.getOwner().equals(Reference.NIL)) {
			for (int i = 0; i < glist.size(); i++) {
				if (glist.get(i).guildID.equals(cap.getOwner())) {
					str = glist.get(i).guildName; break;
				}
			}
			if (str == "Unowned" && cap.getTempTime() >= System.currentTimeMillis()) str = Commands.playerNamefromUUID(server, cap.getOwner());
			if (str == "Unowned" && cap.getTempTime() < System.currentTimeMillis()) {
				cap.setOwner(Reference.NIL);
				cap.setPublic(false);
				cap.setOutpost(false);
				cap.setPlayers(new ArrayList<UUID>());
				cap.fromNBTWhitelist(new NBTTagList());
			}
		}
		return str;
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		ChunkCapability cap = sender.getEntityWorld().getChunkFromChunkCoords(sender.getCommandSenderEntity().chunkCoordX, sender.getCommandSenderEntity().chunkCoordZ).getCapability(ChunkProvider.CHUNK_CAP, null);
		if (args.length == 0) {
			message(new TextComponentString(CoreUtils.tempClaim(sender.getCommandSenderEntity().getUniqueID(), sender.getCommandSenderEntity().chunkCoordX, sender.getCommandSenderEntity().chunkCoordZ, false)), sender);		
			return;
		}
		switch(args[0]) {
		//base arguments <renew/set/add/remove/info>
		case "renew": {
			message(new TextComponentString(CoreUtils.renewClaim(sender.getCommandSenderEntity().getUniqueID(), sender.getCommandSenderEntity().chunkCoordX, sender.getCommandSenderEntity().chunkCoordZ)), sender);
			break;
		}
		case "add": {
			//Maybe add functionality where non-guild members can be added.  renters?
			if (cap.getOwner().equals(sender.getCommandSenderEntity().getUniqueID())) {
				if (Commands.playerUUIDfromString(server, args[1]) != null) {
					cap.includePlayer(Commands.playerUUIDfromString(server, args[1]));
				}
				else message(new TextComponentTranslation("cmd.temp.error.playernotfound"), sender);
			}
			else message(new TextComponentTranslation("cmd.temp.error.owneraction"), sender);
			break;
		}
		case "remove": {
			if (cap.getOwner().equals(sender.getCommandSenderEntity().getUniqueID())) {
				if (Commands.playerUUIDfromString(server, args[1]) != null) {
					UUID player = Commands.playerUUIDfromString(server, args[1]);
					for (int i = 0; i < cap.getPlayers().size(); i++) {
						if (cap.getPlayers().get(i).equals(player)) {
							message(new TextComponentTranslation("cmd.temp.remove", args[1]), sender);
							cap.getPlayers().remove(i);
							break;
						}
					}
				}
				else message(new TextComponentTranslation("cmd.temp.error.playernotfound"), sender);
			}
			else message(new TextComponentTranslation("cmd.temp.error.owneraction"), sender);
			break;
		}
		case "info": {
			Style style = new Style();
			message(new TextComponentString(TextFormatting.GOLD+"==="+ownerName(cap.getOwner(), cap, server)+"==="), sender);
			message(new TextComponentTranslation("cmd.temp.info.line1", String.valueOf(cap.getPrice())), sender);
			if (cap.getTempTime() < System.currentTimeMillis())	message(new TextComponentTranslation("cmd.temp.info.line2a", new TextComponentTranslation("cmd.temp.info.line2b").setStyle(style.setColor(TextFormatting.BLUE))), sender);
			if (cap.getTempTime() >= System.currentTimeMillis()) message(new TextComponentTranslation("cmd.temp.info.line2a", TextFormatting.GREEN+String.valueOf(new Timestamp(cap.getTempTime()))), sender);
			message(new TextComponentTranslation("cmd.temp.info.line3",String.valueOf(cap.getPublic()), String.valueOf(cap.getOutpost())), sender);
			message(new TextComponentTranslation("cmd.temp.info.line4").setStyle(style.setColor(TextFormatting.AQUA)), sender);
			for (UUID player : cap.getPlayers()) {
				message(new TextComponentString(Commands.playerNamefromUUID(server, player)), sender);
			}
			break;
		}
		default: break;
		}
	}

}
