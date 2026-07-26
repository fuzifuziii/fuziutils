package org.fuzi.fuziutils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(FuziUtils.MODID)
public class FuziUtils {
    public static final String MODID = "fuziutils";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FuziUtils(IEventBus modEventBus) {
        LOGGER.info("[FuziUtils] Initialising");
        FuziUtilsCommon.init(modEventBus);
    }
}
