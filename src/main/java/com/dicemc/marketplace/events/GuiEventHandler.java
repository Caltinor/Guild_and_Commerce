package com.dicemc.marketplace.events;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.network.MessageGuiRequest;
import com.dicemc.marketplace.util.Reference;

import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(Side.CLIENT)
public class GuiEventHandler {
	static GuiButtonImage guildButton, chunkButton, playersButton, marketsButton, permsButton, depositButton, withdrawButton;
	public static final ResourceLocation INVENTORY_ADDITIONS = new ResourceLocation(Reference.MOD_ID+":guis/inventoryadditions.png");
	static int xOffset = 80;
	
	@SubscribeEvent
	public static void onInventoryLoad (GuiScreenEvent.InitGuiEvent event) {
		if (event.getGui() instanceof GuiInventory) {
			GuiInventory gui = (GuiInventory) event.getGui();
			int guiX = gui.getGuiLeft();			
			int guiY = gui.getGuiTop();		
			chunkButton = new GuiButtonImage(11, 	guiX +xOffset + 3, 		guiY - 17, 19, 17, 0, 0, 19, INVENTORY_ADDITIONS);
			marketsButton = new GuiButtonImage(12,	guiX +xOffset + 21, 	guiY - 17, 19, 17, 63, 0, 19, INVENTORY_ADDITIONS);
			guildButton = new GuiButtonImage(13, 	guiX +xOffset + 39, 	guiY - 17, 19, 17, 21, 0, 19, INVENTORY_ADDITIONS);
			playersButton = new GuiButtonImage(14, 	guiX +xOffset + 57, 	guiY - 17, 19, 17, 42, 0, 19, INVENTORY_ADDITIONS);
			permsButton = new GuiButtonImage(15, 	guiX +xOffset + 75, 	guiY - 17, 19, 17, 84, 0, 19, INVENTORY_ADDITIONS);
			depositButton = new GuiButtonImage(16, guiX + gui.getXSize(), guiY, 35, 17, 127, 0, 19, INVENTORY_ADDITIONS);
			withdrawButton = new GuiButtonImage(17, guiX + gui.getXSize(), guiY+19, 35, 17, 164, 0, 19, INVENTORY_ADDITIONS);
			event.getButtonList().add(chunkButton);
			event.getButtonList().add(guildButton);
			event.getButtonList().add(playersButton);
			event.getButtonList().add(marketsButton);
			event.getButtonList().add(permsButton);
			event.getButtonList().add(depositButton);
			event.getButtonList().add(withdrawButton);
		}
	}
	
