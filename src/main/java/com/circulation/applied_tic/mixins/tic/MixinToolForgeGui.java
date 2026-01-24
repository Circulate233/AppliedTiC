package com.circulation.applied_tic.mixins.tic;

import com.circulation.applied_tic.mixins.utils.IToolForgeGui;
import net.minecraft.client.gui.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tconstruct.tools.gui.GuiButtonTool;
import tconstruct.tools.gui.ToolForgeGui;

@Mixin(ToolForgeGui.class)
public class MixinToolForgeGui implements IToolForgeGui {

    @Unique
    private String at$domain = "tinker";

    @Override
    public String at$getElementDomain() {
        return at$domain;
    }

    @Inject(method = "actionPerformed", at = @At(value = "INVOKE", target = "Ltconstruct/tools/gui/ToolForgeGui;setSlotType(I)V", remap = false))
    private void actionPerformed(GuiButton button, CallbackInfo ci) {
        at$domain = ((GuiButtonTool) button).element.domain;
    }
}
