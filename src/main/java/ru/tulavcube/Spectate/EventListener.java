package ru.tulavcube.Spectate;

import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
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
        ServerGamePacketListenerImpl plc = ((CraftPlayer) e.getPlayer()).getHandle().connection;
        for (DummyPlayer dummy : DummyPlayer.dummies) {
            plc.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, dummy));
            plc.send(new ClientboundAddPlayerPacket(dummy));
            plc.send(new ClientboundSetEntityDataPacket(dummy.getId(), dummy.getEntityData(), true));
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
        if (entity instanceof Player && ((CraftPlayer) entity).getHandle() instanceof DummyPlayer) {
            if (((CraftPlayer) entity).getHealth() - e.getFinalDamage() <= 0) {
                e.setCancelled(true);
                Player spectator = ((DummyPlayer) ((CraftPlayer) entity).getHandle()).getSpawner();
                Spectate.leaveShadow(spectator);
                spectator.setHealth(0);
            }
        }
    }
}
