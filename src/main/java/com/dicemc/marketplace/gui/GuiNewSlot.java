package com.dicemc.marketplace.gui;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class GuiNewSlot {
    protected final Minecraft mc;
    public int x;
    public int y;
    public int width;
    public int height;
    public int listWidth;
    /** The height of a slot. */
    public final int slotHeight;
    /** The buttonID of the button used to scroll up */
    private int scrollUpButtonID;
    /** The buttonID of the button used to scroll down */
    private int scrollDownButtonID;
    protected int mouseX;
    protected int mouseY;
    protected boolean centerListVertically = true;
    /** Where the mouse was in the window when you first clicked to scroll */
    protected int initialClickY = -2;
    /**
     * What to multiply the amount you moved your mouse by (used for slowing down scrolling when over the items and not
     * on the scroll bar)
     */
    protected float scrollMultiplier;
    /** How far down this slot has been scrolled */
    protected float amountScrolled;
    /** The element in the list that was selected */
    protected int selectedElement = -1;
    /** The time when this button was last clicked. */
    protected long lastClicked;
    protected boolean visible = true;
    /** Set to true if a selected element in this gui will show an outline box */
    protected boolean showSelectionBox = true;
    private boolean enabled = true;

    public GuiNewSlot(Minecraft mcIn, int x, int y, int width, int height, int slotHeightIn)
    {
        this.mc = mcIn;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.slotHeight = slotHeightIn;
        
    }

    public void setDimensions(int x, int y, int width, int height)
    {
        this.width = width;
        this.height = height;
        this.y = y;
        this.x = x;
    }

    public void setShowSelectionBox(boolean showSelectionBoxIn)
    {
        this.showSelectionBox = showSelectionBoxIn;
    }

    protected abstract int getSize();

    /**
     * The element in the slot that was clicked, boolean for whether it was double clicked or not
     */
    protected abstract void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY);

    /**
     * Returns true if the element passed in is currently selected
     */
    protected abstract boolean isSelected(int slotIndex);

    /**
     * Return the height of the content being scrolled
     */
    protected int getContentHeight()
    {
        return this.getSize() * this.slotHeight;
    }

    protected void updateItemPos(int entryID, int insideLeft, int yPos, float partialTicks)
    {
    }

    protected abstract void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn, float partialTicks);

    protected void clickedHeader(int p_148132_1_, int p_148132_2_)
    {
    }

    protected void renderDecorations(int mouseXIn, int mouseYIn)
    {
    }

    public int getSlotIndexFromScreenCoords(int posX, int posY)
    {
        int k = posY - this.y + (int)this.amountScrolled - 4;
        int l = k / this.slotHeight;
        return posX < this.getScrollBarX() && posX >= this.x 
        		&& l >= 0 && k >= 0 && l < this.getSize() ? l : -1;
    }

    /**
     * Registers the IDs that can be used for the scrollbar's up/down buttons.
     */
    public void registerScrollButtons(int scrollUpButtonIDIn, int scrollDownButtonIDIn)
    {
        this.scrollUpButtonID = scrollUpButtonIDIn;
        this.scrollDownButtonID = scrollDownButtonIDIn;
    }

    /**
     * Stop the thing from scrolling out of bounds
     */
    protected void bindAmountScrolled()
    {
        this.amountScrolled = MathHelper.clamp(this.amountScrolled, 0.0F, (float)this.getMaxScroll());
    }

    public int getMaxScroll()
    {
        return Math.max(0, this.getContentHeight());
    }

    /**
     * Returns the amountScrolled field as an integer.
     */
    public int getAmountScrolled()
    {
        return (int)this.amountScrolled;
    }

    public boolean isMouseYWithinSlotBounds(int p_148141_1_)
    {
        return p_148141_1_ >= this.y && p_148141_1_ <= this.y+this.height && this.mouseX >= this.x && this.mouseX <= this.x+this.width;
    }

    /**
     * Scrolls the slot by the given amount. A positive value scrolls down, and a negative value scrolls up.
     */
    public void scrollBy(int amount)
    {
        this.amountScrolled += (float)amount;
        this.bindAmountScrolled();
        this.initialClickY = -2;
    }

    public void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == this.scrollUpButtonID)
            {
                this.amountScrolled -= (float)(this.slotHeight * 2 / 3);
                this.initialClickY = -2;
                this.bindAmountScrolled();
            }
            else if (button.id == this.scrollDownButtonID)
            {
                this.amountScrolled += (float)(this.slotHeight * 2 / 3);
                this.initialClickY = -2;
                this.bindAmountScrolled();
            }
        }
    }

    public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks)
    {
        if (this.visible)
        {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;
            int scrollX = this.getScrollBarX();
            int scrollR = scrollX + 6;
            this.bindAmountScrolled();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            int l = this.y + 4 - (int)this.amountScrolled;
            this.drawSelectionBox(l, mouseXIn, mouseYIn, partialTicks);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(7425);
            GlStateManager.disableTexture2D();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double)this.x, (double)(this.y + 4), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
            bufferbuilder.pos((double)this.x+this.width, (double)(this.y + 4), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
            bufferbuilder.pos((double)this.x+this.width, (double)this.y, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos((double)this.x, (double)this.y, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            bufferbuilder.pos((double)this.x, (double)this.y+this.height, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos((double)this.x+this.width, (double)this.y+this.height, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.pos((double)this.x+this.width, (double)(this.y+this.height - 4), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            bufferbuilder.pos((double)this.x, (double)(this.y+this.height - 4), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
            tessellator.draw();
            int j1 = this.getMaxScroll();

            if (j1 > 0)
            {
                int k1 = (this.height) / this.getContentHeight();
                k1 = MathHelper.clamp(k1, 32, this.height);
                int l1 = (int)this.amountScrolled * (this.height - k1) / j1 + this.y;

                if (l1 < this.y)
                {
                    l1 = this.y;
                }

                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)scrollX, (double)this.y+this.height, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)scrollR, (double)this.y+this.height, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)scrollR, (double)this.y, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)scrollX, (double)this.y, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)scrollX, (double)(l1 + k1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)scrollR, (double)(l1 + k1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)scrollR, (double)l1, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)scrollX, (double)l1, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                tessellator.draw();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)scrollX, (double)(l1 + k1 - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)(scrollR - 1), (double)(l1 + k1 - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)(scrollR - 1), (double)l1, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                bufferbuilder.pos((double)scrollX, (double)l1, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
                tessellator.draw();
            }

            this.renderDecorations(mouseXIn, mouseYIn);
            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(7424);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }

    public void handleMouseInput()
    {
        if (this.isMouseYWithinSlotBounds(this.mouseY))
        {
            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.mouseY >= this.y && this.mouseY <= this.y+this.height)
            {
                int i = (this.width - this.getListWidth()) / 2;
                int j = (this.width + this.getListWidth()) / 2;
                int k = this.mouseY - this.y + (int)this.amountScrolled - 4;
                int l = k / this.slotHeight;

                if (l < this.getSize() && this.mouseX >= i && this.mouseX <= j && l >= 0 && k >= 0)
                {
                    this.elementClicked(l, false, this.mouseX, this.mouseY);
                    this.selectedElement = l;
                }
                else if (this.mouseX >= i && this.mouseX <= j && k < 0)
                {
                    this.clickedHeader(this.mouseX - i, this.mouseY - this.y + (int)this.amountScrolled - 4);
                }
            }

            if (Mouse.isButtonDown(0) && this.getEnabled())
            {
                if (this.initialClickY == -1)
                {
                    boolean flag1 = true;

                    if (this.mouseY >= this.y && this.mouseY <= this.y+this.height)
                    {
                        int j2 = this.x;
                        int k2 = this.x + this.width;
                        int l2 = this.mouseY - this.y + (int)this.amountScrolled - 4;
                        int slotID = l2 / this.slotHeight;

                        if (slotID < this.getSize() && this.mouseX >= j2 && this.mouseX <= k2 && slotID >= 0 && l2 >= 0)
                        {
                            boolean flag = slotID == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L;
                            this.elementClicked(slotID, flag, this.mouseX, this.mouseY);
                            this.selectedElement = slotID;
                            this.lastClicked = Minecraft.getSystemTime();
                        }
                        else if (this.mouseX >= j2 && this.mouseX <= k2 && l2 < 0)
                        {
                            this.clickedHeader(this.mouseX - j2, this.mouseY - this.y + (int)this.amountScrolled - 4);
                            flag1 = false;
                        }

                        int i3 = this.getScrollBarX();
                        int j1 = i3 + 6;

                        if (this.mouseX >= i3 && this.mouseX <= j1)
                        {
                            this.scrollMultiplier = -1.0F;
                            int k1 = this.getMaxScroll();

                            if (k1 < 1)
                            {
                                k1 = 1;
                            }

                            int l1 = (int)((float)(this.height) / (float)this.getContentHeight());
                            l1 = MathHelper.clamp(l1, 32, this.height - 8);
                            this.scrollMultiplier /= (float)(this.height - l1) / (float)k1;
                        }
                        else
                        {
                            this.scrollMultiplier = 1.0F;
                        }

                        if (flag1)
                        {
                            this.initialClickY = this.mouseY;
                        }
                        else
                        {
                            this.initialClickY = -2;
                        }
                    }
                    else
                    {
                        this.initialClickY = -2;
                    }
                }
                else if (this.initialClickY >= 0)
                {
                    this.amountScrolled -= (float)(this.mouseY - this.initialClickY) * this.scrollMultiplier;
                    this.initialClickY = this.mouseY;
                }
            }
            else
            {
                this.initialClickY = -1;
            }

            int i2 = Mouse.getEventDWheel();

            if (i2 != 0)
            {
                if (i2 > 0)
                {
                    i2 = -1;
                }
                else if (i2 < 0)
                {
                    i2 = 1;
                }

                this.amountScrolled += (float)(i2 * this.slotHeight / 2);
            }
        }
    }

    public void setEnabled(boolean enabledIn)
    {
        this.enabled = enabledIn;
    }

    public boolean getEnabled()
    {
        return this.enabled;
    }

    /**
     * Gets the width of the list
     */
    public int getListWidth() {return listWidth;}

    /**
     * Draws the selection box around the selected slot element.
     */
    protected void drawSelectionBox(int topIn, int mouseXIn, int mouseYIn, float partialTicks)
    {
        int i = this.getSize();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        for (int j = 0; j < i; ++j)
        {
            int k = j * this.slotHeight + (topIn - this.y - 4);
            int l = this.slotHeight - 4;

            if (k > this.y+this.height || k + l < this.y)
            {
                this.updateItemPos(j, 0, k, partialTicks);
            }

            if (this.showSelectionBox && this.isSelected(j) && k >= -4 && k + this.slotHeight+4 <= this.height)
            {
                int i1 = this.x ;
                int j1 = this.x + this.width;
                int y1 = this.y+4;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture2D();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                bufferbuilder.pos((double)i1, (double)(k + l + 2 + y1), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j1, (double)(k + l + 2 + y1), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)j1, (double)(k - 2 + y1), 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)i1, (double)(k - 2 + y1), 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                bufferbuilder.pos((double)(i1 + 1), (double)(k + l + 1 + y1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)(j1 - 1), (double)(k + l + 1 + y1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)(j1 - 1), (double)(k - 1 + y1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                bufferbuilder.pos((double)(i1 + 1), (double)(k - 1 + y1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
            }
            if (k >= -4 && k + this.slotHeight+4 <= this.height) this.drawSlot(j, 0, k, l, mouseXIn, mouseYIn, partialTicks);
        }
    }

    protected int getScrollBarX()
    {
        return this.x+this.width-6;
    }

    public int getSlotHeight()
    {
        return this.slotHeight;
    }
}

