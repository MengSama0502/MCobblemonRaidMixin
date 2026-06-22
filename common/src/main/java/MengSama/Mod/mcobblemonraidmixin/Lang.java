package MengSama.Mod.mcobblemonraidmixin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Lang {
    private static final Logger LOGGER = LoggerFactory.getLogger("MCobblemonRaidMixin");
    private static Map<String, Object> langData;

    public static void load(String language) {
        String langFile = "lang_" + language + ".yml";
        Path externalPath = Paths.get("config", "mcobblemonraidmixin", langFile);
        InputStream input = null;

        if (Files.exists(externalPath)) {
            try {
                input = Files.newInputStream(externalPath);
                LOGGER.info("Loading external lang file: {}", externalPath.toAbsolutePath());
            } catch (Exception e) {
                LOGGER.warn("Cannot load external lang file, trying internal", e);
            }
        }

        if (input == null) {
            input = Lang.class.getResourceAsStream("/" + langFile);
        }

        if (input == null) {
            LOGGER.warn("Lang file '{}' not found, falling back to zh", langFile);
            input = Lang.class.getResourceAsStream("/lang_zh.yml");
        }

        InputStream finalInput = input;
        if (finalInput == null) {
            LOGGER.error("No language file found!");
            return;
        }

        try (finalInput) {
            Yaml yaml = new Yaml();
            langData = yaml.load(finalInput);
            LOGGER.info("Language file loaded: {}", langFile);
        } catch (Exception e) {
            LOGGER.error("Failed to load language file", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static String get(String path, String def) {
        if (langData == null) return def;
        String[] parts = path.split("\\.");
        Object current = langData;
        for (String part : parts) {
            if (current instanceof Map<?, ?> map) {
                current = map.get(part);
            } else {
                return def;
            }
        }
        return current instanceof String s ? s : def;
    }

    public static String get(String path) {
        return get(path, path);
    }

    public static String format(String path, String... keyValuePairs) {
        String template = get(path);
        if (template == null) {
            return path;
        }
        for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
            template = template.replace("%" + keyValuePairs[i] + "%", keyValuePairs[i + 1]);
        }
        return template;
    }
}