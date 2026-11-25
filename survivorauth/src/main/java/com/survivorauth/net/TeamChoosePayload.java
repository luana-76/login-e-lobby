package com.survivorauth.net;

import com.survivorauth.SurvivorAuth;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Cliente -> servidor: time escolhido na tela de seleção (id: leste, oeste, norte ou sul). */
public record TeamChoosePayload(String team) implements CustomPacketPayload {
    public static final Type<TeamChoosePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SurvivorAuth.MODID, "team"));

    public static final StreamCodec<ByteBuf, TeamChoosePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(16), TeamChoosePayload::team,
            TeamChoosePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
