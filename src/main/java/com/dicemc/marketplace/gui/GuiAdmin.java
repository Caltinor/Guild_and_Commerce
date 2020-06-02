package com.dicemc.marketplace.gui;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.Account;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.core.Guild;
import com.dicemc.marketplace.core.MarketItem;
import com.dicemc.marketplace.gui.GuiGuildMemberManager.GuiListGuildMembers;
import com.dicemc.marketplace.gui.GuiGuildMemberManager.GuiListGuildMembersEntry;
import com.dicemc.marketplace.gui.GuiMarketManager.GuiListMarket;
import com.dicemc.marketplace.gui.GuiMarketManager.GuiListMarketEntry;
import com.dicemc.marketplace.gui.GuiMarketManager.MarketListItem;
import com.dicemc.marketplace.item.ModItems;
import com.dicemc.marketplace.network.MessageAdminToServer;
import com.dicemc.marketplace.util.AdminGuiType;
import com.dicemc.marketplace.util.MktPktType;
import com.dicemc.marketplace.util.Reference;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

public class GuiAdmin extends GuiScreen{
	//Gui Structural variables
	private static DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	private static AdminGuiType activeMenu = AdminGuiType.NONE;
	private static GuiButton toggleAccount, toggleGuild, toggleMarket, exitButton;
	//Account Menu Objects;
	private static GuiButton toggleAccountGuild, toggleAccountPlayer, accountSet, accountAdd, accountRemove;
	private static GuiTextField balanceBox;
	private static GuiListAccount guiListAccounts;
	private static boolean isPlayerList = true;
	//guild select menu objects;
	private static GuiButton selectGuild;
	private static GuiListNameList guildList;
	//market menu objects
	private static GuiButton toggleLocal, toggleGlobal, toggleAuction, toggleServer;
	private static GuiButton editSave, toggleVendorGive, toggleInfinite, saleExpire, saleRemove;
	private static GuiTextField priceBox, stockBox;
	private static GuiListAdminMarket marketList;
	private static boolean isVendorGive = true;
	private static boolean isInfinite = false;
	private static int selectedMarket = 0;
	//Gui Data variables
	public static int slotIdx = -1;
	private static Map<Account, String> accountList;
	private static Map<UUID, String> nameList;
	private static List<MarketListItem> vendList;
	private static String vendorName, locName, bidderName;
	
	
	public static void syncAccounts(Map<Account, String> accountList) {GuiAdmin.accountList = accountList; guiListAccounts.refreshList();}
	
	public static void syncGuildList(Map<UUID, String> nameList) {GuiAdmin.nameList = nameList; guildList.refreshList();}
	
	public static void syncGuildData() {}
	
	public static void syncGuildLand() {}
	
	public static void syncGuldMembers() {}
	
	public static void syncMarkets(List<MarketListItem> list) {GuiAdmin.vendList = list; marketList.listType = selectedMarket; marketList.refreshList();}
	
	public static void syncMarketDetail(String vendorName, String locName, String bidderName) {
		GuiAdmin.vendorName = vendorName;
		GuiAdmin.locName = locName;
		GuiAdmin.bidderName = bidderName;
		GuiAdmin.updateVisibility();
	}
	
