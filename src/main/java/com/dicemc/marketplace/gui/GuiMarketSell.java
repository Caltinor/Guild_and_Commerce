package com.dicemc.marketplace.gui;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.dicemc.marketplace.Main;
import com.dicemc.marketplace.core.CoreUtils;
import com.dicemc.marketplace.events.GuiEventHandler;
import com.dicemc.marketplace.network.MessageGuiRequest;
import com.dicemc.marketplace.network.MessageMarketsToServer;
import com.dicemc.marketplace.util.MktPktType;
import com.dicemc.marketplace.util.Reference;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMarketSell extends GuiContainer{
	private final List<ItemStack> itemList;
	public GuiButton mktLocal, mktGlobal, mktAuction, requestToggle, postButton, exitButton, mktServer;
	public GuiButton stockAdd, stockSub, stock32, stock64;
	public GuiTextField priceInput;
	public GuiListItem itemGuiList;
	private boolean isRequest;
	private boolean widthTooNarrow;
	private boolean isItemFromList = false;
	private final boolean adminMode;

	public GuiMarketSell(EntityPlayer player, List<ItemStack> itemList, boolean adminMode) {
		super(new ContainerSell(player));
		this.itemList = itemList;
		this.adminMode = adminMode;
	}
	
	public void initGui()
    {
        super.initGui();
        this.widthTooNarrow = this.width < 379;
        itemGuiList = new GuiListItem(this, itemList, mc, this.guiLeft+this.xSize - 100, this.guiTop, 200, this.ySize, 18);
        //buttons rendered above the inventory box
        mktGlobal = new GuiButton(11, this.width/2 - 20, this.guiTop -25, 40, 20, new TextComponentTranslation("gui.market.global").getFormattedText());
        mktLocal = new GuiButton(10, mktGlobal.x - 43, this.guiTop -25, 40, 20, new TextComponentTranslation("gui.market.local").getFormattedText());
        mktAuction = new GuiButton(12, mktGlobal.x + mktGlobal.width + 3, this.guiTop -25, 40, 20, new TextComponentTranslation("gui.market.auction").getFormattedText());
        mktServer = new GuiButton(21, mktAuction.x+mktAuction.width+3, this.guiTop - 25, 40, 20, new TextComponentTranslation("gui.market.server").getFormattedText());
        exitButton = new GuiButton(20, this.width - 22, 2, 20, 20, "X");
        //other objects
        stockAdd = new GuiButton(14, this.guiLeft+ 21 - 100, this.guiTop+ 50, 15, 20, "+");
        stockSub = new GuiButton(15, stockAdd.x+16, stockAdd.y, 15, 20, "-");
        stock32 = new GuiButton(16, stockAdd.x - 16, stockAdd.y, 15, 20, "32");
        stock64 = new GuiButton(17, stockAdd.x + 32, stockAdd.y, 15, 20, "64");
        requestToggle = new GuiButton(13, this.guiLeft + this.xSize - 80, stockAdd.y, 50, 20,  new TextComponentTranslation("gui.giveitem").getFormattedText());
        priceInput = new GuiTextField(18, this.fontRenderer, this.guiLeft+50, this.guiTop+29, 75, 18);
        postButton = new GuiButton(19, this.guiLeft + (this.xSize/2) - 40, this.guiTop + 5, 80, 20,  new TextComponentTranslation("gui.sellitem").getFormattedText());
        this.buttonList.add(mktLocal);
        this.buttonList.add(mktGlobal);
        this.buttonList.add(mktAuction);
        this.buttonList.add(requestToggle);
        this.buttonList.add(stockAdd);
        this.buttonList.add(stockSub);
        this.buttonList.add(stock32);
        this.buttonList.add(stock64);
        this.buttonList.add(postButton);
        this.buttonList.add(exitButton);
        this.buttonList.add(mktServer);
        //launch disables
        mktServer.visible = adminMode ? true : false;
        isRequest = false;
        stockAdd.visible = false;
        stockSub.visible = false;
        stock32.visible = false;
        stock64.visible = false;
        mktLocal.enabled = false;
        itemGuiList.visible = false;
    }
	
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == mktLocal) {
			mktLocal.enabled = false;
			mktGlobal.enabled = true;
			mktAuction.enabled = true;
			mktServer.enabled = true;
		}
		if (button == mktGlobal) {
			mktLocal.enabled = true;
			mktGlobal.enabled = false;
			mktAuction.enabled = true;
			mktServer.enabled = true;
		}
		if (button == mktAuction && !isRequest) {
			mktLocal.enabled = true;
			mktGlobal.enabled = true;
			mktAuction.enabled = false;
			mktServer.enabled = true;
		}
		if (button == mktServer) {
			mktLocal.enabled = true;
			mktGlobal.enabled = true;
			mktAuction.enabled = true;
			mktServer.enabled = false;
		}
		if (button == requestToggle && this.inventorySlots.getSlot(0).inventory.isEmpty() && mktAuction.enabled) {
			isRequest = isRequest ? false : true;
			if (isRequest) {
				requestToggle.displayString = new TextComponentTranslation("gui.request").getFormattedText();
				postButton.displayString = new TextComponentTranslation("gui.requestitem").getFormattedText();
				itemGuiList.visible = true;
				this.guiLeft -= 100;
				requestToggle.x = this.guiLeft + this.xSize - 80;
				priceInput.x = this.guiLeft+50;
				postButton.x = this.guiLeft + (this.xSize/2) - 40;
				this.xSize += 200;
			}
			if (!isRequest) {
				stockAdd.visible = false;
		        stockSub.visible = false;
		        stock32.visible = false;
		        stock64.visible = false;
		        requestToggle.displayString = new TextComponentTranslation("gui.giveitem").getFormattedText();
		        postButton.displayString = new TextComponentTranslation("gui.sellitem").getFormattedText();
		        itemGuiList.visible = false;
		        this.xSize = 176;
		        this.guiLeft = (this.width - this.xSize) / 2;
				requestToggle.x = this.guiLeft + this.xSize - 80;
				priceInput.x = this.guiLeft+50;
				postButton.x = this.guiLeft + (this.xSize/2) - 40;
			}
		}
		if (button == stock64) {
			if (this.inventorySlots.getSlot(0).inventory.getStackInSlot(0).getMaxStackSize() >= 64) {
				this.inventorySlots.getSlot(0).inventory.getStackInSlot(0).setCount(64); }
		}
		if (button == stock32) {
			if (this.inventorySlots.getSlot(0).inventory.getStackInSlot(0).getMaxStackSize() >= 32) {
				this.inventorySlots.getSlot(0).inventory.getStackInSlot(0).setCount(32); }
		}
		if (button == stockAdd) {
			int count = this.inventorySlots.getSlot(0).inventory.getStackInSlot(0).getCount();
			if (count < this.inventorySlots.getSlot(0).inventory.getStackInSlot(0).getMaxStackSize()) {
				this.inventorySlots.getSlot(0).inventory.getStackInSlot(0).setCount(count +1); }
		}
		if (button == stockSub) {
			int count = this.inventorySlots.getSlot(0).inventory.getStackInSlot(0).getCount();
			if (count > 1) {
				this.inventorySlots.getSlot(0).inventory.getStackInSlot(0).setCount(count -1); }
		}
		if (button == postButton && priceInput.getText().length() > 0) {
			int marketType = 0;
			if (mktLocal.enabled == false) { marketType = 0;}
			else if (mktGlobal.enabled == false) { marketType = 1;}
			else if (mktAuction.enabled == false) { marketType = 2;}
			else if (mktServer.enabled == false) {marketType = 3;}
			ItemStack itemOut = this.inventorySlots.getSlot(0).getStack();
			this.inventorySlots.detectAndSendChanges();
			double sellP = -1D;
			try {sellP = Math.abs(Double.valueOf(priceInput.getText()));} catch (NumberFormatException e) {}
			UUID vendor = adminMode ? Reference.NIL : mc.player.getUniqueID();
			if (sellP != -1) Main.NET.sendToServer(new MessageMarketsToServer(MktPktType.SELL, marketType, vendor, itemOut, sellP, isRequest? false : true));
		}
		if (button == exitButton) {
			Main.NET.sendToServer(new MessageGuiRequest(adminMode ? 5: 4));}
	}
	
	public void updateScreen() {super.updateScreen();}
	
	public void handleMouseInput() {
		try {super.handleMouseInput();} catch (IOException e) {} 
		itemGuiList.handleMouseInput();
	}
	
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type){
        super.handleMouseClick(slotIn, slotId, mouseButton, type);
        itemGuiList.handleMouseInput();
    }
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		priceInput.mouseClicked(mouseX, mouseY, mouseButton);
		itemGuiList.mouseClicked(mouseX, mouseY, mouseButton);
		if (itemGuiList.selectedIdx >= 0 && this.inventorySlots.getSlot(0).inventory.isEmpty() && !isItemFromList) {
			this.inventorySlots.getSlot(0).inventory.setInventorySlotContents(0, itemGuiList.getSelectedMember().item.copy());
			isItemFromList = true;
			((ContainerSell) this.inventorySlots).updateInvSlotPosition(500);
			stockAdd.visible = true;
	        stockSub.visible = true;
	        stock32.visible = true;
	        stock64.visible = true;
		}
		else if (itemGuiList.selectedIdx >= 0 && isItemFromList) {
			Item inSlot = this.inventorySlots.getSlot(0).inventory.getStackInSlot(0).getItem();
			Item fromList = itemGuiList.getSelectedMember().item.getItem();
			if (!inSlot.equals(fromList)) {this.inventorySlots.getSlot(0).inventory.setInventorySlotContents(0, itemGuiList.getSelectedMember().item.copy());}
		}
		if (this.getSlotUnderMouse() != null) {
			if (isItemFromList && this.getSlotUnderMouse().equals(this.inventorySlots.getSlot(0))) {
				this.inventorySlots.getSlot(0).putStack(ItemStack.EMPTY);
				itemGuiList.selectedIdx = -1;
				isItemFromList = false;
				((ContainerSell) this.inventorySlots).updateInvSlotPosition(-500);
				stockAdd.visible = false;
		        stockSub.visible = false;
		        stock32.visible = false;
		        stock64.visible = false;
			}
		}		
    }
	
	protected void mouseReleased(int mouseX, int mouseY, int state)
    {
        super.mouseReleased(mouseX, mouseY, state);
        itemGuiList.mouseReleased(mouseX, mouseY, state);
    }
	
	protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
		super.keyTyped(typedChar, keyCode);
		if (this.priceInput.isFocused() && CoreUtils.validNumberKey(keyCode)) { this.priceInput.textboxKeyTyped(typedChar, keyCode);}
    }
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
        this.drawDefaultBackground();
        if (this.widthTooNarrow) this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        else super.drawScreen(mouseX, mouseY, partialTicks);
        priceInput.drawTextBox();
        itemGuiList.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
	
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GuiEventHandler.INVENTORY_ADDITIONS);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawTexturedModalRect(i, j, 0, 90, 176, 166);		
	}
	
	public void onGuiClosed()
    {
        super.onGuiClosed();
    }
	
	public class GuiListItem extends GuiNewListExtended<GuiNewListExtended.IGuiNewListEntry>{
	    public List<ItemStack> itemList;
	    private final List<GuiListItemEntry> entries = Lists.<GuiListItemEntry>newArrayList();
	    /** Index to the currently selected world */
	    private int selectedIdx = -1;
	    public int hoveredSlot = -1;
		
		public GuiListItem(GuiMarketSell guiGM, List<ItemStack> itemList, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
			this.itemList = itemList;
			this.refreshList();
		}
		
	    public void refreshList()
	    {
	    	entries.clear();
	        for (ItemStack entry : itemList) {
	            this.entries.add(new GuiListItemEntry(this, entry));
	        }
	    }
	    
	    public void selectMember(int idx) {
	    	this.selectedIdx = idx;
	    }
	    
	    @Nullable
	    public GuiListItemEntry getSelectedMember()
	    {
	        return this.selectedIdx >= 0 && this.selectedIdx < this.getSize() ? this.getListEntry(this.selectedIdx) : null;
	    }

		@Override
		public GuiListItemEntry getListEntry(int index) {
			return entries.get(index);
		}

		@Override
		protected int getSize() {
			return entries.size();
		}

	}
	
	public class GuiListItemEntry implements GuiNewListExtended.IGuiNewListEntry{
	    private final GuiListItem containingListSel;
	    private Minecraft client = Minecraft.getMinecraft();
	    private ItemStack item;
		
		public GuiListItemEntry (GuiListItem listIn, ItemStack item) {
			this.containingListSel = listIn;
			this.item = item;
		}
		
		public void updatePosition(int slotIndex, int x, int y, float partialTicks) {}

		public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
	        this.client.fontRenderer.drawString(item.getDisplayName(), x+30, y , 16777215);
	        this.client.fontRenderer.drawString("", x+30, y+10 , 16777215);
	        
	        RenderHelper.enableGUIStandardItemLighting();
	        FontRenderer font = item.getItem().getFontRenderer(item);
	        if (font == null) font = fontRenderer;
	        itemRender.renderItemAndEffectIntoGUI(item, x+3, y);
	        itemRender.renderItemOverlayIntoGUI(font, item, x+3, y, null);
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