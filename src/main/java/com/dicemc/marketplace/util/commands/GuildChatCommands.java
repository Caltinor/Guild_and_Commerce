package com.dicemc.marketplace.util.commands;

import java.util.ArrayList;
import java.util.List;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class GuildChatCommands extends CommandBase{

	@Override
	public String getName() {return "myguild";}

	@Override
	public List<String> getAliases() {
		List<String> al = new ArrayList<String>();
		al.add("mg");
		return al;
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}
	
	@Override
	public String getUsage(ICommandSender sender) {return "/myguild <message>";}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		List<Guild> glist = GuildSaver.get(sender.getEntityWorld()).GUILDS;
		int gindex = -1;
		for (int i = 0; i < glist.size(); i++) {
			if (glist.get(i).members.getOrDefault(sender.getCommandSenderEntity().getUniqueID() , -2) >= 0) {
				gindex = i;
				continue;
			}
		}
		//parse arguments into a message string
		if (gindex == -1) return;
		String body = "";
		for (int i = 0; i < args.length; i++) {
			body = body + args[i] + " ";
		}
		//send message to all online members.
		String src = "<"+sender.getName()+">";
		for (EntityPlayerMP plyrs : server.getPlayerList().getPlayers()) {
			if (glist.get(gindex).members.getOrDefault(plyrs.getUniqueID(), -1) >= 0) {
				plyrs.sendMessage(new TextComponentTranslation("cmd.guild.pm", src, body).setStyle(new Style().setColor(TextFormatting.GOLD)));
			}
		}
	}

}
