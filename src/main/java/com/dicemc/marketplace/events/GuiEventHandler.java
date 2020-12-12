package com.dicemc.marketplace.events;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.network.MessageGuiRequest;
import com.dicemc.marketplace.util.Reference;

import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class GuiEventHandler {
	static GuiButtonImage guildButton, chunkButton, playersButton, marketsButton, permsButton;
	public static final ResourceLocation INVENTORY_ADDITIONS = new ResourceLocation(Reference.MOD_ID+":guis/inventoryadditions.png");
	static int xOffset = 80;
	
	@SubscribeEvent
	public static void onInventoryLoad (GuiScreenEvent.InitGuiEvent event) {
		if (event.getGui() instanceof GuiInventory) {
			int guiX = (event.getGui().width - 176)/2;			
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
	public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent event) {
		if (event.getGui() instanceof GuiInventory) {
			int guiX = (event.getGui().width - 176)/2;
			int guiY = (event.getGui().height - 166)/2;
			GuiInventory gui = (GuiInventory) event.getGui();
			int mx = event.getMouseX();
			int my = event.getMouseY();
			if (mx > guiX + xOffset + 3 && mx < guiX + xOffset + 19 && my > guiY-17 && my < guiY) {
				List<String> lines = new ArrayList<String>();
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.title.chunk").setStyle(new Style().setBold(true)).getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.chunk1").getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.chunk2").getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.chunk3").getFormattedText());
				gui.drawHoveringText(lines, event.getMouseX(), event.getMouseY());
			}
			if (mx > guiX + xOffset + 21 && mx < guiX + xOffset + 21 + 19 && my > guiY-17 && my < guiY) {
				List<String> lines = new ArrayList<String>();
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.title.market").setStyle(new Style().setBold(true)).getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.market1").getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.market2").getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.market3").getFormattedText());
				gui.drawHoveringText(lines, event.getMouseX(), event.getMouseY());
			}
			if (mx > guiX + xOffset + 39 && mx < guiX + xOffset + 39 + 19 && my > guiY-17 && my < guiY) {
				List<String> lines = new ArrayList<String>();
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.title.guild").setStyle(new Style().setBold(true)).getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.guild1").getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.guild2").getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.guild3").getFormattedText());
				gui.drawHoveringText(lines, event.getMouseX(), event.getMouseY());
			}
			if (mx > guiX + xOffset + 57 && mx < guiX + xOffset + 57 + 19 && my > guiY-17 && my < guiY) {
				List<String> lines = new ArrayList<String>();
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.title.members").setStyle(new Style().setBold(true)).getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.members1").getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.members2").getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.members3").getFormattedText());
				gui.drawHoveringText(lines, event.getMouseX(), event.getMouseY());
			}
			if (mx > guiX + xOffset + 75 && mx < guiX + xOffset + 75 + 19 && my > guiY-17 && my < guiY) {
				List<String> lines = new ArrayList<String>();
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.title.perms").setStyle(new Style().setBold(true)).getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.perms1").getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.perms2").getFormattedText());
				lines.add(new TextComponentTranslation("gui.inventory.tooltip.perms3").getFormattedText());
				gui.drawHoveringText(lines, event.getMouseX(), event.getMouseY());
			}
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
