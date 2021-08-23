/*
 *  Plugin that adds server-side bots
 *
 *   Copyright (C) 2021  MrTransistor
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package ru.tulavcube.Spectate;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class DummyPlayer extends EntityPlayer {
    public static ArrayList<DummyPlayer> dummies = new ArrayList<>();
    public static ArrayList<String> dummyNames = new ArrayList<>();

    private Player spawner;

    private static String[] getSkin(EntityPlayer entityPlayer) {
        GameProfile gameProfile = entityPlayer.getProfile();
        Property property = gameProfile.getProperties().get("textures").iterator().next();
        String texture = property.getValue();
        String signature = property.getSignature();
        return new String[]{texture, signature};
    }

    public DummyPlayer(MinecraftServer server, WorldServer world, GameProfile profile) {
        super(server, world, profile);
        dummyNames.add(getName());
        dummies.add(this);
    }

    public static DummyPlayer spawnBot(String name, Location location, Player spawner) {
        MinecraftServer server = ((CraftServer) (Bukkit.getServer())).getServer();
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        NetworkManager networkManager = new DummyNetworkManager();

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
        DummyPlayer dummy = new DummyPlayer(server, worldServer, gameProfile);

        dummy.spawner = spawner;

        String[] texSign = getSkin(((CraftPlayer) spawner).getHandle());
        gameProfile.getProperties().put("textures", new Property("textures", texSign[0], texSign[1]));

        dummy.getDataWatcher().set(new DataWatcherObject<>(17, DataWatcherRegistry.a), (byte) 0x7f); // show all model layers

        dummy.b = new PlayerConnection(server, networkManager, dummy);
        worldServer.addPlayerJoin(dummy);

        dummy.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        dummy.setHeadRotation(location.getYaw());
        dummy.setYawPitch(location.getYaw(), location.getPitch());
        worldServer.getChunkProvider().movePlayer(dummy);


        server.getPlayerList().sendAll(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, dummy));
        server.getPlayerList().sendAll(new PacketPlayOutNamedEntitySpawn(dummy));
        server.getPlayerList().sendAll(new PacketPlayOutEntityMetadata(dummy.getId(), dummy.getDataWatcher(), true));
        return dummy;
    }

    public Player getSpawner() {
        return spawner;
    }

    @Override
    public void tick() {
        if (getMinecraftServer().ah() % 10 == 0) {
            b.syncPosition();
            getWorldServer().getChunkProvider().movePlayer(this);
        }
        super.tick();
        playerTick();
    }

    public void remove(String reason) {
        b.disconnect(reason);
        dummyNames.remove(getName());
        dummies.remove(this);
    }

    @Override
    public void die(DamageSource damagesource) {
        super.die(damagesource);
        getMinecraftServer().a(new TickTask(getMinecraftServer().ah(), () -> remove("Died")));
    }

}
