package MengSama.Mod.mcobbleraidmixinfabric;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public class Mcobbleraidmixinfabric implements ModInitializer {
    public static final String MODID = "mcobbleraidmixinfabric";
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        Config.load();
        LOGGER.info(Lang.get("console.common_setup"));
    }
}