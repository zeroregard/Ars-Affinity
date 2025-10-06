package com.github.ars_affinity.client.gui.buttons;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.hollingsworth.arsnouveau.api.documentation.DocAssets;
import com.hollingsworth.arsnouveau.client.gui.buttons.GuiImageButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class AffinityButton extends GuiImageButton {
    
    private static final ResourceLocation BORDER_TEXTURE = ArsAffinity.prefix("textures/gui/affinity_button_border.png");
    
    public AffinityButton(int x, int y, DocAssets.BlitInfo blitInfo, Button.OnPress onPress) {
        super(x, y, blitInfo, onPress);
    }
    
    @Override
    protected void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (!this.visible) {
            return;
        }
        
        // Render the main button image first
        super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);
        
        // Check if player has any available points and render border overlay
        Player player = net.minecraft.client.Minecraft.getInstance().player;
        if (player != null) {
            var affinityData = PlayerAffinityDataHelper.getPlayerAffinityData(player);
            if (affinityData != null && affinityData.hasAnyAvailablePoints()) {
                // Render the border texture as an overlay at the same position as the button
                graphics.blit(BORDER_TEXTURE, getX(), getY(), 0, 0, getWidth(), getHeight(), 23, 20);
            }
        }
    }
}
