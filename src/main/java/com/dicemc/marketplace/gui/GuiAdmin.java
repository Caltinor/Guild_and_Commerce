package com.dicemc.marketplace.gui;

import java.io.IOException;

import com.dicemc.marketplace.util.AdminGuiType;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiAdmin extends GuiScreen{
	private AdminGuiType activeMenu = AdminGuiType.NONE;
	private GuiButton toggleAccount, toggleGuild, toggleMarket, exitButton;
	
	public GuiAdmin() {}
	
	public void initGui() {
		toggleAccount = new GuiButton(10, 3, 3, 75, 20, "Accounts");
		toggleGuild = new GuiButton(11, 3, toggleAccount.y + 23, 75, 20, "Guild");
		toggleMarket = new GuiButton(12, 3, toggleGuild.y + 23, 75, 20, "Markets");
		exitButton = new GuiButton(13, 3, this.height - 30, 75, 20, "Exit");
		this.buttonList.add(toggleAccount);
		this.buttonList.add(toggleGuild);
		this.buttonList.add(toggleMarket);
		this.buttonList.add(exitButton);
	}
	
	public void handleMouseInput() throws IOException {
        super.handleMouseInput();
    }
	
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == exitButton) mc.player.closeScreen();
	}
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
    }
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
    }
    
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    	this.drawDefaultBackground();
    	switch (activeMenu) {
    	//draw only the objects for the specfic screen toggled
    	case ACCOUNT: {
    		break;
    	}
    	case GUILD_SELECT: {
    		break;
    	}
    	case GUILD_MAIN: {
    		break;
    	}
    	case GUILD_LAND: {
    		break;
    	}
    	case GUILD_MEMBER: {
    		break;
    	}
    	case MARKET: {
    		break;
    	}
    	default:
    	}
    	super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
