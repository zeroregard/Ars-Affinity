package com.github.ars_affinity.mixin;

import com.hollingsworth.nuggets.client.gui.BaseScreen;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BaseScreen.class)
public interface ScreenInvoker {
    @Invoker("addRenderableWidget")
    <T extends GuiEventListener & Renderable & NarratableEntry> T callAddRenderableWidget(T widget);
}
