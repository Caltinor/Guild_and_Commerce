package com.dicemc.marketplace.events;

import org.lwjgl.input.Keyboard;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.network.MessageGuiRequest;
import com.dicemc.marketplace.util.Reference;

import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class GuiEventHandler {
	static GuiButtonImage guildButton, chunkButton, playersButton, marketsButton, permsButton;
	public static final ResourceLocation INVENTORY_ADDITIONS = new ResourceLocation(Reference.MOD_ID+":guis/inventoryadditions.png");	
	
	@SubscribeEvent
	public static void onInventoryLoad (GuiScreenEvent.InitGuiEvent event) {
		if (event.getGui() instanceof GuiInventory) {
			int guiX = (event.getGui().width - 176)/2;
			int xOffset = 80;
			int guiY = (event.getGui().height - 166)/2;			
			chunkButton = new GuiButtonImage(11, 	guiX +xOffset + 3, 		guiY - 17, 19, 17, 0, 0, 19, INVENTORY_ADDITIONS);
			marketsButton = new GuiButtonImage(12,	guiX +xOffset + 21, 	guiY - 17, 19, 17, 63, 0, 19, INVENTORY_ADDITIONS);
			guildButton = new GuiButtonImage(13, 	guiX +xOffset + 39, 	guiY - 17, 19, 17, 21, 0, 19, INVENTORY_ADDITIONS);
			playersButton = new GuiButtonImage(14, 	guiX +xOffset + 57, 	guiY - 17, 19, 17, 42, 0, 19, INVENTORY_ADDITIONS);
			permsButton = new GuiButtonImage(15, 	guiX +xOffset + 75, 	guiY - 17, 19, 17, 84, 0, 19, INVENTORY_ADDITIONS);
			event.getButtonList().add(chunkButton);
			event.getButtonList().add(guildButton);
			event.getButtonList().add(playersButton);
			event.getButtonList().add(marketsButton);
			event.getButtonList().add(permsButton);
		}
	}
	
	@SubscribeEvent
	public static void onGuiClick(GuiScreenEvent.ActionPerformedEvent.Post event) {
		if (event.getGui() instanceof GuiInventory) {
			if (event.getButton().equals(chunkButton)) Main.NET.sendToServer(new MessageGuiRequest(0));
			if (event.getButton().equals(playersButton)) Main.NET.sendToServer(new MessageGuiRequest(2));
			if (event.getButton().equals(guildButton)) Main.NET.sendToServer(new MessageGuiRequest(1));
			if (event.getButton().equals(permsButton)) Main.NET.sendToServer(new MessageGuiRequest(3));
			if (event.getButton().equals(marketsButton)) Main.NET.sendToServer(new MessageGuiRequest(4));
		}
	}
	
	@SubscribeEvent
	public static void onKeyPress(GuiScreenEvent.KeyboardInputEvent.Post event) {
		if (event.getGui() instanceof GuiInventory) {
			if (Keyboard.getEventKey() == Keyboard.KEY_1 && Keyboard.isKeyDown(Keyboard.KEY_1)) Main.NET.sendToServer(new MessageGuiRequest(0));
			if (Keyboard.getEventKey() == Keyboard.KEY_2 && Keyboard.isKeyDown(Keyboard.KEY_2)) Main.NET.sendToServer(new MessageGuiRequest(4));
			if (Keyboard.getEventKey() == Keyboard.KEY_3 && Keyboard.isKeyDown(Keyboard.KEY_3)) Main.NET.sendToServer(new MessageGuiRequest(1));
			if (Keyboard.getEventKey() == Keyboard.KEY_4 && Keyboard.isKeyDown(Keyboard.KEY_4)) Main.NET.sendToServer(new MessageGuiRequest(2));
			if (Keyboard.getEventKey() == Keyboard.KEY_5 && Keyboard.isKeyDown(Keyboard.KEY_5)) Main.NET.sendToServer(new MessageGuiRequest(3));
		}
	}
}
