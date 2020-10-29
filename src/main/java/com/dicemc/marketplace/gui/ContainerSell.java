package com.dicemc.marketplace.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSell extends Container{
	private final EntityPlayer player;
	public InventoryBasic saleInv;
	
	public ContainerSell(EntityPlayer player) {
		this.player = player;
		saleInv  = new InventoryBasic("Sell Slot", true, 9);
		this.addSlotToContainer(new Slot (saleInv, 0, 26, 29));
		for (int k = 0; k < 3; k++)
        {
            for (int i1 = 0; i1 < 9; i1++)
            {
                this.addSlotToContainer(new Slot(player.inventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }		
		for (int l = 0; l < 9; l++)
        {
            this.addSlotToContainer(new Slot(player.inventory, l, 8 + l * 18, 142));
        }		
	}
	
	public void onContainerClosed(EntityPlayer playerIn)
    {
        super.onContainerClosed(playerIn);

        if (!player.getEntityWorld().isRemote)
        {
            this.clearContainer(playerIn, player.getEntityWorld(), saleInv);
        }
    }
	
	public void updateInvSlotPosition(int offsetMod) {
		for (int i = 1; i < this.inventorySlots.size(); i++) {
			if (offsetMod >= 0) {this.inventorySlots.get(i).xPos += offsetMod; }
			if (offsetMod < 0) {this.inventorySlots.get(i).xPos -= Math.abs(offsetMod); }
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {if (player.world != null ) return true; return false;}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			stack = slotStack.copy();

			if (index == 0) {
				if (!mergeItemStack(slotStack, 9, inventorySlots.size(), true)) {return ItemStack.EMPTY;}
				slot.onSlotChange(slotStack, stack);
			}
			else if (index >= 9 && index < inventorySlots.size()) {return ItemStack.EMPTY;}
			else if (!mergeItemStack(slotStack, 9, inventorySlots.size(), false)) {return ItemStack.EMPTY;}

			if (slotStack.isEmpty()) {slot.putStack(ItemStack.EMPTY);}
			else { slot.onSlotChanged();}

			if (slotStack.getCount() == stack.getCount())
				return ItemStack.EMPTY;

			slot.onTake(player, slotStack);
		}

		return stack;
    }
}
