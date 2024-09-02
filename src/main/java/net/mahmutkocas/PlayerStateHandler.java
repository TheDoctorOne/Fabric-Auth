package net.mahmutkocas;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerStateHandler {

    private static class PlayerState {
        public final ServerPlayerEntity player;
        public final Vec3d pos;

        PlayerState(ServerPlayerEntity player, Vec3d pos) {
            this.player = player;
            this.pos = pos;
        }
    }
    public static final PlayerStateHandler INSTANCE = new PlayerStateHandler();

    private final Map<String, PlayerState> lockedPlayer = new HashMap<>();

    private PlayerStateHandler(){}

    public void add(ServerPlayerEntity player) {
        var pos = player.getPos();
        if(player.isDead() && player.getSpawnPointPosition() != null && player.getSpawnPointPosition().toCenterPos() != null) {
            pos = player.getSpawnPointPosition().toCenterPos();
        }
        lockedPlayer.put(player.getUuidAsString(), new PlayerState(player, pos));
    }

    public boolean isLocked(PlayerEntity player) {
        return lockedPlayer.containsKey(player.getUuidAsString());
    }

    public boolean isLocked(String uuid) {
        return lockedPlayer.containsKey(uuid);
    }

    public Vec3d getPlayerLockedLocation(ServerPlayerEntity player) {
        return lockedPlayer.get(player.getUuidAsString()).pos;
    }

    public List<ServerPlayerEntity> getPlayers() {
        return lockedPlayer.values().stream().map(playerState -> playerState.player).toList();
    }

    public void remove(ServerPlayerEntity player) {
        lockedPlayer.remove(player.getUuidAsString());
    }
}