	public GuiAdmin() {
		accountList = new HashMap<Account, String>();
		nameList = new HashMap<UUID, String>();
		vendList = new ArrayList<MarketListItem>();
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
		accountRemove = new GuiButton(15, this.width - (this.width/5) - 38, this.height - 30, 120, 20, "Remove Account");
		this.buttonList.add(toggleAccountPlayer);
		this.buttonList.add(toggleAccountGuild);
		this.buttonList.add(accountSet);
		this.buttonList.add(accountAdd);
		this.buttonList.add(accountRemove);
		guiListAccounts.visible = false;
		toggleAccountPlayer.visible = false;
		toggleAccountGuild.visible = false;
		balanceBox.setVisible(false);
		accountSet.visible = false;
		accountAdd.visible = false;
		accountRemove.visible = false;
		//Guild Select menu specific objects
		selectGuild = new GuiButton(20, (this.width - 80)/2 + 42, this.height - 30, 75, 20, "Select Guild");
		guildList = new GuiListNameList(this, mc, nameList, 83, 30, this.width - 90, this.height - 65, 10);
		this.buttonList.add(selectGuild);
		selectGuild.visible = false;
		guildList.visible = false;
		//Guild Main menu specific objects
		
		//guild Land menu specific objects
		
		//guild members menu specific objects
		
		//markets menu specific objects
		marketList = new GuiListAdminMarket(this, vendList, Reference.NIL, 0, mc, 83, 40, (this.width-80)/2, this.height-45, 25);
		toggleLocal = new GuiButton(60, 83, 5, 70, 20, "Local");
		toggleGlobal = new GuiButton(61, toggleLocal.x + toggleLocal.width, 5, toggleLocal.width, 20, "Global");
		toggleAuction = new GuiButton(62, toggleGlobal.x + toggleLocal.width, 5, toggleLocal.width, 20, "Auction");
		toggleServer = new GuiButton(63, toggleAuction.x + toggleLocal.width, 5, toggleLocal.width, 20, "Server");
		editSave = new GuiButton(65, marketList.x + marketList.width + 3, this.height - 30, 75, 20, "Save Changes");
		saleRemove = new GuiButton(66, editSave.x + editSave.width + 2, editSave.y, editSave.width, 20, "Remove");
		saleExpire = new GuiButton(67, saleRemove.x, saleRemove.y - 21, editSave.width, 20, "Expire");
		priceBox = new GuiTextField(601, this.fontRenderer, marketList.x + marketList.width + 3, marketList.y + 22, 75, 20);
		stockBox = new GuiTextField(602, this.fontRenderer, marketList.x + marketList.width + 3, marketList.y + 55, 75, 20);
		toggleInfinite = new GuiButton(68, stockBox.x+stockBox.width+2, stockBox.y, 70, 20, "");
		toggleVendorGive = new GuiButton(69, priceBox.x+priceBox.width+2, priceBox.y, 70, 20, "");	
		this.buttonList.add(toggleLocal);
		this.buttonList.add(toggleGlobal);
		this.buttonList.add(toggleAuction);
		this.buttonList.add(toggleServer);
		this.buttonList.add(editSave);
		this.buttonList.add(toggleVendorGive);
		this.buttonList.add(toggleInfinite);
		this.buttonList.add(saleExpire);
		this.buttonList.add(saleRemove);
		marketList.visible = false;
		toggleLocal.visible = false;
		toggleGlobal.visible = false;
		toggleAuction.visible = false;
		toggleServer.visible = false;
		editSave.visible = false;
		toggleVendorGive.visible = false;
		toggleInfinite.visible = false;
		saleExpire.visible = false;
		saleRemove.visible = false;
		priceBox.setVisible(false);
		stockBox.setVisible(false);
		//market Sell specific objects
	}
	
