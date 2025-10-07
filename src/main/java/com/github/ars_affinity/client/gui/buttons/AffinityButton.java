package com.github.ars_affinity.client.gui.buttons;

import com.hollingsworth.arsnouveau.api.documentation.DocAssets;
import com.hollingsworth.arsnouveau.client.gui.buttons.GuiImageButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;

public class AffinityButton extends GuiImageButton {
    
    public AffinityButton(int x, int y, DocAssets.BlitInfo blitInfo, Button.OnPress onPress) {
        super(x, y, blitInfo, onPress);
    }
    
    @Override
    protected void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (!this.visible) {
            return;
        }
        
        super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);
    }
}
