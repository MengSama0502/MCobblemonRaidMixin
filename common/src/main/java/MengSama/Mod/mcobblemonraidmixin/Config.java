package MengSama.Mod.mcobblemonraidmixin;

import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;
import java.util.stream.Collectors;


public class Config {

    private static final Logger LOGGER =
            LoggerFactory.getLogger("MCobblemonRaidMixin");

    private static final String CONFIG_RESOURCE_PATH = "/mcobblemonraidmixin.yml";

    private static final Path EXTERNAL_CONFIG_PATH =
            Paths.get("config", "mcobblemonraidmixin.yml");

    private static final int TIER_COUNT = 7;


    public enum IVMode {
        SET,
        RANDOM,
        MIN_MAX,
        EXACT
    }


    public static String language = "zh";

    public static IVMode ivMode = IVMode.SET;

    public static List<Integer> guaranteedVPerTier =
            Arrays.asList(0, 1, 1, 1, 2, 2, 3);

    public static List<Integer> minIVPerTier =
            Arrays.asList(0, 0, 0, 0, 0, 0, 0);

    public static List<Integer> maxIVPerTier =
            Arrays.asList(20, 20, 20, 20, 20, 20, 20);

    public static List<Integer> maxVPerTier =
            Arrays.asList(80, 90, 100, 110, 120, 130, 140);

    public static List<Integer> minIVRangePerTier =
            Arrays.asList(0, 5, 10, 15, 20, 25, 31);

    public static List<Integer> maxIVRangePerTier =
            Arrays.asList(10, 15, 20, 25, 31, 31, 31);

    public static List<int[]> exactIVsPerTier = new ArrayList<>();


    public static List<String> spawnBlacklist = new ArrayList<>();
    public static Set<ResourceLocation> spawnBlacklistIds = new HashSet<>();


    public static Map<ResourceLocation, SpeciesOverride> speciesOverrides =
            new HashMap<>();

    public static List<List<WeightedCommand>> rewardCommandsPerTier =
            new ArrayList<>();

    public static class WeightedCommand {
        public String command;
        public int weight;

        public WeightedCommand(String command, int weight) {
            this.command = command;
            this.weight = weight;
        }
    }

    public static class SpeciesOverride {

        public IVMode mode;

        public int[] exactIVs;

        public Integer maxV;
        public Integer minIV;
        public Integer guaranteed;
        public Integer maxIV;
        public Integer minRange;
        public Integer maxRange;
    }


