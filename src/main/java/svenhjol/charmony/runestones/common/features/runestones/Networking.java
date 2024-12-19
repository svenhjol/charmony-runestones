package svenhjol.charmony.runestones.common.features.runestones;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import svenhjol.charmony.api.RunestoneLocation;
import svenhjol.charmony.core.base.Setup;
import svenhjol.charmony.runestones.RunestonesMod;

import java.util.Objects;

public final class Networking extends Setup<Runestones> {
    public Networking(Runestones feature) {
        super(feature);
    }

    // Client-to-server packet that tells the server what runestone blockpos the player is looking at.
    public record C2SPlayerLooking(BlockPos pos) implements CustomPacketPayload {
        public static Type<C2SPlayerLooking> TYPE = new Type<>(RunestonesMod.id("runestones_player_looking"));
        public static StreamCodec<FriendlyByteBuf, C2SPlayerLooking> CODEC =
            StreamCodec.of(C2SPlayerLooking::encode, C2SPlayerLooking::decode);

        public static void send(BlockPos pos) {
            ClientPlayNetworking.send(new C2SPlayerLooking(pos));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, C2SPlayerLooking self) {
            buf.writeBlockPos(self.pos());
        }

        private static C2SPlayerLooking decode(FriendlyByteBuf buf) {
            return new C2SPlayerLooking(buf.readBlockPos());
        }
    }

    // Server-to-client packet that tells the client the location that the player has just teleported to.
    public record S2CTeleportedLocation(RunestoneLocation location) implements CustomPacketPayload {
        public static Type<S2CTeleportedLocation> TYPE = new Type<>(RunestonesMod.id("runestones_teleported_location"));
        public static StreamCodec<FriendlyByteBuf, S2CTeleportedLocation> CODEC =
            StreamCodec.of(S2CTeleportedLocation::encode, S2CTeleportedLocation::decode);

        public static void send(ServerPlayer player, RunestoneLocation location) {
            ServerPlayNetworking.send(player, new S2CTeleportedLocation(location));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, S2CTeleportedLocation self) {
            buf.writeNbt(self.location.save());
        }

        private static S2CTeleportedLocation decode(FriendlyByteBuf buf) {
            return new S2CTeleportedLocation(RunestoneLocation.load(Objects.requireNonNull(buf.readNbt())));
        }
    }

    // Server-to-client packet that informs the client that a runestone wants to consume an item.
    public record S2CActivationWarmup(BlockPos runestonePos, Vec3 itemPos) implements CustomPacketPayload {
        public static Type<S2CActivationWarmup> TYPE = new Type<>(RunestonesMod.id("runestones_activation_warmup"));
        public static StreamCodec<FriendlyByteBuf, S2CActivationWarmup> CODEC =
            StreamCodec.of(S2CActivationWarmup::encode, S2CActivationWarmup::decode);

        public static void send(ServerPlayer player, BlockPos runestonePos, Vec3 itemPos) {
            ServerPlayNetworking.send(player, new S2CActivationWarmup(runestonePos, itemPos));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, S2CActivationWarmup self) {
            buf.writeBlockPos(self.runestonePos);
            buf.writeVec3(self.itemPos);
        }

        private static S2CActivationWarmup decode(FriendlyByteBuf buf) {
            return new S2CActivationWarmup(buf.readBlockPos(), buf.readVec3());
        }
    }

    // Server-to-client packet that contains a unique seed for the world.
    public record S2CUniqueWorldSeed(long seed) implements CustomPacketPayload {
        public static Type<S2CUniqueWorldSeed> TYPE = new Type<>(RunestonesMod.id("runestones_unique_world_seed"));
        public static StreamCodec<FriendlyByteBuf, S2CUniqueWorldSeed> CODEC =
            StreamCodec.of(S2CUniqueWorldSeed::encode, S2CUniqueWorldSeed::decode);

        public static void send(ServerPlayer player, long seed) {
            ServerPlayNetworking.send(player, new S2CUniqueWorldSeed(seed));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        private static void encode(FriendlyByteBuf buf, S2CUniqueWorldSeed self) {
            buf.writeLong(self.seed());
        }

        private static S2CUniqueWorldSeed decode(FriendlyByteBuf buf) {
            return new S2CUniqueWorldSeed(buf.readLong());
        }
    }
}