	@SubscribeEvent
	public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent event) {
		if (event.getGui() instanceof GuiInventory) {
			GuiInventory gui = (GuiInventory) event.getGui();
			gui.mc.getTextureManager().bindTexture(INVENTORY_ADDITIONS);
			int guiX = gui.getGuiLeft();
			int guiY = gui.getGuiTop();			
			int mx = event.getMouseX();
			int my = event.getMouseY();
			drawTooltips(gui, mx, my, guiX, guiY, event.getMouseX(), event.getMouseY());
		}
	}
	
	private static void drawTooltips(GuiInventory gui, int mx, int my, int guiX, int guiY, int mouseX, int mouseY) {
		RenderHelper.enableGUIStandardItemLighting();
		if (mx > chunkButton.x && mx < chunkButton.x + chunkButton.width && my > chunkButton.y && my < chunkButton.y + chunkButton.height) {
			List<String> lines = new ArrayList<String>();
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.title.chunk").setStyle(new Style().setBold(true)).getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.chunk1").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.chunk2").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.chunk3").getFormattedText());
			gui.drawHoveringText(lines, mouseX, mouseY);
		}
		if (mx > marketsButton.x && mx < marketsButton.x + marketsButton.width && my > marketsButton.y && my < marketsButton.y + marketsButton.height) {
			List<String> lines = new ArrayList<String>();
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.title.market").setStyle(new Style().setBold(true)).getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.market1").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.market2").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.market3").getFormattedText());
			gui.drawHoveringText(lines, mouseX, mouseY);
		}
		if (mx > guildButton.x && mx < guildButton.x + guildButton.width && my > guildButton.y && my < guildButton.y + guildButton.height) {
			List<String> lines = new ArrayList<String>();
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.title.guild").setStyle(new Style().setBold(true)).getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.guild1").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.guild2").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.guild3").getFormattedText());
			gui.drawHoveringText(lines, mouseX, mouseY);
		}
		if (mx > playersButton.x && mx < playersButton.x + playersButton.width && my > playersButton.y && my < playersButton.y + playersButton.height) {
			List<String> lines = new ArrayList<String>();
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.title.members").setStyle(new Style().setBold(true)).getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.members1").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.members2").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.members3").getFormattedText());
			gui.drawHoveringText(lines, mouseX, mouseY);
		}
		if (mx > permsButton.x && mx < permsButton.x + permsButton.width && my > permsButton.y && my < permsButton.y + permsButton.height) {
			List<String> lines = new ArrayList<String>();
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.title.perms").setStyle(new Style().setBold(true)).getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.perms1").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.perms2").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.perms3").getFormattedText());
			gui.drawHoveringText(lines, mouseX, mouseY);
		}	
		if (mx > depositButton.x && mx < depositButton.x + depositButton.width && my > depositButton.y && my < depositButton.y + depositButton.height) {
			List<String> lines = new ArrayList<String>();
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.title.deposit").setStyle(new Style().setBold(true)).getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.deposit1").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.deposit2").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.deposit3").getFormattedText());
			gui.drawHoveringText(lines, mouseX, mouseY);
		}
		if (mx > withdrawButton.x && mx < withdrawButton.x + withdrawButton.width && my > withdrawButton.y && my < withdrawButton.y + withdrawButton.height) {
			List<String> lines = new ArrayList<String>();
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.title.withdraw").setStyle(new Style().setBold(true)).getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.withdraw1").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.withdraw2").getFormattedText());
			lines.add(new TextComponentTranslation("gui.inventory.tooltip.withdraw3").getFormattedText());
			gui.drawHoveringText(lines, mouseX, mouseY);
		}
		RenderHelper.disableStandardItemLighting();
	}
	
	@SubscribeEvent
	public static void onGuiClick(GuiScreenEvent.ActionPerformedEvent.Post event) {
		if (event.getGui() instanceof GuiInventory) {
			GuiInventory gui = (GuiInventory) event.getGui();
			if (event.getButton().id == 10) {
				chunkButton.x = 	gui.getGuiLeft() 	+xOffset + 3;
				marketsButton.x = 	gui.getGuiLeft()	+xOffset + 21;
				guildButton.x = 	gui.getGuiLeft()	+xOffset + 39;
				playersButton.x = 	gui.getGuiLeft()	+xOffset + 57;				
				permsButton.x = 	gui.getGuiLeft()	+xOffset + 75;				
				depositButton.x = 	gui.getGuiLeft() 	+gui.getXSize();
				withdrawButton.x = 	gui.getGuiLeft() 	+gui.getXSize();
			}
			if (event.getButton().equals(chunkButton)) Main.NET.sendToServer(new MessageGuiRequest(0));
			if (event.getButton().equals(playersButton)) Main.NET.sendToServer(new MessageGuiRequest(2));
			if (event.getButton().equals(guildButton)) Main.NET.sendToServer(new MessageGuiRequest(1));
			if (event.getButton().equals(permsButton)) Main.NET.sendToServer(new MessageGuiRequest(3));
			if (event.getButton().equals(marketsButton)) Main.NET.sendToServer(new MessageGuiRequest(4));
			if (event.getButton().equals(depositButton)) event.getGui().mc.player.sendChatMessage("/account deposit");
			if (event.getButton().equals(withdrawButton)) Main.NET.sendToServer(new MessageGuiRequest(6));
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
			if (Keyboard.getEventKey() == Keyboard.KEY_D && Keyboard.isKeyDown(Keyboard.KEY_D)) event.getGui().mc.player.sendChatMessage("/account deposit");
			if (Keyboard.getEventKey() == Keyboard.KEY_W && Keyboard.isKeyDown(Keyboard.KEY_W)) Main.NET.sendToServer(new MessageGuiRequest(5));
		}
	}
}
