package com.dicemc.marketplace.gui;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.gui.GuiGuildMemberManager.GuiListGuildMembers;
import com.dicemc.marketplace.gui.GuiGuildMemberManager.GuiListGuildMembersEntry;
import com.dicemc.marketplace.util.AdminGuiType;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextFormatting;

public class GuiAdmin extends GuiScreen{
	//Gui Structural variables
	private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	private AdminGuiType activeMenu = AdminGuiType.NONE;
	private GuiButton toggleAccount, toggleGuild, toggleMarket, exitButton;
	//Account Menu Objects;
	private GuiButton toggleAccountGuild, toggleAccountPlayer, accountSet, accountAdd;
	private GuiTextField balanceBox;
	private GuiListAccount guiListAccounts;
	private boolean isPlayerList = true;
	//Gui Data variables
	public static Map<Account, String> accountList;
	
	
	public static void syncAccounts(Map<Account, String> accountList) {GuiAdmin.accountList = accountList;}
	
	public static void syncGuildData() {}
	
	public static void syncGuildLand() {}
	
	public static void syncGuldMembers() {}
	
	public static void syncMarkets() {}
	
	public GuiAdmin() {
		accountList = new HashMap<Account, String>();
	}
	
	public void initGui() {
		toggleAccount = new GuiButton(1, 3, 3, 75, 20, "Accounts");
		toggleGuild = new GuiButton(2, 3, toggleAccount.y + 23, 75, 20, "Guild");
		toggleMarket = new GuiButton(3, 3, toggleGuild.y + 23, 75, 20, "Markets");
		exitButton = new GuiButton(4, 3, this.height - 30, 75, 20, "Exit");
		this.buttonList.add(toggleAccount);
		this.buttonList.add(toggleGuild);
		this.buttonList.add(toggleMarket);
		this.buttonList.add(exitButton);
		//account menu specific objects
		guiListAccounts = new GuiListAccount(this, mc, accountList, 83, 30, (this.width - 86)/2, this.height - 33, 10);
		toggleAccountPlayer = new GuiButton(10, guiListAccounts.x + guiListAccounts.width + 3, 3, guiListAccounts.width/2 - 2, 20, "Players");
		toggleAccountGuild = new GuiButton(11, toggleAccountPlayer.x + toggleAccountPlayer.width + 3, 3, guiListAccounts.width/2 - 2, 20, "Guilds");
		balanceBox = new GuiTextField(12, this.fontRenderer, guiListAccounts.x + guiListAccounts.width + 3, this.height/2, guiListAccounts.width - 6, 20);
		accountSet = new GuiButton(13, balanceBox.x, balanceBox.y+balanceBox.height+3, toggleAccountGuild.width, 20, "Set");
		accountAdd = new GuiButton(14, accountSet.x+ accountSet.width +3, balanceBox.y+balanceBox.height+3, toggleAccountGuild.width, 20, "Add");
		this.buttonList.add(toggleAccountPlayer);
		this.buttonList.add(toggleAccountGuild);
		this.buttonList.add(accountSet);
		this.buttonList.add(accountAdd);
		guiListAccounts.visible = false;
		toggleAccountPlayer.visible = false;
		toggleAccountGuild.visible = false;
		balanceBox.setVisible(false);
		accountSet.visible = false;
		accountAdd.visible = false;
		//Guild Main menu specific objects
		
		//guild Land menu specific objects
		
		//guild members menu specific objects
		
		//markets menu specific objects
	}
	
