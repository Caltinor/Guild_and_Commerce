package com.dicemc.marketplace.item;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.WhitelistItem;
import com.dicemc.marketplace.util.IHasModel;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class ItemWhitelister extends Item implements IHasModel{
	
	public ItemWhitelister(String name) {
		setUnlocalizedName(name);
		setRegistryName(name);
		setCreativeTab(CreativeTabs.MATERIALS);
		ModItems.ITEMS.add(this);
	}
	
	@Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		try {nbt = stack.getTagCompound();
		list = nbt.getTagList("whitelister", Constants.NBT.TAG_COMPOUND);} catch (NullPointerException e) {}
		for (int i = 0; i < list.tagCount(); i++) {
			WhitelistItem wlItem = new WhitelistItem(list.getCompoundTagAt(i));
			tooltip.add(TextFormatting.GOLD+ wlItem.getBlock().substring(wlItem.getBlock().indexOf(":") != -1 ? wlItem.getBlock().indexOf(":")+1 : 0) + 
					wlItem.getEntity().substring(wlItem.getEntity().indexOf("Entity") != -1 ? wlItem.getEntity().indexOf("Entity")+ 6 : 0) + 
					TextFormatting.RED+" Break:"+ (wlItem.getCanBreak() ? "Yes":"No") + TextFormatting.BLUE+" Interact:"+ (wlItem.getCanInteract() ? "Yes":"No"));	
		}
    }
	
	@Override
	public void registerModels() {
		Main.proxy.registerItemRenderer(this, 0, "inventory");		
	}
	
	public static List<WhitelistItem> getWhitelist(ItemStack stack) {
		List<WhitelistItem> whitelist = new ArrayList<WhitelistItem>();
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		try {nbt = stack.getTagCompound();
		list = nbt.getTagList("whitelister", Constants.NBT.TAG_COMPOUND);} catch (NullPointerException e) {}
		for (int i = 0; i < list.tagCount(); i++) {
			WhitelistItem wlItem = new WhitelistItem(list.getCompoundTagAt(i));
			whitelist.add(wlItem);	
		}
		return whitelist;
	}
	
	public static void setWhitelister(ItemStack stack, List<WhitelistItem> whitelist) {
		stack.setTagCompound(new NBTTagCompound());
		NBTTagList list = new NBTTagList();
		for (WhitelistItem entry : whitelist) {
			list.appendTag(entry.toNBT());
		}
		stack.setTagInfo("whitelister", list);
	}
	
	public static void addToWhitelister(ItemStack stack, String block, boolean enableCanBreak, boolean enableCanInteract) {
		List<WhitelistItem> whitelist = getWhitelist(stack);
		WhitelistItem wlItem = new WhitelistItem(block);
		boolean existingEntry = false;
		for (WhitelistItem entry : whitelist) {
			if (entry.getBlock().equals(block)) {
				entry.setCanBreak(enableCanBreak ? (entry.getCanBreak() ? false: true) : entry.getCanBreak());
				entry.setCanInteract(enableCanInteract ? (entry.getCanInteract() ? false: true) : entry.getCanInteract());
				existingEntry = true;
				break;
			}
		}
		if (!existingEntry) {
			wlItem.setCanBreak(enableCanBreak ? true : wlItem.getCanBreak());
			wlItem.setCanInteract(enableCanInteract ? true : wlItem.getCanInteract());
			whitelist.add(wlItem);
		}
		NBTTagList list = new NBTTagList();
		for (WhitelistItem entry : whitelist) {
			list.appendTag(entry.toNBT());
		}
		stack.setTagInfo("whitelister", list);
	}
	
	public static void addToWhitelister(ItemStack stack, Entity entity, boolean enableCanBreak, boolean enableCanInteract) {
		List<WhitelistItem> whitelist = getWhitelist(stack);
		WhitelistItem wlItem = new WhitelistItem(entity);
		boolean existingEntry = false;
		for (WhitelistItem entry : whitelist) {
			if (entry.getEntity() == "") continue;
			if (entry.getEntity().equalsIgnoreCase(entity.getClass().getSimpleName())) {
				entry.setCanBreak(enableCanBreak ? (entry.getCanBreak() ? false: true) : entry.getCanBreak());
				entry.setCanInteract(enableCanInteract ? (entry.getCanInteract() ? false: true) : entry.getCanInteract());
				existingEntry = true;
				break;
			}
		}
		if (!existingEntry) {
			wlItem.setCanBreak(enableCanBreak ? true : wlItem.getCanBreak());
			wlItem.setCanInteract(enableCanInteract ? true : wlItem.getCanInteract());
			whitelist.add(wlItem);
		}
		NBTTagList list = new NBTTagList();
		for (WhitelistItem entry : whitelist) {
			list.appendTag(entry.toNBT());
		}
		stack.setTagInfo("whitelister", list);
	}
}
