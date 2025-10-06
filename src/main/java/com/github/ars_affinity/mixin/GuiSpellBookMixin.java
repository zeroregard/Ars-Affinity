package com.github.ars_affinity.mixin;

import com.github.ars_affinity.client.gui.ArsAffinityDocAssets;
import com.github.ars_affinity.client.gui.buttons.AffinityButton;
import com.github.ars_affinity.client.screen.AffinityScreen;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.hollingsworth.arsnouveau.client.gui.book.GuiSpellBook;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSpellBook.class)
public abstract class GuiSpellBookMixin {

    @Inject(method = "init", at = @At("TAIL"))
    private void addAffinityButton(CallbackInfo ci) {
        if (!ArsAffinityConfig.ENABLE_SPELL_BOOK_BUTTON.get()) {
            return;
        }

        GuiSpellBook spellBook = (GuiSpellBook) (Object) this;
        int bookLeft = spellBook.bookLeft;
        int bookTop = spellBook.bookTop;

        ((ScreenInvoker) (Object) this).callAddRenderableWidget(
            new AffinityButton(
                bookLeft - 15,
                bookTop + 140,
                ArsAffinityDocAssets.AFFINITY_TAB,
                (b) -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    if (minecraft.player != null) {
                        GuiSpellBook currentScreen = (GuiSpellBook) (Object) this;
                        minecraft.setScreen(new AffinityScreen(minecraft.player, true, currentScreen));
                    }
                }
            ).withTooltip(Component.translatable("ars_affinity.gui.affinities"))
        );
    }
}
