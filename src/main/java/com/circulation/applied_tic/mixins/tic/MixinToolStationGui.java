package com.circulation.applied_tic.mixins.tic;

import net.minecraft.util.ResourceLocation;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.circulation.applied_tic.AppliedTiC;
import com.circulation.applied_tic.mixins.utils.IToolForgeGui;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import tconstruct.tools.gui.ToolStationGui;

@Mixin(ToolStationGui.class)
public class MixinToolStationGui {

    @Unique
    private static final ResourceLocation at$rl = new ResourceLocation("applied_tic", "textures/gui/icons.png");

    @WrapOperation(
        method = "drawGuiContainerBackgroundLayer",
        at = @At(
            value = "FIELD",
            target = "Ltconstruct/tools/gui/ToolStationGui;icons:Lnet/minecraft/util/ResourceLocation;",
            opcode = Opcodes.GETSTATIC))
    public ResourceLocation getNewRL(Operation<ResourceLocation> original) {
        if ((Object) this instanceof IToolForgeGui gui) {
            if (gui.at$getElementDomain()
                .equals(AppliedTiC.MODID)) {
                return at$rl;
            }
        }
        return original.call();
    }
}
