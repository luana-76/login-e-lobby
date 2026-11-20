package com.survivorauth.net;

import com.survivorauth.SurvivorAuth;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Cliente -> servidor: envio do formulário da tela (registrar ou entrar). */
public record AuthSubmitPayload(boolean register, String password, String confirm) implements CustomPacketPayload {
    public static final Type<AuthSubmitPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SurvivorAuth.MODID, "submit"));

    public static final StreamCodec<ByteBuf, AuthSubmitPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, AuthSubmitPayload::register,
            ByteBufCodecs.stringUtf8(64), AuthSubmitPayload::password,
            ByteBufCodecs.stringUtf8(64), AuthSubmitPayload::confirm,
            AuthSubmitPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
