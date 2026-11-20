package com.survivorauth.net;

import com.survivorauth.SurvivorAuth;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Servidor -> cliente: estado da autenticação (ver {@link AuthState}). */
public record AuthStatePayload(int state, int arg, int seconds) implements CustomPacketPayload {
    public static final Type<AuthStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SurvivorAuth.MODID, "state"));

    public static final StreamCodec<ByteBuf, AuthStatePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AuthStatePayload::state,
            ByteBufCodecs.VAR_INT, AuthStatePayload::arg,
            ByteBufCodecs.VAR_INT, AuthStatePayload::seconds,
            AuthStatePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
