package de.theshadowsdust.containersort.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.theshadowsdust.containersort.api.ContainerSortApi;
import de.theshadowsdust.containersort.configuration.ContainerSortConfiguration;
import de.theshadowsdust.containersort.configuration.ContainerSortProperty;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ConfigurationService {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
    private final ContainerSortApi api;
    private final File configFile;
    private ContainerSortConfiguration configuration;
    private final ContainerSortConfiguration defaultConfig;

    public ConfigurationService(@NotNull ContainerSortApi api) throws IOException {
        this.api = api;

        this.configFile = new File(api.getPlugin().getDataFolder(), "config.json");
        this.defaultConfig = new ContainerSortConfiguration(getPluginVersion(),
                List.of("world_nether", "world_the_end"),
                List.of("chest", "barrel", "shulker_box"),
                ContainerSortProperty.DEFAULTS,
                List.of("&f[&6ContainerSort&f]", "&e%sign_owner_name%", "%container_sort_type%", " "));

        if (Files.exists(this.configFile.toPath())) {
            this.configuration = loadConfig();
        } else {
            Files.createFile(this.configFile.toPath());
            this.configuration = defaultConfig;
            saveConfig(this.defaultConfig);
        }
    }

    public void reloadConfig() throws IOException {
        this.configuration = loadConfig();
    }

    @NotNull
    public ContainerSortConfiguration getConfiguration() {
        return configuration;
    }

    @NotNull
    private ContainerSortConfiguration loadConfig() throws IOException {

        var config = defaultConfig;
        boolean needUpdate;

        try (BufferedReader bufferedReader = Files.newBufferedReader(this.configFile.toPath())) {
            config = gson.fromJson(bufferedReader, ContainerSortConfiguration.class);
            needUpdate = config == null || !config.getPluginVersion().equals(defaultConfig.getPluginVersion());
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }

        if (needUpdate) {

            File file = new File(this.api.getPlugin().getDataFolder(), String.format("bak_config_%s.json", dateFormat.format(new Date())));
            if (!file.exists()) {
                Files.copy(this.configFile.toPath(), file.toPath());
            }

            config = defaultConfig;
            saveConfig(defaultConfig);
        }

        return config;
    }

    private void saveConfig(@NotNull ContainerSortConfiguration config) {
        try (BufferedWriter writer = Files.newBufferedWriter(this.configFile.toPath())) {
            writer.write(gson.toJson(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @NotNull
    private String getPluginVersion() {

        var pluginMeta = this.api.getPlugin().getPluginMeta();
        if (pluginMeta == null) return "";

        String version = pluginMeta.getVersion();
        String versionSplitter = "-";
        if (version.contains(versionSplitter)) {
            version = version.split(versionSplitter)[0];
        }
        return version;
    }
}
