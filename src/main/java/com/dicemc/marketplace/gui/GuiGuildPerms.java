package com.dicemc.marketplace.gui;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.network.MessagePermsToServer;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class GuiGuildPerms extends GuiScreen {
	private GuiListPerms permList;
	private GuiButton set0, set1, set2, set3;
	private boolean hasPermission = true;
	private String permIndex[];	
	private Guild guild;
	
	public void syncGui(Guild guild) {
		this.guild = guild;
		permList.refreshList();
		if (permList.selectedIdx >= 0 && hasPermission) {
        	set0.enabled = permList.getSelectedMember().permLvlRank == 0 ? false : true;
        	set1.enabled = permList.getSelectedMember().permLvlRank == 1 ? false : true;
        	set2.enabled = permList.getSelectedMember().permLvlRank == 2 ? false : true;
        	set3.enabled = permList.getSelectedMember().permLvlRank == 3 ? false : true;
        }
	}
	
	public GuiGuildPerms(Guild guild) {
		this.guild = guild;
		permIndex = new String[guild.permissions.size()];
		int i = 0;
		for (Map.Entry<String, Integer> entry : guild.permissions.entrySet()) {permIndex[i] = entry.getKey(); i++;}
	}
	
	public void initGui() {
		permList = new GuiListPerms(this, mc, (this.width-350)/2, 30, 250, this.height - 40, 11);
		set0 = new GuiButton(10, permList.x+permList.width+3, this.height/2 - 44, 100, 20, guild.permLevels.get(0));
		set1 = new GuiButton(11, permList.x+permList.width+3, this.height/2 - 22, 100, 20, guild.permLevels.get(1));
		set2 = new GuiButton(12, permList.x+permList.width+3, this.height/2 + 2, 100, 20, guild.permLevels.get(2));
		set3 = new GuiButton(13, permList.x+permList.width+3, this.height/2 + 24, 100, 20, guild.permLevels.get(3));
		this.buttonList.add(new GuiButton(16, this.width - 22, 2, 20, 20, "X"));
		this.buttonList.add(set0);
		this.buttonList.add(set1);
		this.buttonList.add(set2);
		this.buttonList.add(set3);	
		hasPermission = guild.members.getOrDefault(mc.player.getUniqueID(), 3) <= guild.permissions.getOrDefault("setperms", 0);
		set0.enabled = hasPermission ? true : false;
		set1.enabled = hasPermission ? true : false;
		set2.enabled = hasPermission ? true : false;
		set3.enabled = hasPermission ? true : false;
	}
	
	public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        permList.handleMouseInput();
    }
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        permList.mouseClicked(mouseX, mouseY, mouseButton);
        if (permList.selectedIdx >= 0 && hasPermission) {
        	set0.enabled = permList.getSelectedMember().permLvlRank == 0 ? false : true;
        	set1.enabled = permList.getSelectedMember().permLvlRank == 1 ? false : true;
        	set2.enabled = permList.getSelectedMember().permLvlRank == 2 ? false : true;
        	set3.enabled = permList.getSelectedMember().permLvlRank == 3 ? false : true;
        }
    }
	
	protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        permList.mouseReleased(mouseX, mouseY, state);
    }
	
	@SuppressWarnings("static-access")
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 16) {
			mc.player.closeScreen();
			mc.getMinecraft().displayGuiScreen(new GuiInventory(mc.player));
		}
		if (button == set0 && permList.selectedIdx >= 0) {Main.NET.sendToServer(new MessagePermsToServer(guild.guildID, permList.getSelectedMember().permName , 0));}
		if (button == set1 && permList.selectedIdx >= 0) {Main.NET.sendToServer(new MessagePermsToServer(guild.guildID, permList.getSelectedMember().permName , 1));}
		if (button == set2 && permList.selectedIdx >= 0) {Main.NET.sendToServer(new MessagePermsToServer(guild.guildID, permList.getSelectedMember().permName , 2));}
		if (button == set3 && permList.selectedIdx >= 0) {Main.NET.sendToServer(new MessagePermsToServer(guild.guildID, permList.getSelectedMember().permName , 3));}
	}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, new TextComponentTranslation("gui.perms.header").setStyle(new Style().setColor(TextFormatting.GOLD)).getFormattedText(), this.width/2, 3, 16777215);
        permList.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

	public class GuiListPerms extends GuiNewListExtended<GuiNewListExtended.IGuiNewListEntry>{
	    private final GuiGuildPerms permsManager;
	    public Guild guild;
	    private final List<GuiListPermsEntry> entries = Lists.<GuiListPermsEntry>newArrayList();
	    /** Index to the currently selected world */
	    private int selectedIdx = -1;
		
		public GuiListPerms(GuiGuildPerms guiGM, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.permsManager = guiGM;
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	entries.clear();
	    	guild = permsManager.guild;	    	
	        Map<String, Integer> permissions = guild.permissions;

	        for (Map.Entry<String, Integer> entry : permissions.entrySet()){
	            this.entries.add(new GuiListPermsEntry(this, entry.getKey(), entry.getValue()));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListPermsEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListPermsEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListPermsEntry implements GuiNewListExtended.IGuiNewListEntry{
		public String permName; 
		public int permLvlRank;
		private Minecraft client = Minecraft.getMinecraft();
	    private final GuiListPerms containingListSel;
		
		public GuiListPermsEntry (GuiListPerms listSelectionIn, String permName, int permLvlRank) {
			containingListSel = listSelectionIn;
			this.permName = permName;
			this.permLvlRank = permLvlRank;
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
	        this.client.fontRenderer.drawString(permDescription(permName), x+3, y , 16777215);
	        this.client.fontRenderer.drawString(permName(permLvlRank), x+180, y , 16777215);
		}
		
		private String permName(int key) {
			String str = "Missing Rank";
			switch (key) {
			case 0: {
				str = TextFormatting.DARK_GREEN+ containingListSel.guild.permLevels.get(key);
				break;
			}
			case 1: {
				str = TextFormatting.DARK_PURPLE+ containingListSel.guild.permLevels.get(key);
				break;
			}
			case 2: {
				str = TextFormatting.BLUE+ containingListSel.guild.permLevels.get(key);
				break;
			}
			case 3: {
				str = containingListSel.guild.permLevels.get(key);
				break;
			}
			default:
			}
			return str;
		}
		
		private String permDescription(String title) { return new TextComponentTranslation("gui.perms."+title).getFormattedText(); 
		}

		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
	        this.containingListSel.selectMember(slotIndex);
	        this.containingListSel.showSelectionBox = true;
	        return false;
		}

		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {		
		}

	}
}
