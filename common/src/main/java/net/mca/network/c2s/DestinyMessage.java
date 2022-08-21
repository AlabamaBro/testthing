package net.mca.network.c2s;

import net.mca.Config;
import net.mca.cobalt.network.Message;
import net.mca.util.WorldUtils;
import net.mca.util.compat.FuzzyPositionsCompat;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;

import java.io.Serial;
import java.util.EnumSet;

public class DestinyMessage implements Message {
    @Serial
    private static final long serialVersionUID = -782119062565197963L;

    private final String location;

    public DestinyMessage(String location) {
        this.location = location;
    }

    @Override
    public void receive(ServerPlayerEntity player) {
        if (player.isSpectator()) {
            player.changeGameMode(GameMode.SURVIVAL);
        }

        if (Config.getInstance().allowDestinyTeleportation && location != null) {
            WorldUtils.getClosestStructurePosition(player.getServerWorld(), player.getBlockPos(), new Identifier(location), 128).ifPresent(pos -> {
                player.getServerWorld().getWorldChunk(pos);
                pos = player.getServerWorld().getTopPosition(Heightmap.Type.WORLD_SURFACE, pos);
                pos = FuzzyPositionsCompat.upWhile(pos, player.getServerWorld().getHeight(), p -> player.getServerWorld().getBlockState(p).shouldSuffocate(player.getServerWorld(), p));
                pos = FuzzyPositionsCompat.downWhile(pos, 1, p -> !player.getServerWorld().getBlockState(p.down()).isFullCube(player.getServerWorld(), p));

                ChunkPos chunkPos = new ChunkPos(pos);
                player.getServerWorld().getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getId());
                player.networkHandler.requestTeleport(pos.getX(), pos.getY(), pos.getZ(), player.getYaw(), player.getPitch(), EnumSet.noneOf(PlayerPositionLookS2CPacket.Flag.class));

                //set spawn
                player.setSpawnPoint(player.world.getRegistryKey(), pos, 0.0f, true, false);
                if (player.world.getServer() != null && player.world.getServer().isHost(player.getGameProfile())) {
                    player.getServerWorld().setSpawnPos(pos, 0.0f);
                }
            });
        }
    }
}
