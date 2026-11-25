package com.survivorauth.net;

import com.survivorauth.SurvivorAuth;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Servidor -> cliente: quantidade real de contas vinculadas a cada time. */
public record TeamCountsPayload(int leste, int oeste, int norte, int sul) implements CustomPacketPayload {
    public static final Type<TeamCountsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SurvivorAuth.MODID, "team_counts"));
    public static final StreamCodec<ByteBuf, TeamCountsPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TeamCountsPayload::leste,
            ByteBufCodecs.VAR_INT, TeamCountsPayload::oeste,
            ByteBufCodecs.VAR_INT, TeamCountsPayload::norte,
            ByteBufCodecs.VAR_INT, TeamCountsPayload::sul,
            TeamCountsPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
