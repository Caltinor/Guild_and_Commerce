package com.dicemc.marketplace.gui;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.network.MessageCreateInfoToServer;
import com.dicemc.marketplace.util.Reference;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.text.TextFormatting;

public class GuiGuildCreate extends GuiScreen {
	private Map<UUID, String> invitedGuilds;
	private double balance, guildPrice;
	//Components
	private GuiTextField guildName;
	private GuiListGuildInvite inviteList;
	private GuiButton createButton;
	private GuiButton joinButton, rejectButton;

	
	public GuiGuildCreate(Map<UUID, String> invitedGuilds, double balP, double guildPrice) {
		this.invitedGuilds = invitedGuilds;
		this.balance = balP;
		this.guildPrice = guildPrice;
	}
	
	public void initGui() {
		inviteList = new GuiListGuildInvite(this, mc, this.width/2 + 10, 40, this.width/2 - 20, this.height-80, 10);
		guildName = new GuiTextField(1, this.fontRenderer, this.width/4 - (this.width/8), this.height/2, this.width/4, 20);
		createButton = new GuiButton(10, guildName.x +((this.width/4 - this.width/5)/2), guildName.y + 25, this.width/5, 20, "Create Guild");
		joinButton = new GuiButton(11, inviteList.x, inviteList.y+inviteList.height+5, inviteList.width/2-1, 20, "Join");
		rejectButton = new GuiButton(12, joinButton.x+joinButton.width+2, joinButton.y, joinButton.width, 20, "Reject");
		this.buttonList.add(new GuiButton(13, this.width-22, 2, 20, 20, "X"));
		this.buttonList.add(createButton);
		this.buttonList.add(joinButton);
		this.buttonList.add(rejectButton);
		//Disabling unusable buttons
		if (balance < Main.ModConfig.GUILD_CREATE_COST) createButton.enabled = false;
	}
	
	public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        inviteList.handleMouseInput();
    }
	
	@SuppressWarnings("static-access")
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 10 && guildName.getText().length() > 0) {//create guild
			Main.NET.sendToServer(new MessageCreateInfoToServer(0, Reference.NIL, guildName.getText()));
			mc.player.closeScreen();
			mc.getMinecraft().displayGuiScreen(new GuiInventory(mc.player));
		}
		else if (button.id == 11) {//join guild
			if (inviteList.selectedIdx >= 0) {
				Main.NET.sendToServer(new MessageCreateInfoToServer(1, inviteList.getSelectedMember().guildID, ""));
				mc.player.closeScreen();
				mc.getMinecraft().displayGuiScreen(new GuiInventory(mc.player));
			}
		}
		else if (button.id == 12) {//reject guild
			if (inviteList.selectedIdx >= 0 && inviteList.getSelectedMember().name.startsWith(TextFormatting.RED+"INV"))	{
				Main.NET.sendToServer(new MessageCreateInfoToServer(2, inviteList.getSelectedMember().guildID, ""));
			}
		}
		else if (button.id == 13) {//exit
			mc.player.closeScreen();
			mc.getMinecraft().displayGuiScreen(new GuiInventory(mc.player));
		}
	}
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        guildName.mouseClicked(mouseX, mouseY, mouseButton);
        inviteList.mouseClicked(mouseX, mouseY, mouseButton);
    }
	
	protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        inviteList.mouseReleased(mouseX, mouseY, state);
    }
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
		super.keyTyped(typedChar, keyCode);
		if (this.guildName.isFocused()) guildName.textboxKeyTyped(typedChar, keyCode);
    }
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, TextFormatting.GOLD+"To access guild options, accept an invite from the list or create your own Guild.", this.width/2, 3, 16777215);
        this.drawString(this.fontRenderer, "New Guild Cost $"+String.valueOf(guildPrice), guildName.x, guildName.y - 26, 16777215);
        this.drawString(this.fontRenderer, "Name:", guildName.x, guildName.y - 13, 16777215);
        this.drawString(this.fontRenderer, "Guild Invites", inviteList.x, inviteList.y - 13, 16777215);
        inviteList.drawScreen(mouseX, mouseY, partialTicks);
        guildName.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
	
	
	public class GuiListGuildInvite extends GuiNewListExtended<GuiNewListExtended.IGuiNewListEntry>{
	    private final GuiGuildCreate guiManager;
	    public Map<UUID, String> invitedGuilds;
	    private final List<GuiListGuildInviteEntry> entries = Lists.<GuiListGuildInviteEntry>newArrayList();
	    /** Index to the currently selected world */
	    private int selectedIdx = -1;
		
		public GuiListGuildInvite(GuiGuildCreate guiGM, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.guiManager = guiGM;
			this.invitedGuilds = guiGM.invitedGuilds;
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	this.invitedGuilds = guiManager.invitedGuilds;
	    	entries.clear();
	    	if (invitedGuilds.size() > 0) {		    	
		        Map<UUID, String> members = invitedGuilds;	
		        for (Map.Entry<UUID, String> entry : members.entrySet()){
		            this.entries.add(new GuiListGuildInviteEntry(this, entry.getKey(), entry.getValue()));
		        }
	    	}
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListGuildInviteEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListGuildInviteEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListGuildInviteEntry implements GuiNewListExtended.IGuiNewListEntry{
		public final UUID guildID;
		private final String name; 
		private Minecraft client = Minecraft.getMinecraft();
	    private final GuiListGuildInvite containingListSel;
		
		public GuiListGuildInviteEntry (GuiListGuildInvite listSelectionIn, UUID guild, String name) {
			containingListSel = listSelectionIn;
			this.guildID = guild;	
			this.name = name;
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
	        this.client.fontRenderer.drawString(name, x+3, y , 16777215);
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
