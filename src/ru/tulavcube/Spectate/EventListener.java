package ru.tulavcube.Spectate;

import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        PlayerConnection plc = ((CraftPlayer) e.getPlayer()).getHandle().b;
        for (DummyPlayer dummy : DummyPlayer.dummies) {
            plc.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a,
                    dummy));
            plc.sendPacket(new PacketPlayOutNamedEntitySpawn(dummy));
            plc.sendPacket(new PacketPlayOutEntityMetadata(dummy.getId(),
                    dummy.getDataWatcher(), true));
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (player.getScoreboardTags().contains("ru.tulavcube.Spectate:spectating")) Spectate.leaveShadow(player);
    }

    @EventHandler
    public void onTakeDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        if (entity instanceof Player && ((CraftPlayer) entity).getHandle() instanceof DummyPlayer){
            if(((CraftPlayer) entity).getHealth() - e.getFinalDamage() <= 0){
                e.setCancelled(true);
                Player spectator = ((DummyPlayer) ((CraftPlayer) entity).getHandle()).getSpawner();
                Spectate.leaveShadow(spectator);
                spectator.setHealth(0);
            }
        }
    }
}
