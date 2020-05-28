package com.dicemc.marketplace.util.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.dicemc.marketplace.gui.ContainerSell;
import com.dicemc.marketplace.gui.GuiChunkManager;
import com.dicemc.marketplace.gui.GuiGuildCreate;
import com.dicemc.marketplace.gui.GuiGuildManager;
import com.dicemc.marketplace.gui.GuiGuildMemberManager;
import com.dicemc.marketplace.gui.GuiGuildPerms;
import com.dicemc.marketplace.gui.GuiMarketManager;
import com.dicemc.marketplace.gui.GuiMarketSell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GuiProxy implements IGuiHandler{

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == 0) {return new ContainerSell(player);}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == 0) {
			List<ItemStack> itemList = new ArrayList<ItemStack>();			
			for (Map.Entry<ResourceLocation, Item> entry :ForgeRegistries.ITEMS.getEntries()) {
				itemList.add(new ItemStack(entry.getValue()));
			}
			Collections.sort(itemList, new Comparator<ItemStack>() {
				@Override
			    public int compare(ItemStack o1, ItemStack o2) {
			        return o1.getDisplayName().compareTo(o2.getDisplayName());
			    }
			});
			return new GuiMarketSell(player, itemList);
		}
		return null;
	}
}
