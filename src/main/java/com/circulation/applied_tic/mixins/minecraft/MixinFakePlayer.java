package com.circulation.applied_tic.mixins.minecraft;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FakePlayer.class)
public abstract class MixinFakePlayer extends EntityPlayerMP {

    public MixinFakePlayer(MinecraftServer p_i45285_1_, WorldServer p_i45285_2_, GameProfile p_i45285_3_, ItemInWorldManager p_i45285_4_) {
        super(p_i45285_1_, p_i45285_2_, p_i45285_3_, p_i45285_4_);
    }

    @Intrinsic
    @Override
    public void addChatMessage(IChatComponent message) {

    }
}
