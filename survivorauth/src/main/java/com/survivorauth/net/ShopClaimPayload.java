package com.survivorauth.net;

import com.survivorauth.SurvivorAuth;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Cliente -> servidor: pede para receber/vestir uma roupa comprada na loja (id da loja, ex.: "vip_manto_sombrio"). */
public record ShopClaimPayload(String id) implements CustomPacketPayload {
    public static final Type<ShopClaimPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SurvivorAuth.MODID, "shop_claim"));

    public static final StreamCodec<ByteBuf, ShopClaimPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(48), ShopClaimPayload::id,
            ShopClaimPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
