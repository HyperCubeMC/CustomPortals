package dev.custom.portals.mixin;

import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import dev.custom.portals.CustomPortals;
import dev.custom.portals.util.EntityMixinAccess;

import org.spongepowered.asm.mixin.Final;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    @Final
    @Shadow
    private MinecraftClient client;
    @Shadow
    private int scaledWidth;
    @Shadow
    private int scaledHeight;

    @Inject(method = "renderPortalOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/Sprite;getMinU()F"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void renderPortalOverlay(float f, CallbackInfo ci, Sprite sprite) {
        int color = ((EntityMixinAccess)this.client.player).getPortalColor();
        if (color != 0) {
            Block spriteModel = switch (color) {
                case 29 -> CustomPortals.BLACK_PORTAL;
                case 25 -> CustomPortals.BLUE_PORTAL;
                case 26 -> CustomPortals.BROWN_PORTAL;
                case 23 -> CustomPortals.CYAN_PORTAL;
                case 21 -> CustomPortals.GRAY_PORTAL;
                case 27 -> CustomPortals.GREEN_PORTAL;
                case 17 -> CustomPortals.LIGHT_BLUE_PORTAL;
                case 22 -> CustomPortals.LIGHT_GRAY_PORTAL;
                case 19 -> CustomPortals.LIME_PORTAL;
                case 16 -> CustomPortals.MAGENTA_PORTAL;
                case 15 -> CustomPortals.ORANGE_PORTAL;
                case 20 -> CustomPortals.PINK_PORTAL;
                case 24 -> CustomPortals.PURPLE_PORTAL;
                case 28 -> CustomPortals.RED_PORTAL;
                case 8 -> CustomPortals.WHITE_PORTAL;
                case 18 -> CustomPortals.YELLOW_PORTAL;
                default -> Blocks.NETHER_PORTAL;
            };
            sprite = this.client.getBlockRenderManager().getModels().getSprite(spriteModel.getDefaultState());
            float g = sprite.getMinU();
            float h = sprite.getMinV();
            float i = sprite.getMaxU();
            float j = sprite.getMaxV();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            bufferBuilder.vertex(0.0D, (double)this.scaledHeight, -90.0D).texture(g, j).next();
            bufferBuilder.vertex((double)this.scaledWidth, (double)this.scaledHeight, -90.0D).texture(i, j).next();
            bufferBuilder.vertex((double)this.scaledWidth, 0.0D, -90.0D).texture(i, h).next();
            bufferBuilder.vertex(0.0D, 0.0D, -90.0D).texture(g, h).next();
            tessellator.draw();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            ci.cancel();
        }
    }
}
