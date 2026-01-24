package com.circulation.applied_tic;

import com.circulation.applied_tic.proxy.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = AppliedTiC.MODID, version = Tags.VERSION, name = "Applied TiC", acceptedMinecraftVersions = "[1.7.10]")
public class AppliedTiC {

    public static final String MODID = "applied_tic";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.circulation.applied_tic.proxy.ClientProxy",
        serverSide = "com.circulation.applied_tic.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance
    public static AppliedTiC instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {

    }
}
