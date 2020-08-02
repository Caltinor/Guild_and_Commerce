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
import net.minecraft.util.text.TextComponentString;

public class AccountCommands extends CommandBase {
	@Override
	public String getName() { return "account"; }

	@Override
	public String getUsage(ICommandSender sender) { return "/account <action>";}
	
	private void message(String str, ICommandSender sender) {sender.sendMessage(new TextComponentString(str));}

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
			String strG = g.equals(Reference.NIL) ? " [No Guild]" : " [GUILD $"+String.valueOf(acctG)+"]";
			message("$"+String.valueOf(acctP)+strG, sender);
			return;
		}
		switch(args[0]) {
		//base arguments <deposit/withdraw/guild/help>
		case "deposit": {
			EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(sender.getCommandSenderEntity().getUniqueID());
			double value = 0;
			for (int i = 0; i < player.inventoryContainer.inventorySlots.size(); i++) {
				if (player.inventoryContainer.getSlot(i).getStack().getItem().equals(ModItems.MONEYBAG)) {
					System.out.println("Moneybag detected");
					value += (player.inventoryContainer.getSlot(i).getStack().getTagCompound().getDouble("value") * player.inventoryContainer.getSlot(i).getStack().getCount());
					player.inventoryContainer.getSlot(i).putStack(ItemStack.EMPTY);
				}
			}
			AccountSaver.get(sender.getEntityWorld()).getPlayers().addBalance(sender.getCommandSenderEntity().getUniqueID(), value);
			AccountSaver.get(sender.getEntityWorld()).markDirty();
			message("$"+String.valueOf(value)+" deposited to account from Moneybags.", sender);
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
			message("$"+args[1]+" Moneybag placed in inventory.", sender);
			break;
		}
		case "help": {
			message("/account  (shows yours and your guild's balances)", sender);
			message("/account deposit (deposits all moneybags in your inventory to your account", sender);
			message("/account withdraw <amount> (puts money from your account into your inventory in a moneybag)", sender);
			break;
		}
		default: message("Argument not recognized", sender);
		break;
		}
	}
}
