package com.dicemc.marketplace.util.commands;

import java.util.List;
import java.util.UUID;

import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.item.ModItems;
import com.dicemc.marketplace.util.Reference;
import com.dicemc.marketplace.util.datasaver.AccountSaver;
import com.dicemc.marketplace.util.datasaver.GuildSaver;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class AccountCommands extends CommandBase {
	TextComponentTranslation tctGetusage = new TextComponentTranslation("cmd.account.getusage");
	TextComponentTranslation tctBalNoGuild = new TextComponentTranslation("cmd.account.balancenoguild");
	TextComponentTranslation tctBalHasGuild = new TextComponentTranslation("cmd.account.balancehasguild");
	TextComponentTranslation tctDepSuc = new TextComponentTranslation("cmd.account.depositsuccess");
	TextComponentTranslation tctWithSuc = new TextComponentTranslation("cmd.account.withdrawsuccess");
	TextComponentTranslation tctHelp1 = new TextComponentTranslation("cmd.account.help1");
	TextComponentTranslation tctHelp2 = new TextComponentTranslation("cmd.account.help2");
	TextComponentTranslation tctHelp3 = new TextComponentTranslation("cmd.account.help3");
	TextComponentTranslation tctGenArgNR = new TextComponentTranslation("cmd.general.argnotrecognized");
	
	@Override
	public String getName() { return "account"; }

	@Override
	public String getUsage(ICommandSender sender) {	return tctGetusage.getFormattedText(); }
	
	private void message(ITextComponent str, ICommandSender sender) {sender.sendMessage(str);}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			double acctP = AccountSaver.get(sender.getEntityWorld()).getPlayers().getBalance(sender.getCommandSenderEntity().getUniqueID());
			List<Guild> glist = GuildSaver.get(sender.getEntityWorld()).GUILDS;
			UUID g = Reference.NIL;
			for (int i = 0; i < glist.size(); i++) {
				if (glist.get(i).members.containsKey(sender.getCommandSenderEntity().getUniqueID())) {
					if (glist.get(i).members.get(sender.getCommandSenderEntity().getUniqueID()) >= 0) g = glist.get(i).guildID;
				}
			}
			double acctG = AccountSaver.get(sender.getEntityWorld()).getGuilds().getBalance(g);
			TextComponentString msg = new TextComponentString("$"+String.valueOf(acctP));
			msg.appendSibling(g.equals(Reference.NIL) ? tctBalNoGuild : tctBalHasGuild.appendText(String.valueOf(acctG)+"]"));
			message(msg, sender);
			return;
		}
		switch(args[0]) {
		//base arguments <deposit/withdraw/guild/help>
		case "deposit": {
			EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(sender.getCommandSenderEntity().getUniqueID());
			double value = 0;
			for (int i = 0; i < player.inventoryContainer.inventorySlots.size(); i++) {
				if (player.inventoryContainer.getSlot(i).getStack().getItem().equals(ModItems.MONEYBAG)) {
					value += (player.inventoryContainer.getSlot(i).getStack().getTagCompound().getDouble("value") * player.inventoryContainer.getSlot(i).getStack().getCount());
					player.inventoryContainer.getSlot(i).putStack(ItemStack.EMPTY);
				}
			}
			AccountSaver.get(sender.getEntityWorld()).getPlayers().addBalance(sender.getCommandSenderEntity().getUniqueID(), value);
			AccountSaver.get(sender.getEntityWorld()).markDirty();
			TextComponentString msg = new TextComponentString("$"+String.valueOf(value));
			msg.appendSibling(tctDepSuc);
			message(msg, sender);
			break;
		}
		case "withdraw": {
			double balP = AccountSaver.get(sender.getEntityWorld()).getPlayers().getBalance(sender.getCommandSenderEntity().getUniqueID());
			if (balP >= Math.abs(Double.valueOf(args[1]))) {
				AccountSaver.get(sender.getEntityWorld()).getPlayers().addBalance(sender.getCommandSenderEntity().getUniqueID(), -1 * Math.abs(Double.valueOf(args[1])));
				ItemStack item = new ItemStack(ModItems.MONEYBAG);
				item.setTagInfo("value", new NBTTagDouble(Math.abs(Double.valueOf(args[1]))));
				server.getPlayerList().getPlayerByUUID(sender.getCommandSenderEntity().getUniqueID()).addItemStackToInventory(item);
			}
			TextComponentString msg = new TextComponentString("$"+args[1]);
			msg.appendSibling(tctWithSuc);
			message(msg, sender);
			break;
		}
		case "help": {
			message(tctHelp1, sender);
			message(tctHelp2, sender);
			message(tctHelp3, sender);
			break;
		}
		default: message(tctGenArgNR, sender);
		break;
		}
	}
}
