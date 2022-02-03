package ru.tulavcube.Spectate;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import ru.tulavcube.Spectate.commands.spec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Spectate extends JavaPlugin {

    public static List<String> worldWhitelist = new ArrayList<>();
    public static int geoFenceRange;

    public static HashMap<Player, DummyPlayer> playerShadowMap = new HashMap<>();
    public static HashMap<Player, GameMode> playerGameModeMap = new HashMap<>();

    private BukkitTask geoFence;

    @Override
    public void onEnable() {
        super.onEnable();

        saveDefaultConfig();
        worldWhitelist = getConfig().getStringList("worlds");
        geoFenceRange = getConfig().getInt("geofence-range");

        getServer().getPluginManager().registerEvents(new EventListener(), this);

        getCommand("spec").setExecutor(new spec(this));
        getCommand("spec").setTabCompleter(new spec(this));

        geoFence = new GeoFence().runTaskTimer(this, 0, 5);

    }

    @Override
    public void onDisable() {
        super.onDisable();
        geoFence.cancel();
        for (Player player : getServer().getOnlinePlayers())
            if (player.getScoreboardTags().contains("ru.tulavcube.Spectate:spectating")) Spectate.leaveShadow(player);
        for (DummyPlayer dummy : DummyPlayer.dummies) dummy.remove("Plugin disabled");
    }

    public static void turnIntoShadow(Player player) {
        DummyPlayer shadow = DummyPlayer.spawnBot("" + ChatColor.GRAY + ChatColor.BOLD + "SPECTATING"
                + ChatColor.RESET, player.getLocation(), player);
        shadow.getBukkitEntity().setGameMode(player.getGameMode());
        if (player.isFlying()) shadow.getBukkitEntity().setFlying(true);
        playerShadowMap.put(player, shadow);
        shadow.getBukkitEntity().getInventory().setContents(player.getInventory().getContents());
        shadow.getBukkitEntity().setLevel(player.getLevel());
        shadow.getBukkitEntity().setExp(player.getExp());
        Entity playerVehicle = player.getVehicle();
        if (playerVehicle != null) playerVehicle.addPassenger(shadow.getBukkitEntity());
        player.addScoreboardTag("ru.tulavcube.Spectate:spectating");
        playerGameModeMap.put(player, player.getGameMode());
        player.setGameMode(GameMode.SPECTATOR);
    }

    public static void leaveShadow(Player player) {
        DummyPlayer shadow = playerShadowMap.get(player);
        if (shadow != null) {
            player.teleport(shadow.getBukkitEntity());
            player.getInventory().setContents(shadow.getBukkitEntity().getInventory().getContents()); // fixing #1
            player.setLevel(shadow.getBukkitEntity().getLevel());
            player.setExp(shadow.getBukkitEntity().getExp());
            Entity shadowVehicle = shadow.getBukkitEntity().getVehicle();
            if (shadowVehicle != null) shadowVehicle.addPassenger(shadow.getSpawner());
            player.setHealth(shadow.getHealth());
            shadow.remove("Player left spectating mode");
        }
        player.removeScoreboardTag("ru.tulavcube.Spectate:spectating");
        GameMode gameMode = playerGameModeMap.get(player);
        if (gameMode != null) player.setGameMode(gameMode);
        playerShadowMap.remove(player);
        playerGameModeMap.remove(player);
    }
}
