package com.circulation.applied_tic.mixins.IguanaTweaksTConstruct;

import java.util.ListIterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import iguanaman.iguanatweakstconstruct.leveling.handlers.LevelingToolTipHandler;

@Mixin(value = LevelingToolTipHandler.class, remap = false)
public class MixinLevelingToolTipHandler {

    @Redirect(
        method = "onItemToolTip",
        at = @At(value = "INVOKE", target = "Ljava/util/ListIterator;next()Ljava/lang/Object;"))
    public Object onItemToolTip(ListIterator<String> instance) {
        if (instance.hasNext()) {
            return instance.next();
        }
        return "a";
    }
}
