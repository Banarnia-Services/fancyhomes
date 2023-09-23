package de.banarnia.fancyhomes;

import co.aikar.commands.BukkitCommandManager;
import co.aikar.commands.CommandManager;
import de.banarnia.fancyhomes.api.UtilFile;
import de.banarnia.fancyhomes.api.config.Config;
import de.banarnia.fancyhomes.api.config.YamlConfig;
import de.banarnia.fancyhomes.api.config.YamlVersionConfig;
import de.banarnia.fancyhomes.api.lang.LanguageHandler;
import de.banarnia.fancyhomes.commands.*;
import de.banarnia.fancyhomes.config.HomeConfig;
import de.banarnia.fancyhomes.data.storage.Home;
import de.banarnia.fancyhomes.lang.Message;
import de.banarnia.fancyhomes.listener.HomeListener;
import de.banarnia.fancyhomes.manager.HomeManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class FancyHomes extends JavaPlugin {

    private static FancyHomes instance;

    private CommandManager commandManager;
    private HomeConfig homeConfig;
    private LanguageHandler languageHandler;
    private HomeManager manager;

    @Override
    public void onLoad() {
        super.onLoad();
        instance = this;

        ConfigurationSerialization.registerClass(Home.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        this.commandManager = new BukkitCommandManager(this);
        commandManager.usePerIssuerLocale(true);

        Config config = YamlVersionConfig.of(this, getDataFolder(), "config.yml",
                                    "config.yml", "1.0");
        this.homeConfig = new HomeConfig(this, config);

        File langFolder = new File(getDataFolder(), "lang");
        YamlConfig.fromResource(this, "lang/en.yml", langFolder, "en.yml");
        YamlConfig.fromResource(this, "lang/de.yml", langFolder, "de.yml");
        this.languageHandler = new LanguageHandler(this, homeConfig.getLanguage());
        this.languageHandler.register(Message.class);

        this.manager = new HomeManager(this, homeConfig);
        if (!this.manager.init()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(new HomeListener(manager), this);

        CommandSetup.initCommandCompletion(commandManager);
        CommandSetup.initCommandContext(commandManager);

        commandManager.registerCommand(new HomeCommand(homeConfig));
        commandManager.registerCommand(new HomesCommand());
        commandManager.registerCommand(new SethomeCommand());
        commandManager.registerCommand(new DelhomeCommand());

        Bukkit.getOnlinePlayers().forEach(player -> manager.getHomeData(player.getUniqueId()));
    }

    public static FancyHomes getInstance() {
        return instance;
    }

    public HomeManager getManager() {
        return manager;
    }
}
