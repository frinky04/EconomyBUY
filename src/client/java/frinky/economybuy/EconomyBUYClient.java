package frinky.economybuy;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;

public class EconomyBUYClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(EB_Entities.TRADER_ENTITY, (context) -> new LivingEntityRenderer<>(context, new PlayerEntityModel(context.getPart(EntityModelLayers.PLAYER), false), 0.5f) {
            @Override
            public PlayerEntityRenderState createRenderState() {
                return new PlayerEntityRenderState();
            }

            @Override
            public Identifier getTexture(PlayerEntityRenderState state) {
                return Identifier.of(EconomyBUY.MOD_ID, "textures/entity/prapor.png");
            }

        });
	}
}