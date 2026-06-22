package MengSama.Mod.mcobblemonraidmixin.neoforge;

import MengSama.Mod.mcobblemonraidmixin.Config;
import MengSama.Mod.mcobblemonraidmixin.Lang;
import MengSama.Mod.mcobblemonraidmixin.RaidParticipantTracker;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod("mcobblemonraidmixin")
public class Mcobblemonraidmixin {
    private static final Logger LOGGER = LogUtils.getLogger();

    public Mcobblemonraidmixin(IEventBus modEventBus) {
        Config.load();
        LOGGER.info(Lang.get("console.common_setup"));
    }
}