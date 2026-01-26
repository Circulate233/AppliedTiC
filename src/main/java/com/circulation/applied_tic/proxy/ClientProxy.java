package com.circulation.applied_tic.proxy;

import net.minecraft.util.StatCollector;
import net.minecraftforge.client.MinecraftForgeClient;

import com.circulation.applied_tic.AppliedTiC;
import com.circulation.applied_tic.registry.ItemRegistry;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import tconstruct.client.FlexibleToolRenderer;
import tconstruct.library.client.TConstructClientRegistry;

public final class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        FlexibleToolRenderer renderer = new FlexibleToolRenderer();
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.itemCell, renderer);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        /*
         * xButton与yButton 以256x256贴图左上角作为原点，18x18切分一次，x和y从左到右从上到下对应位置，从0开始
         * 数组同理，4个按钮对应0123位置，分别为X和Y数组，皆为13时认为这个格子不被需要从而放置在左边
         * 为什么4个按钮的纹理固定使用匠魂自己的？
         * slotType为格子排布方法 ToolForgeGui#setSlotType
         */
        TConstructClientRegistry.addTierTwoButton(
            2,
            0,
            0,
            new int[] { 1, 2, 13, 13 },
            new int[] { 0, 0, 13, 13 },
            StatCollector.translateToLocal("tool.ticstoragecell"),
            StatCollector.translateToLocal("tool.ticstoragecell.desc"),
            AppliedTiC.MODID,
            "textures/gui/icons.png");
    }
}
