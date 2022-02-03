package ru.tulavcube.Spectate;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GeoFence extends BukkitRunnable {
    @Override
    public void run() {
        if(Spectate.geoFenceRange <= 0) return;
        for (Player spectator : Spectate.playerShadowMap.keySet()) {
            Player shadow = Spectate.playerShadowMap.get(spectator).getBukkitEntity();
            if (spectator.getLocation().distance(shadow.getLocation()) > 40) {
                spectator.sendMessage(ChatColor.RED + "Can`t go any further");
                Location specLoc = spectator.getLocation();
                Vector rayTraceDir = specLoc.subtract(shadow.getLocation()).toVector().normalize();
                Location backOffLoc = shadow.getLocation().add(rayTraceDir.multiply(39));
                backOffLoc.setYaw(specLoc.getYaw());
                backOffLoc.setPitch(specLoc.getPitch());
                spectator.teleport(backOffLoc);
            }
        }
    }
}