    public static void load() {
        Lang.load("zh");

        InputStream input = null;
        boolean usingExternal = false;

        if (Files.exists(EXTERNAL_CONFIG_PATH)) {
            try {
                input = Files.newInputStream(EXTERNAL_CONFIG_PATH);
                usingExternal = true;
                LOGGER.info(Lang.format("config.loading_external", "path", EXTERNAL_CONFIG_PATH.toAbsolutePath().toString()));
            } catch (IOException e) {
                LOGGER.warn(Lang.get("config.load_external_failed"), e);
            }
        }
        String loadedResourcePath = null;
        if (input == null) {
            String lang = System.getProperty("mcobblemonraidmixin.language", "zh");
            String langResourcePath = "/mcobblemonraidmixin_" + lang + ".yml";
            input = Config.class.getResourceAsStream(langResourcePath);
            if (input != null) {
                LOGGER.info("Loading language-specific config: {}", langResourcePath);
                loadedResourcePath = langResourcePath;
            } else {
                LOGGER.info("Language-specific config '{}' not found, falling back to default", langResourcePath);
                input = Config.class.getResourceAsStream(CONFIG_RESOURCE_PATH);
                if (input != null) {
                    loadedResourcePath = CONFIG_RESOURCE_PATH;
                }
            }
            if (input == null) {
                LOGGER.error(Lang.format("config.not_found", "path", CONFIG_RESOURCE_PATH));
                setHardcodedDefaults();
                return;
            }
            LOGGER.info(Lang.format("config.loading_internal", "path", CONFIG_RESOURCE_PATH));
        }
        final String finalLoadedResourcePath = loadedResourcePath;
        final InputStream finalInput = input;
        try (finalInput) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(finalInput);

            if (data == null) {
                LOGGER.warn(Lang.get("config.empty"));
                setHardcodedDefaults();
                return;
            }

            language = (String) data.getOrDefault("language", "zh");
            if (!language.equals("zh")) {
                Lang.load(language);
            }

            ivMode = IVMode.valueOf(
                    (String) data.getOrDefault("raidIVMode", "SET")
            );

            guaranteedVPerTier = getIntegerList(
                    data,
                    "guaranteedVPerTier",
                    Arrays.asList(0, 1, 1, 1, 2, 2, 3)
            );

            minIVPerTier = getIntegerList(
                    data,
                    "minIVPerTier",
                    Arrays.asList(0, 0, 0, 0, 0, 0, 0)
            );

            maxIVPerTier = getIntegerList(
                    data,
                    "maxIVPerTier",
                    Arrays.asList(20, 20, 20, 20, 20, 20, 20)
            );

            maxVPerTier = getIntegerList(
                    data,
                    "maxVPerTier",
                    Arrays.asList(80, 90, 100, 110, 120, 130, 140)
            );

            minIVRangePerTier = getIntegerList(
                    data,
                    "minIVRangePerTier",
                    Arrays.asList(0, 5, 10, 15, 20, 25, 31)
            );

            maxIVRangePerTier = getIntegerList(
                    data,
                    "maxIVRangePerTier",
                    Arrays.asList(10, 15, 20, 25, 31, 31, 31)
            );

            exactIVsPerTier.clear();
            Object exactObj = data.get("exactIVsPerTier");
            if (exactObj instanceof List<?> list) {
                for (Object entry : list) {
                    if (entry instanceof List<?> innerList) {
                        int[] arr = innerList.stream()
                                .mapToInt(v -> ((Number) v).intValue())
                                .toArray();
                        exactIVsPerTier.add(arr);
                    }
                }
            }
            if (exactIVsPerTier.isEmpty()) {
                for (int i = 0; i < TIER_COUNT; i++) {
                    exactIVsPerTier.add(new int[]{31, 31, 31, 31, 31, 31});
                }
            }

            spawnBlacklist = getStringList(data, "spawnBlacklist", new ArrayList<>());
            spawnBlacklistIds = spawnBlacklist.stream()
                    .map(name -> name.contains(":") ? ResourceLocation.parse(name) : ResourceLocation.parse("cobblemon:" + name))
                    .collect(Collectors.toCollection(HashSet::new));

            speciesOverrides.clear();
            parseOverrideExact(data.get("overrideExact"));
            parseOverrideRandom(data.get("overrideRandom"));
            parseOverrideSet(data.get("overrideSet"));
            parseOverrideMinMax(data.get("overrideMinMax"));

            parseRewardCommands(data.get("rewardCommandsPerTier"));

            validateListSizes();

            if (finalLoadedResourcePath != null) {
                InputStream saveStream = Config.class.getResourceAsStream(finalLoadedResourcePath);
                if (saveStream != null) {
                    saveDefaultConfig(saveStream);
                }
            }

            LOGGER.info(Lang.format("config.loaded",
                    "count", String.valueOf(speciesOverrides.size()),
                    "source", usingExternal ? Lang.get("config.source_external") : Lang.get("config.source_internal")));

        } catch (Exception e) {
            LOGGER.error(Lang.get("config.load_failed"), e);
            setHardcodedDefaults();
        }
    }

    private static void validateListSizes() {
        List<List<?>> lists = Arrays.asList(
                guaranteedVPerTier,
                minIVPerTier,
                maxIVPerTier,
                maxVPerTier,
                minIVRangePerTier,
                maxIVRangePerTier
        );
        for (List<?> list : lists) {
            while (list.size() < TIER_COUNT) {
                if (list == guaranteedVPerTier) {
                    ((List<Integer>) list).add(0);
                } else if (list == minIVPerTier) {
                    ((List<Integer>) list).add(0);
                } else if (list == maxIVPerTier) {
                    ((List<Integer>) list).add(20);
                } else if (list == maxVPerTier) {
                    ((List<Integer>) list).add(80);
                } else if (list == minIVRangePerTier) {
                    ((List<Integer>) list).add(0);
                } else if (list == maxIVRangePerTier) {
                    ((List<Integer>) list).add(31);
                }
            }
        }
        while (exactIVsPerTier.size() < TIER_COUNT) {
            exactIVsPerTier.add(new int[]{31, 31, 31, 31, 31, 31});
        }
        while (rewardCommandsPerTier.size() < TIER_COUNT) {
            rewardCommandsPerTier.add(new ArrayList<>());
        }
    }


    private static void saveDefaultConfig(InputStream source) {
        try {
            Files.createDirectories(EXTERNAL_CONFIG_PATH.getParent());
            Files.copy(source, EXTERNAL_CONFIG_PATH);
        } catch (Exception e) {
            LOGGER.warn("Failed to save default config", e);
        } finally {
            try {
                source.close();
            } catch (IOException ignored) {
            }
        }
    }


    private static void setHardcodedDefaults() {
        Lang.load("zh");
        language = "zh";
        ivMode = IVMode.SET;
        guaranteedVPerTier = Arrays.asList(0, 1, 1, 1, 2, 2, 3);
        minIVPerTier = Arrays.asList(0, 0, 0, 0, 0, 0, 0);
        maxIVPerTier = Arrays.asList(20, 20, 20, 20, 20, 20, 20);
        maxVPerTier = Arrays.asList(80, 90, 100, 110, 120, 130, 140);
        minIVRangePerTier = Arrays.asList(0, 5, 10, 15, 20, 25, 31);
        maxIVRangePerTier = Arrays.asList(10, 15, 20, 25, 31, 31, 31);
        exactIVsPerTier.clear();
        for (int i = 0; i < TIER_COUNT; i++) {
            exactIVsPerTier.add(new int[]{31, 31, 31, 31, 31, 31});
        }
        spawnBlacklist = new ArrayList<>();
        spawnBlacklistIds = new HashSet<>();
        speciesOverrides.clear();
        rewardCommandsPerTier.clear();
        for (int i = 0; i < TIER_COUNT; i++) {
            rewardCommandsPerTier.add(new ArrayList<>());
        }
        LOGGER.warn(Lang.get("config.defaults_applied"));
    }


    @SuppressWarnings("unchecked")
    private static List<Integer> getIntegerList(
            Map<String, Object> data,
            String key,
            List<Integer> defaultValue
    ) {
        Object val = data.get(key);
        if (val instanceof List<?> list) {
            List<Integer> result = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof Number num) {
                    result.add(num.intValue());
                } else {
                    return defaultValue;
                }
            }
            return result;
        }
        return defaultValue;
    }


    @SuppressWarnings("unchecked")
    private static List<String> getStringList(
            Map<String, Object> data,
            String key,
            List<String> defaultValue
    ) {
        Object val = data.get(key);
        if (val instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object obj : list) {
                if (obj instanceof String str) {
                    result.add(str);
                } else {
                    return defaultValue;
                }
            }
            return result;
        }
        return defaultValue;
    }


    @SuppressWarnings("unchecked")
    private static void parseOverrideExact(Object obj) {
        if (!(obj instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            ResourceLocation species = ResourceLocation.parse(entry.getKey().toString());
            if (!(entry.getValue() instanceof List<?> list)) {
                continue;
            }
            if (list.size() != 6) {
                continue;
            }
            int[] ivs = new int[6];
            boolean valid = true;
            for (int i = 0; i < 6; i++) {
                if (list.get(i) instanceof Number num) {
                    ivs[i] = num.intValue();
                } else {
                    valid = false;
                    break;
                }
            }
            if (!valid) {
                continue;
            }
            SpeciesOverride ov = new SpeciesOverride();
            ov.mode = IVMode.EXACT;
            ov.exactIVs = ivs;
            speciesOverrides.put(species, ov);
        }
    }


    @SuppressWarnings("unchecked")
    private static void parseOverrideRandom(Object obj) {
        if (!(obj instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            ResourceLocation species = ResourceLocation.parse(entry.getKey().toString());
            SpeciesOverride ov = new SpeciesOverride();
            ov.mode = IVMode.RANDOM;
            if (entry.getValue() instanceof Number num) {
                ov.maxV = num.intValue();
            }
            speciesOverrides.put(species, ov);
        }
    }


    @SuppressWarnings("unchecked")
    private static void parseOverrideSet(Object obj) {
        if (!(obj instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            ResourceLocation species = ResourceLocation.parse(entry.getKey().toString());
            if (!(entry.getValue() instanceof List<?> list)) {
                continue;
            }
            if (list.size() < 2) {
                continue;
            }
            if (!(list.get(0) instanceof Number) || !(list.get(1) instanceof Number)) {
                continue;
            }
            SpeciesOverride ov = new SpeciesOverride();
            ov.mode = IVMode.SET;
            ov.minIV = ((Number) list.get(0)).intValue();
            ov.guaranteed = ((Number) list.get(1)).intValue();
            if (list.size() >= 3 && list.get(2) instanceof Number) {
                ov.maxIV = ((Number) list.get(2)).intValue();
            }
            speciesOverrides.put(species, ov);
        }
    }


    @SuppressWarnings("unchecked")
    private static void parseOverrideMinMax(Object obj) {
        if (!(obj instanceof Map<?, ?> map)) {
            return;
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            ResourceLocation species = ResourceLocation.parse(entry.getKey().toString());
            if (!(entry.getValue() instanceof List<?> list)) {
                continue;
            }
            if (list.size() != 2) {
                continue;
            }
            if (!(list.get(0) instanceof Number) || !(list.get(1) instanceof Number)) {
                continue;
            }
            SpeciesOverride ov = new SpeciesOverride();
            ov.mode = IVMode.MIN_MAX;
            ov.minRange = ((Number) list.get(0)).intValue();
            ov.maxRange = ((Number) list.get(1)).intValue();
            speciesOverrides.put(species, ov);
        }
    }

    @SuppressWarnings("unchecked")
    private static void parseRewardCommands(Object obj) {
        rewardCommandsPerTier.clear();
        for (int i = 0; i < TIER_COUNT; i++) {
            rewardCommandsPerTier.add(new ArrayList<>());
        }

        if (obj == null) {
            return;
        }

        if (obj instanceof Map<?, ?> mapObj) {
            for (Map.Entry<?, ?> entry : mapObj.entrySet()) {
                String key = entry.getKey().toString().toLowerCase();
                int tierIndex = getTierIndexFromKey(key);
                if (tierIndex < 0 || tierIndex >= TIER_COUNT) {
                    continue;
                }
                List<WeightedCommand> commands = parseCommandsFromObject(entry.getValue());
                rewardCommandsPerTier.set(tierIndex, commands);
            }
        } else if (obj instanceof List<?> list) {
            for (int i = 0; i < Math.min(list.size(), TIER_COUNT); i++) {
                List<WeightedCommand> commands = parseCommandsFromObject(list.get(i));
                rewardCommandsPerTier.set(i, commands);
            }
        }

        LOGGER.info(Lang.format("config.reward_commands_loaded", "count", String.valueOf(rewardCommandsPerTier.size())));
    }

    private static int getTierIndexFromKey(String key) {
        return switch (key) {
            case "tier1", "tier_1", "1", "one" -> 0;
            case "tier2", "tier_2", "2", "two" -> 1;
            case "tier3", "tier_3", "3", "three" -> 2;
            case "tier4", "tier_4", "4", "four" -> 3;
            case "tier5", "tier_5", "5", "five" -> 4;
            case "tier6", "tier_6", "6", "six" -> 5;
            case "tier7", "tier_7", "7", "seven" -> 6;
            default -> -1;
        };
    }

    @SuppressWarnings("unchecked")
    private static List<WeightedCommand> parseCommandsFromObject(Object obj) {
        List<WeightedCommand> commands = new ArrayList<>();

        if (obj instanceof String str && !str.isEmpty()) {
            commands.add(new WeightedCommand(str, 100));
        } else if (obj instanceof List<?> cmdList) {
            for (Object cmdObj : cmdList) {
                if (cmdObj instanceof Map<?, ?> cmdMap) {
                    Object cmd = cmdMap.get("command");
                    Object wgt = cmdMap.get("weight");
                    if (cmd instanceof String commandStr) {
                        int weight = 100;
                        if (wgt instanceof Number num) {
                            weight = num.intValue();
                        }
                        commands.add(new WeightedCommand(commandStr, weight));
                    }
                } else if (cmdObj instanceof String str) {
                    commands.add(new WeightedCommand(str, 100));
                }
            }
        }

        return commands;
    }
}