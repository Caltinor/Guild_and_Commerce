package com.dicemc.marketplace.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.network.MessageGuildInfoToServer;
import com.dicemc.marketplace.network.MessageMemberInfoToServer;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class GuiGuildMemberManager extends GuiScreen{
	private Guild guild;
	private Map<UUID, String> mbrNames;
	private GuiListGuildMembers memberList, inviteList;
	private GuiTextField inviteText;
	private GuiButton guiProm, guiDemo, guiKick;
	private boolean permProDemo, permKick;
	private boolean inviteListClicked;
	private Map<String, Boolean> discriminators = new HashMap<String, Boolean>();
	
	public void syncMembers(Guild updatedguild, Map<UUID, String> memberNames) {
		guild = updatedguild; 
		mbrNames = memberNames;
		inviteList.guild = updatedguild;
		inviteList.mbrNames = memberNames;
		inviteList.refreshList();
	}
	
	public GuiGuildMemberManager(Guild guild, Map<UUID, String> memberNames) {
		this.guild = guild;
		inviteListClicked = true;
		this.mbrNames = memberNames;
		newDescrims();
	}
	
	public void newDescrims() {
		discriminators.put("name", false);
		discriminators.put("open", false);
		discriminators.put("tax", false);
		discriminators.put("permnames", false);
		discriminators.put("permvalues", false);
		discriminators.put("members", false);
	}
	
	public void initGui() {		
		memberList = new GuiListGuildMembers(this, mc, false, 3, 30, this.width/3, this.height-60, 10);
		inviteList = new GuiListGuildMembers(this, mc, true, this.width-(this.width/3)-3, 30, this.width/3, this.height-60, 10);
		inviteText = new GuiTextField(1, this.fontRenderer, (this.width / 2)-this.width/8, this.height-85, this.width/4, 20);
		this.buttonList.add(new GuiButton(10, (this.width / 2)-38, this.height - 28, 75, 20, new TextComponentTranslation("gui.back").getFormattedText()));
		guiProm = new GuiButton(50, inviteText.x, 30, inviteText.width, inviteText.height, new TextComponentTranslation("gui.members.promote").getFormattedText());
		guiDemo = new GuiButton(51, inviteText.x, 55, inviteText.width, inviteText.height, new TextComponentTranslation("gui.members.demote").getFormattedText());
		guiKick = new GuiButton(52, inviteText.x, 80, inviteText.width, inviteText.height, new TextComponentTranslation("gui.members.kick").getFormattedText());
		this.buttonList.add(new GuiButton(11, inviteText.x, inviteText.y+inviteText.height+5, inviteText.width, 20, new TextComponentTranslation("gui.members.invite").getFormattedText()));
		this.buttonList.add(guiProm);
		this.buttonList.add(guiDemo);
		this.buttonList.add(guiKick);
		//permission based disables
		permProDemo = (guild.members.getOrDefault(mc.player.getUniqueID(), 4) > guild.permissions.get("setpromotedemote")) ? false : true;
		permKick = (guild.members.getOrDefault(mc.player.getUniqueID(), 4) > guild.permissions.get("setkick")) ? false : true;
		if (guild.members.getOrDefault(mc.player.getUniqueID(), 4) > guild.permissions.get("setinvite")) for (GuiButton button : this.buttonList) if (button.id == 11) button.enabled = false;
		if (!permProDemo) {guiProm.enabled = false; guiDemo.enabled = false;}
		if (!permKick) guiKick.enabled = false;
	}
	
	public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        memberList.handleMouseInput();
        inviteList.handleMouseInput();
    }
	
	@SuppressWarnings("static-access")
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 10) { //Cancel Button
			Main.NET.sendToServer(new MessageGuildInfoToServer(guild, discriminators));
			mc.player.closeScreen();
			mc.getMinecraft().displayGuiScreen(new GuiInventory(mc.player));
		}
		if (button.id == 11) { //invite Button
			Main.NET.sendToServer(new MessageMemberInfoToServer(this.inviteText.getText(), guild.guildID, false));
			inviteList.refreshList();
		}
		if (button == guiProm && memberList.selectedElement >=0) {//Promote button
			guild.promoteMember(memberList.getSelectedMember().player);
			discriminators.put("members", true);
			memberList.refreshList();
		}
		if (button == guiDemo && memberList.selectedElement >=0) {//Demote Button
			guild.demoteMember(memberList.getSelectedMember().player);
			discriminators.put("members", true);
			memberList.refreshList();
		}
		if (button == guiKick) {//Kick button
			if (inviteListClicked && inviteList.selectedElement >=0) {//action when the active list is the active list (removes invites)
				guild.removeMember(inviteList.getSelectedMember().player);
				discriminators.put("members", true);
			}
			else if (!inviteListClicked && memberList.selectedElement >=0) {//action when the member list is the active list (removes members)
				guild.removeMember(memberList.getSelectedMember().player);
				discriminators.put("members", true);
			}
			memberList.refreshList();
			inviteList.refreshList();
		}
	}
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseX >= memberList.x && mouseX < memberList.x+memberList.width && mouseY >= memberList.y && mouseY < memberList.y+memberList.height) {
        	inviteListClicked = false;
        	if (permProDemo) guiProm.enabled = true;
        	if (permProDemo) guiDemo.enabled = true;
        }
        if (mouseX >= inviteList.x && mouseX < inviteList.x+inviteList.width && mouseY >= inviteList.y && mouseY < inviteList.y+inviteList.height) {
        	inviteListClicked = true;
        	guiProm.enabled = false;
        	guiDemo.enabled = false;
        }
        guiKick.displayString = inviteListClicked ? new TextComponentTranslation("gui.members.removeinvite").getFormattedText() : new TextComponentTranslation("gui.members.kickmember").getFormattedText();
        if (inviteListClicked) memberList.selectedElement = -1;
        if (!inviteListClicked) inviteList.selectedElement = -1;
        memberList.mouseClicked(mouseX, mouseY, mouseButton);
        inviteList.mouseClicked(mouseX, mouseY, mouseButton);
        inviteText.mouseClicked(mouseX, mouseY, mouseButton);
    }
	
	protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        memberList.mouseReleased(mouseX, mouseY, state);
        inviteList.mouseReleased(mouseX, mouseY, state);
    }
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (this.inviteText.isFocused()) this.inviteText.textboxKeyTyped(typedChar, keyCode);
    }
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, guild.guildName+" Guild Members", this.width / 2, 8, 16777215);
		inviteText.drawTextBox();
		memberList.drawScreen(mouseX, mouseY, partialTicks);
		inviteList.drawScreen(mouseX, mouseY, partialTicks);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}   
	
	public class GuiListGuildMembers extends GuiNewListExtended<GuiNewListExtended.IGuiNewListEntry>{
	    private final GuiGuildMemberManager guildManager;
	    public Guild guild;
	    public Map<UUID, String> mbrNames;
	    private boolean invites;
	    private final List<GuiListGuildMembersEntry> entries = Lists.<GuiListGuildMembersEntry>newArrayList();
	    /** Index to the currently selected world */
	    private int selectedIdx = -1;
		
		public GuiListGuildMembers(GuiGuildMemberManager guiGM, Minecraft mcIn, boolean invites, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.guildManager = guiGM;
			this.guild = guildManager.guild;
			this.mbrNames = guildManager.mbrNames;
			this.invites = invites;
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	entries.clear();
	        Map<UUID, Integer> members = guild.members;

	        for (Map.Entry<UUID, Integer> entry : members.entrySet()){
	            if (!invites && entry.getValue() != -1) this.entries.add(new GuiListGuildMembersEntry(this, entry.getKey(), entry.getValue(), guild.permLevels, mbrNames));
	            if (invites && entry.getValue() == -1) this.entries.add(new GuiListGuildMembersEntry(this, entry.getKey(), entry.getValue(), guild.permLevels, mbrNames));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListGuildMembersEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListGuildMembersEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListGuildMembersEntry implements GuiNewListExtended.IGuiNewListEntry{
		public final UUID player;
		private final String name; 
		public int permLvl;
		private Map<Integer, String> perms;
		private Minecraft client = Minecraft.getMinecraft();
	    private final GuiListGuildMembers containingListSel;
		
		public GuiListGuildMembersEntry (GuiListGuildMembers listSelectionIn, UUID player, int permLvl, Map<Integer, String> permLvls, Map<UUID, String> mbrNames) {
			containingListSel = listSelectionIn;
			this.player = player;
			this.permLvl = permLvl;
			this.perms = permLvls;		
			this.name = mbrNames.get(player);
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
			String rankFormat = "";
	        switch(permLvl) {
	        case 0: {
	        	rankFormat = TextFormatting.DARK_GREEN + perms.getOrDefault(0, new TextComponentTranslation("core.guild.rank0").getFormattedText());
	        	break;
	        }
	        case 1: {
	        	rankFormat = TextFormatting.DARK_PURPLE + perms.getOrDefault(1, new TextComponentTranslation("core.guild.rank1").getFormattedText());
	        	break;
	        }
	        case 2: {
	        	rankFormat = TextFormatting.BLUE + perms.getOrDefault(2, new TextComponentTranslation("core.guild.rank2").getFormattedText());
	        	break;
	        }
	        case 3: {
	        	rankFormat = perms.getOrDefault(3, new TextComponentTranslation("core.guild.rank3").getFormattedText());
	        	break;
	        }
	        case -1: {
	        	rankFormat = TextFormatting.DARK_RED + new TextComponentTranslation("core.guild.ranki").getFormattedText();
	        	break;
	        }
	        default:
	        break;
	        }
	        
	        this.client.fontRenderer.drawString(name+" :"+rankFormat, x+3, y , 16777215);
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
