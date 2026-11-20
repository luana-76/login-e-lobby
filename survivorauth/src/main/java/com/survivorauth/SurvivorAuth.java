package com.survivorauth;

import com.survivorauth.config.ClientConfig;
import com.survivorauth.config.ServerConfig;
import com.survivorauth.item.ModArmorMaterials;
import com.survivorauth.item.ModItems;
import com.survivorauth.net.NetworkSetup;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(SurvivorAuth.MODID)
public class SurvivorAuth {
    public static final String MODID = "survivorauth";

    public SurvivorAuth(IEventBus modBus, ModContainer container) {
        // world/serverconfig/survivorauth-server.toml
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
        // config/survivorauth-client.toml (cores e efeitos da tela)
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        // roupas VIP vestíveis (capacete, colete, calça, máscara, manto)
        ModArmorMaterials.MATERIALS.register(modBus);
        ModItems.ITEMS.register(modBus);
        modBus.addListener(ModItems::addToCreativeTab);

        modBus.addListener(NetworkSetup::register);
    }
}
