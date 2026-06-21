package MengSama.Mod.mcobbleraidmixinfabric.mixin;

import MengSama.Mod.mcobbleraidmixinfabric.Config;
import MengSama.Mod.mcobbleraidmixinfabric.Lang;
import MengSama.Mod.mcobbleraidmixinfabric.RaidParticipantTracker;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Pokemon;

import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.data.raid.RaidTier;

import com.mojang.logging.LogUtils;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;

import org.slf4j.Logger;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;


@Mixin(RaidBoss.class)
public abstract class RaidBossMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<Stats> BASE_STATS = Arrays.asList(
            Stats.HP,
            Stats.ATTACK,
            Stats.DEFENCE,
            Stats.SPECIAL_ATTACK,
            Stats.SPECIAL_DEFENCE,
            Stats.SPEED
    );

    private void shuffleList(List<Stats> list, RandomSource random) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Stats temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }

    private int safeTierIndex(RaidTier tier, List<?> targetList) {
        int idx = tier.ordinal();
        int maxIdx = targetList.size() - 1;
        if (maxIdx < 0) maxIdx = 0;
        return Math.max(0, Math.min(idx, maxIdx));
    }

    @Inject(method = "getRewardPokemon", at = @At("RETURN"), remap = false)
    private void modifyRewardIV(ServerPlayer player, CallbackInfoReturnable<Pokemon> cir) {
        Pokemon pokemon = cir.getReturnValue();
        if (pokemon == null) return;

        RandomSource random = (player != null) ? player.getRandom() : RandomSource.create();
        RaidTier tier = ((RaidBoss) (Object) this).getTier();
        ResourceLocation speciesId = pokemon.getSpecies().getResourceIdentifier();
        Config.SpeciesOverride override = Config.speciesOverrides.get(speciesId);
        if (override != null) {
            applyOverrideProfile(pokemon, random, tier, override);
        } else {
            applyIVProfile(pokemon, random, tier);
        }

        Set<ServerPlayer> targets = new HashSet<>();
        if (player != null) {
            targets.add(player);
        } else {
            Set<ServerPlayer> participants = RaidParticipantTracker.getParticipants((RaidBoss) (Object) this);
            if (!participants.isEmpty()) {
                targets.addAll(participants);
                RaidParticipantTracker.clearParticipants((RaidBoss) (Object) this);
            } else {
                LOGGER.debug(Lang.get("reward.no_player_found"));
                return;
            }
        }

        int tierIndex = safeTierIndex(tier, Config.rewardCommandsPerTier);
        if (tierIndex < 0 || tierIndex >= Config.rewardCommandsPerTier.size()) {
            return;
        }
        List<Config.WeightedCommand> commands = Config.rewardCommandsPerTier.get(tierIndex);
        if (commands == null || commands.isEmpty()) {
            return;
        }
        Config.WeightedCommand selected = selectWeightedCommand(commands, random);
        if (selected == null || selected.command == null || selected.command.isEmpty()) {
            return;
        }

        for (ServerPlayer target : targets) {
            executeRewardCommand(target, tierIndex, selected);
        }
    }

    private void executeRewardCommand(ServerPlayer player, int tierIndex, Config.WeightedCommand selected) {
        if (player == null || player.getServer() == null) {
            return;
        }

        String playerName = player.getName().getString();
        String command = selected.command
                .replace("{player}", playerName)
                .replace("{tier}", String.valueOf(tierIndex + 1))
                .replace("{tierIndex}", String.valueOf(tierIndex));

        executeCommand(player, command);
    }

    private Config.WeightedCommand selectWeightedCommand(List<Config.WeightedCommand> commands, RandomSource random) {
        if (commands == null || commands.isEmpty()) {
            return null;
        }

        if (commands.size() == 1) {
            return commands.get(0);
        }

        int totalWeight = 0;
        for (Config.WeightedCommand cmd : commands) {
            totalWeight += cmd.weight;
        }

        if (totalWeight <= 0) {
            return commands.get(0);
        }

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;

        for (Config.WeightedCommand cmd : commands) {
            cumulative += cmd.weight;
            if (roll < cumulative) {
                return cmd;
            }
        }

        return commands.get(commands.size() - 1);
    }

    private void executeCommand(ServerPlayer player, String command) {
        CommandDispatcher<CommandSourceStack> dispatcher = player.getServer().getCommands().getDispatcher();
        CommandSourceStack source = player.createCommandSourceStack().withPermission(4);

        String commandToParse = command.startsWith("/") ? command.substring(1) : command;

        try {
            ParseResults<CommandSourceStack> parseResults = dispatcher.parse(new StringReader(commandToParse), source);
            dispatcher.execute(parseResults);
        } catch (Exception e) {
            LOGGER.error(Lang.format("reward.command_failed", "command", command, "error", e.getMessage()));
        }
    }


    private void applyOverrideProfile(
            Pokemon pokemon,
            RandomSource random,
            RaidTier tier,
            Config.SpeciesOverride override
    ) {
        switch (override.mode) {
            case SET -> {
                int tierIndex = safeTierIndex(tier, Config.guaranteedVPerTier);
                int min = override.minIV != null ? override.minIV : Config.minIVPerTier.get(tierIndex);
                int guaranteed = override.guaranteed != null ? override.guaranteed : Config.guaranteedVPerTier.get(tierIndex);
                int max = override.maxIV != null ? override.maxIV : Config.maxIVPerTier.get(tierIndex);
                applySetMode(pokemon, random, min, guaranteed, max);
            }

            case RANDOM -> {
                int tierIndex = safeTierIndex(tier, Config.maxVPerTier);
                int maxV = override.maxV != null ? override.maxV : Config.maxVPerTier.get(tierIndex);
                applyRandomMode(pokemon, random, maxV);
            }

            case MIN_MAX -> {
                int tierIndex = safeTierIndex(tier, Config.minIVRangePerTier);
                int minRange = override.minRange != null ? override.minRange : Config.minIVRangePerTier.get(tierIndex);
                int maxRange = override.maxRange != null ? override.maxRange : Config.maxIVRangePerTier.get(tierIndex);
                applyMinMaxMode(pokemon, random, minRange, maxRange);
            }

            case EXACT -> {
                if (override.exactIVs != null) {
                    applyExactMode(pokemon, override.exactIVs);
                } else {
                    int tierIndex = safeTierIndex(tier, Config.exactIVsPerTier);
                    applyExactModeByTier(pokemon, tierIndex);
                }
            }
        }
    }


    private void applyIVProfile(Pokemon pokemon, RandomSource random, RaidTier tier) {
        switch (Config.ivMode) {
            case SET -> {
                int tierIndex = safeTierIndex(tier, Config.guaranteedVPerTier);
                applySetModeByTier(pokemon, random, tierIndex);
            }
            case RANDOM -> {
                int tierIndex = safeTierIndex(tier, Config.maxVPerTier);
                applyRandomModeByTier(pokemon, random, tierIndex);
            }
            case MIN_MAX -> {
                int tierIndex = safeTierIndex(tier, Config.minIVRangePerTier);
                applyMinMaxModeByTier(pokemon, random, tierIndex);
            }
            case EXACT -> {
                int tierIndex = safeTierIndex(tier, Config.exactIVsPerTier);
                applyExactModeByTier(pokemon, tierIndex);
            }
        }
    }


    private void applySetModeByTier(Pokemon pokemon, RandomSource random, int tierIndex) {
        int min = Config.minIVPerTier.get(tierIndex);
        int guaranteed = Config.guaranteedVPerTier.get(tierIndex);
        int max = Config.maxIVPerTier.get(tierIndex);
        applySetMode(pokemon, random, min, guaranteed, max);
    }


    private void applyRandomModeByTier(Pokemon pokemon, RandomSource random, int tierIndex) {
        int maxV = Config.maxVPerTier.get(tierIndex);
        applyRandomMode(pokemon, random, maxV);
    }


    private void applyMinMaxModeByTier(Pokemon pokemon, RandomSource random, int tierIndex) {
        int min = Config.minIVRangePerTier.get(tierIndex);
        int max = Config.maxIVRangePerTier.get(tierIndex);
        applyMinMaxMode(pokemon, random, min, max);
    }


    private void applyExactModeByTier(Pokemon pokemon, int tierIndex) {
        if (tierIndex >= Config.exactIVsPerTier.size()) {
            for (Stats stat : BASE_STATS) {
                pokemon.getIvs().set(stat, 31);
            }
            return;
        }
        int[] exact = Config.exactIVsPerTier.get(tierIndex);
        applyExactMode(pokemon, exact);
    }

    private void applySetMode(
            Pokemon pokemon,
            RandomSource random,
            int min,
            int guaranteed,
            int max
    ) {
        List<Stats> statsList = new ArrayList<>(BASE_STATS);
        shuffleList(statsList, random);
        for (int i = 0; i < statsList.size(); i++) {
            int value;
            if (i < guaranteed) {
                value = 31;
            } else {
                if (min == max) {
                    value = min;
                } else {
                    int low = Math.min(min, max);
                    int high = Math.max(min, max);
                    value = low + random.nextInt(high - low + 1);
                }
            }
            pokemon.getIvs().set(statsList.get(i), value);
        }
    }


    private void applyRandomMode(Pokemon pokemon, RandomSource random, int maxV) {
        if (maxV > 186) maxV = 186;
        if (maxV < 0) maxV = 0;

        List<Stats> statsList = new ArrayList<>(BASE_STATS);
        shuffleList(statsList, random);

        double[] raw = new double[6];
        double rawSum = 0;
        for (int i = 0; i < 6; i++) {
            raw[i] = random.nextDouble() + 0.001;
            rawSum += raw[i];
        }

        int[] ivs = new int[6];
        int allocated = 0;
        for (int i = 0; i < 6; i++) {
            ivs[i] = (int) Math.round(maxV * raw[i] / rawSum);
            ivs[i] = Math.min(31, Math.max(0, ivs[i]));
            allocated += ivs[i];
        }

        int diff = maxV - allocated;
        while (diff != 0) {
            List<Integer> adjustable = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                if (diff > 0 && ivs[i] < 31) adjustable.add(i);
                if (diff < 0 && ivs[i] > 0) adjustable.add(i);
            }
            if (adjustable.isEmpty()) break;
            int idx = adjustable.get(random.nextInt(adjustable.size()));
            if (diff > 0) {
                ivs[idx]++;
                diff--;
            } else {
                ivs[idx]--;
                diff++;
            }
        }

        for (int i = 0; i < 6; i++) {
            pokemon.getIvs().set(statsList.get(i), ivs[i]);
        }
    }


    private void applyMinMaxMode(Pokemon pokemon, RandomSource random, int min, int max) {
        int lo = Math.min(min, max);
        int hi = Math.max(min, max);
        for (Stats stat : BASE_STATS) {
            int iv = (lo == hi) ? lo : random.nextInt(hi - lo + 1) + lo;
            pokemon.getIvs().set(stat, iv);
        }
    }


    private void applyExactMode(Pokemon pokemon, int[] exact) {
        if (exact.length != BASE_STATS.size()) {
            LOGGER.error(Lang.format("reward.exact_iv_length_error",
                    "expected", String.valueOf(BASE_STATS.size()),
                    "actual", String.valueOf(exact.length)));
            for (Stats stat : BASE_STATS) {
                pokemon.getIvs().set(stat, 31);
            }
            return;
        }
        int i = 0;
        for (Stats stat : BASE_STATS) {
            pokemon.getIvs().set(stat, exact[i++]);
        }
    }
}