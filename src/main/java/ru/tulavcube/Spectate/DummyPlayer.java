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
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class DummyPlayer extends ServerPlayer {
    public static ArrayList<DummyPlayer> dummies = new ArrayList<>();
    public static ArrayList<String> dummyNames = new ArrayList<>();

    private Player spawner;

    private static String[] getSkin(ServerPlayer player) {
        GameProfile gameProfile = player.getGameProfile();
        if (!gameProfile.getProperties().containsKey("textures")) return null;
        Property property = gameProfile.getProperties().get("textures").iterator().next();
        String texture = property.getValue();
        String signature = property.getSignature();
        return new String[]{texture, signature};
    }

    public DummyPlayer(MinecraftServer server, ServerLevel world, GameProfile profile) {
        super(server, world, profile, null);
        dummyNames.add(getName().getString());
        dummies.add(this);
    }

    public static DummyPlayer spawnBot(String name, Location location, Player spawner) {
        MinecraftServer server = ((CraftServer) (Bukkit.getServer())).getServer();
        ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();
        Connection conn = new DummyConnection();

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);
        DummyPlayer dummy = new DummyPlayer(server, world, gameProfile);

        dummy.spawner = spawner;

        String[] texSign = getSkin(((CraftPlayer) spawner).getHandle());

        if (texSign != null)
            gameProfile.getProperties().put("textures", new Property("textures", texSign[0], texSign[1]));

        dummy.getEntityData().set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 0x7f); // show all model layers

        dummy.connection = new ServerGamePacketListenerImpl(server, conn, dummy);
        world.addNewPlayer(dummy);

        dummy.forceSetPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        dummy.setYHeadRot(location.getYaw());
        world.getChunkSource().move(dummy);

        server.getPlayerList().broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, dummy));
        server.getPlayerList().broadcastAll(new ClientboundAddPlayerPacket(dummy));
        server.getPlayerList().broadcastAll(new ClientboundSetEntityDataPacket(dummy.getId(), dummy.getEntityData(), true));
        return dummy;
    }

    public Player getSpawner() {
        return spawner;
    }

    @Override
    public void tick() {
        super.tick();
        if (getServer().getTickCount() % 10 == 0) {
            connection.resetPosition();
            getLevel().getChunkSource().move(this);
        }
        doTick();
    }

    public void remove(String reason) {
        connection.disconnect(reason);
        dummyNames.remove(getName().getContents());
        dummies.remove(this);
    }

    @Override
    public void die(DamageSource damagesource) {
        super.die(damagesource);
        getServer().execute(new TickTask(getServer().getTickCount() + 200, () -> remove("Died")));
    }
}