	public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (guiListAccounts.visible) {guiListAccounts.handleMouseInput();}
    }
	
	private void updateVisibility() {
		//Main menu buttons
		toggleAccount.enabled = activeMenu == AdminGuiType.ACCOUNT ? false : true;
		toggleGuild.enabled = activeMenu == AdminGuiType.GUILD_SELECT ? false : true;
		toggleMarket.enabled = activeMenu == AdminGuiType.MARKET ? false : true;
		//account objects
		guiListAccounts.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		toggleAccountPlayer.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		toggleAccountGuild.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		balanceBox.setVisible(activeMenu == AdminGuiType.ACCOUNT ? true :false);
		accountSet.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		accountAdd.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		toggleAccountPlayer.enabled = isPlayerList ? false : true;
		toggleAccountGuild.enabled = isPlayerList ? true : false;
	}
	
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == exitButton) mc.player.closeScreen();
		if (button == toggleAccount) {
			activeMenu = AdminGuiType.ACCOUNT;
			updateVisibility();
		}
		if (button == toggleGuild) {
			activeMenu = AdminGuiType.GUILD_SELECT;
			updateVisibility();
		}
		if (button == toggleMarket) {
			activeMenu = AdminGuiType.MARKET;
			updateVisibility();
		}
		if (button == toggleAccountPlayer) {
			isPlayerList = true;
			updateVisibility();
			//sent packet to sync lists
		}
		if (button == toggleAccountGuild) {
			isPlayerList = false;
			updateVisibility();
			//sent packet to sync lists
		}
	}
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (guiListAccounts.visible) {guiListAccounts.mouseClicked(mouseX, mouseY, mouseButton);}
		if (balanceBox.getVisible()) {balanceBox.mouseClicked(mouseX, mouseY, mouseButton);}
	}
	
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		if (guiListAccounts.visible) {guiListAccounts.mouseReleased(mouseX, mouseY, state);}
    }
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (balanceBox.getVisible() && CoreUtils.validNumberKey(keyCode)) balanceBox.textboxKeyTyped(typedChar, keyCode);
    }
    
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    	this.drawDefaultBackground();
    	switch (activeMenu) {
    	//draw only the objects for the specfic screen toggled
    	case ACCOUNT: {
    		this.drawString(this.fontRenderer, TextFormatting.GOLD+"Account Balance:", balanceBox.x, balanceBox.y - 22, 16777215);
    		double balance = guiListAccounts.selectedIdx >= 0 ? guiListAccounts.getSelectedMember().balance : 0;
    		this.drawString(this.fontRenderer, TextFormatting.GOLD+"$"+df.format(balance), balanceBox.x, balanceBox.y - 11, 16777215);
    		this.drawString(this.fontRenderer, "Account Owner List", guiListAccounts.x, guiListAccounts.y - 11, 16777215);
    		this.drawString(this.fontRenderer, TextFormatting.GRAY+"Negative numbers when clicking", balanceBox.x, balanceBox.y+46, 16777215);
    		this.drawString(this.fontRenderer, TextFormatting.GRAY+"'add' reduce the balance.", balanceBox.x, balanceBox.y+57, 16777215);
    		balanceBox.drawTextBox();
    		guiListAccounts.drawScreen(mouseX, mouseY, partialTicks);
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
		//default screen objects
    	super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    public class GuiListAccount extends GuiListExtendedMember{
	    private final GuiAdmin parentGui;
	    public Map<Account, String> accountList;
	    private final List<GuiListAccountEntry> entries = Lists.<GuiListAccountEntry>newArrayList();
	    /** Index to the currently selected world */
	    private int selectedIdx = -1;
		
		public GuiListAccount(GuiAdmin parentGui, Minecraft mcIn, Map<Account, String> accountList, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.parentGui = parentGui;
			this.accountList = accountList;
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	entries.clear();
	        accountList = parentGui.accountList;

	        for (Map.Entry<Account, String> entry : accountList.entrySet()){
	            this.entries.add(new GuiListAccountEntry(this, entry.getKey().owner, entry.getKey().balance, entry.getValue()));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListAccountEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListAccountEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListAccountEntry implements GuiListExtendedMember.IGuiNewListEntry{
		private final UUID owner;
		private final double balance;
		private final String name; 
		private Minecraft client = Minecraft.getMinecraft();
	    private final GuiListAccount containingListSel;
	    private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
		
		public GuiListAccountEntry (GuiListAccount listSelectionIn, UUID owner, double balance, String name) {
			containingListSel = listSelectionIn;
			this.owner = owner;
			this.balance = balance;
			this.name = name;
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
	        this.client.fontRenderer.drawString(name +" $"+ df.format(balance), x+3, y , 16777215);
		}

		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
	        this.containingListSel.selectMember(slotIndex);
	        this.containingListSel.showSelectionBox = true;
	        return false;
		}

		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}

	}
}