	public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (guiListAccounts.visible) {guiListAccounts.handleMouseInput();}
        if (guildList.visible) {guildList.handleMouseInput();}
        if (marketList.visible) {marketList.handleMouseInput();}
    }
	
	private static void updateVisibility() {
		//Main menu buttons
		toggleAccount.enabled = activeMenu == AdminGuiType.ACCOUNT ? false : true;
		toggleGuild.enabled = activeMenu == AdminGuiType.GUILD_SELECT ? false : true;
		toggleMarket.enabled = activeMenu == AdminGuiType.MARKET ? false : true;
		//account objects
		guiListAccounts.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		toggleAccountPlayer.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		toggleAccountGuild.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		balanceBox.setVisible(activeMenu == AdminGuiType.ACCOUNT);
		accountSet.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		accountAdd.visible = activeMenu == AdminGuiType.ACCOUNT ? true :false;
		accountRemove.visible = activeMenu == AdminGuiType.ACCOUNT ? true: false;
		toggleAccountPlayer.enabled = isPlayerList ? false : true;
		toggleAccountGuild.enabled = isPlayerList ? true : false;
		//guild select objects
		selectGuild.visible = activeMenu == AdminGuiType.GUILD_SELECT ? true : false;
		guildList.visible = activeMenu == AdminGuiType.GUILD_SELECT ? true : false;
		//guild main objects
		//guild land objects
		//guild member objects
		//market objects
		marketList.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		toggleLocal.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		toggleGlobal.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		toggleAuction.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		toggleServer.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		editSave.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		toggleVendorGive.visible = (activeMenu == AdminGuiType.MARKET && selectedMarket != 2) ? true : false;
		isVendorGive = (marketList.selectedIdx >= 0 ? (marketList.getSelectedMember().posting.item.vendorGiveItem ? true : false) : false);
		toggleVendorGive.displayString = isVendorGive ? "Giving" : "Requesting";
		toggleInfinite.visible = (activeMenu == AdminGuiType.MARKET && selectedMarket != 2) ? true : false;
		isInfinite = (marketList.selectedIdx >= 0 ? (marketList.getSelectedMember().posting.item.infinite ? true : false) : false);
		toggleInfinite.displayString = isInfinite ? "Infinite" : "Remaining";
		saleExpire.visible = (activeMenu == AdminGuiType.MARKET && selectedMarket == 2) ? true : false;
		saleRemove.visible = activeMenu == AdminGuiType.MARKET ? true : false;
		toggleLocal.enabled = selectedMarket == 0 ? false : true;
		toggleGlobal.enabled = selectedMarket == 1 ? false : true;
		toggleAuction.enabled = selectedMarket == 2 ? false : true;
		toggleServer.enabled = selectedMarket == 3 ? false : true;
		priceBox.setVisible(activeMenu == AdminGuiType.MARKET);
		priceBox.setText(marketList.selectedIdx >= 0 ? df.format(marketList.getSelectedMember().posting.item.price) : "0");
		stockBox.setVisible(activeMenu == AdminGuiType.MARKET && selectedMarket != 2);
		stockBox.setEnabled(marketList.selectedIdx >= 0 ? !marketList.getSelectedMember().posting.item.infinite : false);
		stockBox.setText(marketList.selectedIdx >= 0 ? String.valueOf(marketList.getSelectedMember().posting.item.vendStock) : "0");
		//market sell objects
	}
	
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == exitButton) mc.player.closeScreen();
		//Main menu buttons
		if (button == toggleAccount) {
			activeMenu = AdminGuiType.ACCOUNT;
			isPlayerList = true;
			Main.NET.sendToServer(new MessageAdminToServer(false));
			updateVisibility();
		}
		if (button == toggleGuild) {
			activeMenu = AdminGuiType.GUILD_SELECT;
			Main.NET.sendToServer(new MessageAdminToServer(0));
			updateVisibility();
		}
		if (button == toggleMarket) {
			activeMenu = AdminGuiType.MARKET;
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.LOCAL));
			updateVisibility();
		}
		//Account Menu Buttons
		if (button == toggleAccountPlayer) {
			isPlayerList = true;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(false));
		}
		if (button == toggleAccountGuild) {
			isPlayerList = false;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(true));
		}
		if (button == accountSet && guiListAccounts.selectedIdx >= 0) {
			double amount = 0D;
			try {amount = Double.valueOf(balanceBox.getText());} catch (NumberFormatException e) {}
			Main.NET.sendToServer(new MessageAdminToServer(!isPlayerList, guiListAccounts.getSelectedMember().owner, amount));
		}
		if (button == accountAdd  && guiListAccounts.selectedIdx >= 0) {
			double amount = guiListAccounts.getSelectedMember().balance;
			try {amount += Double.valueOf(balanceBox.getText());} catch (NumberFormatException e) {}
			Main.NET.sendToServer(new MessageAdminToServer(!isPlayerList, guiListAccounts.getSelectedMember().owner, amount));
		}
		if (button == accountRemove  && guiListAccounts.selectedIdx >= 0) {
			Main.NET.sendToServer(new MessageAdminToServer(!isPlayerList, guiListAccounts.getSelectedMember().owner));
			guiListAccounts.selectedIdx = -1;
			guiListAccounts.selectedElement = -1;
		}
		//Market menu buttons
		if (button == toggleLocal) {
			selectedMarket = 0;
			marketList.selectedIdx = -1;
			marketList.selectedElement = -1;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.LOCAL));		
		}
		if (button == toggleGlobal) {
			selectedMarket = 1;
			marketList.selectedIdx = -1;
			marketList.selectedElement = -1;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.GLOBAL));
		}
		if (button == toggleAuction) {
			selectedMarket = 2;
			marketList.selectedIdx = -1;
			marketList.selectedElement = -1;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.AUCTION));
		}
		if (button == toggleServer) {
			selectedMarket = 3;
			marketList.selectedIdx = -1;
			marketList.selectedElement = -1;
			updateVisibility();
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.SERVER));
		}
		if (button == toggleVendorGive) {
			isVendorGive = isVendorGive ? false: true;
			toggleVendorGive.displayString = isVendorGive ? "Giving" : "Requesting";
		}
		if (button == toggleInfinite) {
			isInfinite = isInfinite ? false : true;
			toggleInfinite.displayString = isInfinite ? "Infinite" : "Remaining";
			stockBox.setEnabled(isInfinite ? false : true);
		}
		if (button == editSave && marketList.selectedIdx >= 0) {			
			MktPktType type = GuiAdmin.selectedMarket == 0 ? MktPktType.LOCAL : (GuiAdmin.selectedMarket == 1 ? MktPktType.GLOBAL : (GuiAdmin.selectedMarket == 2 ? MktPktType.AUCTION : MktPktType.SERVER));
			double amount = -1D;
			try {Math.abs(amount = Double.valueOf(priceBox.getText()));} catch (NumberFormatException e) {}
			int stock = 0;
			try {Math.abs(stock = Integer.valueOf(stockBox.getText()));} catch (NumberFormatException e) {}
			Main.NET.sendToServer(new MessageAdminToServer(type, marketList.getSelectedMember().posting.key, amount, isVendorGive, stock, isInfinite, marketList.getSelectedMember().posting.item.bidEnd));
		}
		if (button == saleExpire && marketList.selectedIdx >= 0) {
			Main.NET.sendToServer(new MessageAdminToServer(MktPktType.AUCTION, marketList.getSelectedMember().posting.key, true));
			marketList.selectedIdx = -1;
			marketList.selectedElement = -1;			
		}
		if (button == saleRemove && marketList.selectedIdx >= 0) {
			MktPktType type = GuiAdmin.selectedMarket == 0 ? MktPktType.LOCAL : (GuiAdmin.selectedMarket == 1 ? MktPktType.GLOBAL : (GuiAdmin.selectedMarket == 2 ? MktPktType.AUCTION : MktPktType.SERVER));
			Main.NET.sendToServer(new MessageAdminToServer(type, marketList.getSelectedMember().posting.key, false));
			marketList.selectedIdx = -1;
			marketList.selectedElement = -1;
		}
	}
	
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (guiListAccounts.visible) {guiListAccounts.mouseClicked(mouseX, mouseY, mouseButton);}
		if (balanceBox.getVisible()) {balanceBox.mouseClicked(mouseX, mouseY, mouseButton);}
		if (priceBox.getVisible()) {priceBox.mouseClicked(mouseX, mouseY, mouseButton);}
		if (stockBox.getVisible()) {stockBox.mouseClicked(mouseX, mouseY, mouseButton);}
		if (guildList.visible) {guildList.mouseClicked(mouseX, mouseY, mouseButton);}
		if (marketList.visible) {marketList.mouseClicked(mouseX, mouseY, mouseButton);}
	}
	
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		if (guiListAccounts.visible) {guiListAccounts.mouseReleased(mouseX, mouseY, state);}
		if (guildList.visible) {guildList.mouseReleased(mouseX, mouseY, state);}
		if (marketList.visible) {marketList.mouseReleased(mouseX, mouseY, state);}
		if (marketList.selectedIdx >= 0 && mouseX > marketList.x && mouseX < marketList.x+marketList.width-6 && mouseY > marketList.y && mouseY < marketList.height+marketList.y) {
			MktPktType type = GuiAdmin.selectedMarket == 0 ? MktPktType.LOCAL : (GuiAdmin.selectedMarket == 1 ? MktPktType.GLOBAL : (GuiAdmin.selectedMarket == 2 ? MktPktType.AUCTION : MktPktType.SERVER));
			Main.NET.sendToServer(new MessageAdminToServer(type, marketList.getSelectedMember().posting.key));
		}
    }
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		super.keyTyped(typedChar, keyCode);
		if (balanceBox.getVisible() && CoreUtils.validNumberKey(keyCode)) balanceBox.textboxKeyTyped(typedChar, keyCode);
		if (priceBox.getVisible() && CoreUtils.validNumberKey(keyCode)) priceBox.textboxKeyTyped(typedChar, keyCode);
		if (stockBox.getVisible() && CoreUtils.validNumberKey(keyCode)) stockBox.textboxKeyTyped(typedChar, keyCode);
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
    		this.drawString(this.fontRenderer, TextFormatting.GREEN+"GUILD LIST", selectGuild.x, 15, 16777215);
    		guildList.drawScreen(mouseX, mouseY, partialTicks);
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
    		this.drawString(this.fontRenderer, "Market List", marketList.x, marketList.y - 11, 16777215);
    		int baseX = marketList.x+marketList.width+2;
    		int baseY = marketList.y;
    		this.drawString(this.fontRenderer, "Vendor: "+vendorName, baseX, baseY, 16777215);
    		this.drawString(this.fontRenderer, TextFormatting.GOLD+"Price", baseX, baseY+11, 16777215);
    		if (selectedMarket != 2) this.drawString(this.fontRenderer, TextFormatting.LIGHT_PURPLE+"Supply Remaining", baseX, baseY + 44, 16777215);
    		String line1 = selectedMarket == 2 ? "Highest Bidder: " : "Locality:";
    		String line2 = selectedMarket == 2 ? bidderName : locName;
    		long end = marketList.selectedIdx >= 0 ? marketList.getSelectedMember().posting.item.bidEnd : 0;
    		String line3 = selectedMarket == 2 ? TextFormatting.AQUA+"Bidding Ends: " : "";
    		String line4 = selectedMarket == 2 ? String.valueOf(new Timestamp(end)) : "";
    		this.drawString(this.fontRenderer, line1, baseX, stockBox.y+stockBox.height+10, 16777215);
    		this.drawString(this.fontRenderer, line2, baseX, stockBox.y+stockBox.height+21, 16777215);
    		this.drawString(this.fontRenderer, line3, baseX, stockBox.y+stockBox.height+32, 16777215);
    		this.drawString(this.fontRenderer, line4, baseX, stockBox.y+stockBox.height+43, 16777215);
    		marketList.drawScreen(mouseX, mouseY, partialTicks);
    		priceBox.drawTextBox();
    		stockBox.drawTextBox();
    		//render tooltips on marketList
	        if (slotIdx >=0) {
            	if (mouseX > marketList.x && mouseX < marketList.x+20 && mouseY > marketList.getListEntry(slotIdx).getY() && mouseY < marketList.getListEntry(slotIdx).getY()+20) {
    	        	RenderHelper.enableGUIStandardItemLighting();
    	        	renderToolTip(marketList.getListEntry(slotIdx).posting.item.item, mouseX, mouseY);
    	        	RenderHelper.disableStandardItemLighting();
            	}
            	else {slotIdx = -1;} 
            }
    		break;
    	}
    	case MARKET_SELL: {
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
	        this.client.fontRenderer.drawString(name +TextFormatting.GOLD+" $"+ df.format(balance), x+3, y , 16777215);
		}

		public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
	        this.containingListSel.selectMember(slotIndex);
	        this.containingListSel.showSelectionBox = true;
	        return false;
		}

		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}

	}

	public class GuiListNameList extends GuiListExtendedMember{
	    private final GuiAdmin parentGui;
	    public Map<UUID, String> nameList;
	    private final List<GuiListNameListEntry> entries = Lists.<GuiListNameListEntry>newArrayList();
	    /** Index to the currently selected world */
	    private int selectedIdx = -1;
		
		public GuiListNameList(GuiAdmin parentGui, Minecraft mcIn, Map<UUID, String> nameList, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.parentGui = parentGui;
			this.nameList = nameList;
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	entries.clear();
	        nameList = parentGui.nameList;

	        for (Map.Entry<UUID, String> entry : nameList.entrySet()){
	            this.entries.add(new GuiListNameListEntry(this, entry.getKey(), entry.getValue()));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListNameListEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListNameListEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListNameListEntry implements GuiListExtendedMember.IGuiNewListEntry{
		private final UUID entityID;
		private final String name; 
		private Minecraft client = Minecraft.getMinecraft();
	    private final GuiListNameList containingListSel;
		
		public GuiListNameListEntry (GuiListNameList listSelectionIn, UUID entityID, String name) {
			containingListSel = listSelectionIn;
			this.entityID = entityID;
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

		public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}

	}

	public class GuiListAdminMarket extends GuiListExtendedMember{
	    private final GuiAdmin parentGui;
	    public List<MarketListItem> vendList;
	    public UUID locality;
	    private final List<GuiListAdminMarketEntry> entries = Lists.<GuiListAdminMarketEntry>newArrayList();
	    private int selectedIdx = -1;
	    public int hoveredSlot = -1;
	    public int listType = -1;
		
		public GuiListAdminMarket(GuiAdmin parentGui, List<MarketListItem> vendList, UUID locality, int listType, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.parentGui = parentGui;
			this.vendList = vendList;
			this.locality = locality;
			this.listType = listType;
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	entries.clear();
	    	vendList = GuiAdmin.vendList;
	        for (MarketListItem entry : vendList) {
	            this.entries.add(new GuiListAdminMarketEntry(this, entry, locality));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListAdminMarketEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListAdminMarketEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListAdminMarketEntry implements GuiListExtendedMember.IGuiNewListEntry{
	    private final GuiListAdminMarket containingListSel;
	    private Minecraft client = Minecraft.getMinecraft();
	    MarketListItem posting;
	    private String line1, line2;
	    private DecimalFormat df = new DecimalFormat("###,###,###,##0.00");
	    private int slotY;
	    
	    public int getY() {return slotY;}
		
		public GuiListAdminMarketEntry (GuiListAdminMarket listIn, MarketListItem posting, UUID locality) {
			this.containingListSel = listIn;
			this.posting = posting;
			if (containingListSel.listType != 2) {
				line1 = TextFormatting.GOLD+"$"+df.format(posting.item.price);				
				line2 = TextFormatting.LIGHT_PURPLE+(posting.item.infinite ? "INFINITE" : "Supply="+String.valueOf(posting.item.vendStock));	
				line2 += (posting.item.vendorGiveItem) ? TextFormatting.GREEN+" (Giving)": TextFormatting.BLUE+" (Requesting)";
			}
			if (containingListSel.listType == 2) {
				line1 = TextFormatting.GOLD+"$" + df.format(posting.item.price);
				line1 += (posting.item.highestBidder.equals(Reference.NIL)) ? TextFormatting.WHITE+" [No Bids]" : TextFormatting.RED+" [Active]";
				line2 = (posting.item.bidEnd < 3600000) ? TextFormatting.RED+"" : (posting.item.bidEnd < 86400000) ? TextFormatting.YELLOW+"" : TextFormatting.GREEN+"";
				line2 += String.valueOf(new Timestamp(posting.item.bidEnd));
			}
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
	        this.client.fontRenderer.drawString(line1, x+30, y+3, 16777215);
	        this.client.fontRenderer.drawString(line2, x+30, y+13 , 16777215);
	        RenderHelper.enableGUIStandardItemLighting();
	        FontRenderer font = posting.item.item.getItem().getFontRenderer(posting.item.item);
	        if (font == null) font = fontRenderer;
	        itemRender.renderItemAndEffectIntoGUI(posting.item.item, x+3, y);
	        itemRender.renderItemOverlayIntoGUI(font, posting.item.item, x+3, y, null);
	        if (mouseX > x+3 && mouseX < x+23 && mouseY > y && mouseY < y+20) {GuiAdmin.slotIdx = slotIndex; slotY = y;}
	        RenderHelper.disableStandardItemLighting();	        
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
