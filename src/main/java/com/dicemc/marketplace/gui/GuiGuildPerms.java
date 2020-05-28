package com.dicemc.marketplace.gui;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.network.MessagePermsToServer;
import com.dicemc.marketplace.util.Reference;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.text.TextFormatting;

public class GuiGuildPerms extends GuiScreen {
	private static GuiButton setB[][] = new GuiButton[10][4];
	public static final String permIndex[] = new String[10];
	private double arrayX, arrayY;
	private static int rankOffset;
	
	public static Guild guild;
	
	public static void syncGui(Guild guild) {
		GuiGuildPerms.guild = guild;
		updateButtonEnables();
	}
	
	public GuiGuildPerms(Guild guild) {
		this.guild = guild;
		
		permIndex[0] = "setname";
		permIndex[1] = "setopen";
		permIndex[2] = "settax";
		permIndex[3] = "setperms";
		permIndex[4] = "setinvite";
		permIndex[5] = "setkick";
		permIndex[6] = "setclaim";
		permIndex[7] = "setsell";
		permIndex[8] = "setwithdraw";
		permIndex[9] = "setpromotedemote";
		rankOffset = 25;
	}
	
	public void initGui() {
		arrayX = (this.width/2)-40;
		arrayY = 25;
		for (int r = 0; r < 10; r++) {
			for (int c = 0; c < 4; c++) {
				String idx = String.valueOf(r)+String.valueOf(c);
				setB[r][c] = new GuiButton(Integer.valueOf(idx), (int)arrayX + (c*20), (int)arrayY + (r*20), 20, 20, String.valueOf(c));
				this.buttonList.add(setB[r][c]);
			}
		}
		this.buttonList.add(new GuiButton(16, this.width - 78, 3, 75, 20, "Back"));
		updateButtonEnables();
		
	}
	
	private static void updateButtonEnables() {		
		rankOffset = 25;
		for (int r = 0; r < 10; r++) {
			for (int c = 0; c < 4; c++) {
				setB[r][c].enabled = true;
			}
		}
		for (int b = 0; b < 4; b++) {
			int count = 0;
			for (Map.Entry<UUID, Integer> entry : guild.members.entrySet()) {if(entry.getValue() == b) count++;}
			if (b == 0 && count == 0) setB[3][b].enabled = false;
			else if (count == 0 && !setB[3][b-1].enabled) setB[3][b].enabled = false;
		}
		for (int i = 0; i < 10; i++) {
			setB[i][guild.permissions.get(permIndex[i])].enabled = false;
		}
		//permission based changes
		if (guild.members.getOrDefault(Minecraft.getMinecraft().player.getUniqueID(), 4) > guild.permissions.getOrDefault("setperms", 0)) {
			for (int r = 0; r < 10; r++) {
				for (int c = 0; c < 4; c++) {
					setB[r][c].enabled = false;
					setB[r][c].visible = false;
				}
			}
			rankOffset = -60;
		}
	}
	
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 16) {
			mc.player.closeScreen();
			mc.getMinecraft().displayGuiScreen(new GuiInventory(mc.player));
		}
		if (button.id != 16) {Main.NET.sendToServer(new MessagePermsToServer(guild.guildID, permIndex[(button.id/10)] , (button.id%10)));}
	}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, TextFormatting.GOLD+"Guild Permissions", this.width/2, 3, 16777215);
        //Draw the permission descriptions
        int o = 130;
        this.drawString(this.fontRenderer, "Change Guild Name  ", setB[0][0].x - o, setB[0][0].y+5, 16777215);
        this.drawString(this.fontRenderer, "Change Open-To-Join", setB[1][0].x - o, setB[1][0].y+5, 16777215);
        this.drawString(this.fontRenderer, "Change Guild Tax   ", setB[2][0].x - o, setB[2][0].y+5, 16777215);
        this.drawString(this.fontRenderer, "Change Permissions ", setB[3][0].x - o, setB[3][0].y+5, 16777215);
        this.drawString(this.fontRenderer, "Change Invite Level", setB[4][0].x - o, setB[4][0].y+5, 16777215);
        this.drawString(this.fontRenderer, "Change Kick Level  ", setB[5][0].x - o, setB[5][0].y+5, 16777215);
        this.drawString(this.fontRenderer, "Change Claim Level ", setB[6][0].x - o, setB[6][0].y+5, 16777215);
        this.drawString(this.fontRenderer, "Change Sell Level  ", setB[7][0].x - o, setB[7][0].y+5, 16777215);
        this.drawString(this.fontRenderer, "Change Withdraw Lvl", setB[8][0].x - o, setB[8][0].y+5, 16777215);
        this.drawString(this.fontRenderer, "Change Member Ranks", setB[9][0].x - o, setB[9][0].y+5, 16777215);
        //Draw the current setting
        for (int v = 0; v < 10; v++) this.drawString(this.fontRenderer, permName(permIndex[v]) , setB[v][3].x + rankOffset, setB[v][0].y+5, 16777215);
        //Next
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
	
	private String permName(String key) {
		String str = "";
		switch (guild.permissions.get(key)) {
		case 0: {
			str = TextFormatting.DARK_GREEN+ guild.permLevels.get(guild.permissions.get(key));
			break;
		}
		case 1: {
			str = TextFormatting.DARK_PURPLE+ guild.permLevels.get(guild.permissions.get(key));
			break;
		}
		case 2: {
			str = TextFormatting.BLUE+ guild.permLevels.get(guild.permissions.get(key));
			break;
		}
		case 3: {
			str = guild.permLevels.get(guild.permissions.get(key));
			break;
		}
		default:
		}
		return str;
	}
	
}
