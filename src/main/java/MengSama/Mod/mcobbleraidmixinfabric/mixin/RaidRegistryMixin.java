package MengSama.Mod.mcobbleraidmixinfabric.mixin;

import MengSama.Mod.mcobbleraidmixinfabric.Config;

import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.registry.RaidRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;


@Mixin(RaidRegistry.class)
public abstract class RaidRegistryMixin {

    @Inject(
            method = "getRandomRaidBoss(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/Level;Ljava/util/BitSet;Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void filterBlacklist(
            RandomSource random,
            Level level,
            BitSet bitSet,
            String cacheKey,
            CallbackInfoReturnable<ResourceLocation> cir
    ) {

        if (Config.spawnBlacklistIds == null || Config.spawnBlacklistIds.isEmpty()) {
            return;
        }

        int size = bitSet.cardinality();
        if (size == 0) {
            cir.setReturnValue(null);
            return;
        }
        int[] allMatches = new int[size];
        int idx = 0;
        for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
            allMatches[idx++] = i;
        }

        List<Integer> filteredIndices = new ArrayList<>();
        for (int match : allMatches) {
            ResourceLocation raidLocation = RaidRegistry.RAID_LIST.get(match);
            RaidBoss boss = RaidRegistry.RAID_LOOKUP.get(raidLocation);
            if (boss == null) {
                continue;
            }
            String speciesStr = boss.getReward().getSpecies();
            if (speciesStr == null) {
                continue;
            }
            ResourceLocation speciesId;
            if (speciesStr.contains(":")) {
                speciesId = ResourceLocation.parse(speciesStr);
            } else {
                speciesId = ResourceLocation.parse("cobblemon:" + speciesStr);
            }
            if (!Config.spawnBlacklistIds.contains(speciesId)) {
                filteredIndices.add(match);
            }
        }

        if (filteredIndices.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        float[] weights = new float[filteredIndices.size()];
        float sum = 0.0F;

        for (int i = 0; i < filteredIndices.size(); i++) {
            int index = filteredIndices.get(i);
            ResourceLocation loc = RaidRegistry.RAID_LIST.get(index);
            RaidBoss boss = RaidRegistry.RAID_LOOKUP.get(loc);
            float weight = (float) (boss.getWeight() * boss.getTier().getWeight(level));
            sum += weight;
            weights[i] = sum;
        }

        float roll = random.nextFloat() * weights[weights.length - 1];
        int chosenIdx = java.util.Arrays.binarySearch(weights, roll);
        if (chosenIdx < 0) {
            chosenIdx = -chosenIdx - 1;
        }
        int finalIndex = filteredIndices.get(chosenIdx);
        ResourceLocation result = RaidRegistry.RAID_LIST.get(finalIndex);
        cir.setReturnValue(result);
    }
}