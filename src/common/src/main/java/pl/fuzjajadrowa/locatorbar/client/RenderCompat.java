package pl.fuzjajadrowa.locatorbar.client;

//? if <1.21.11
/*import com.mojang.blaze3d.systems.RenderSystem;*/
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=1.21.11
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

final class RenderCompat {
    private RenderCompat() {
    }

    static void push(GuiGraphicsExtractor guiGraphics) {
        //? if >=1.21.11
        guiGraphics.pose().pushMatrix();
        //? if <1.21.11
        /*guiGraphics.pose().pushPose();*/
    }

    static void pop(GuiGraphicsExtractor guiGraphics) {
        //? if >=1.21.11
        guiGraphics.pose().popMatrix();
        //? if <1.21.11
        /*guiGraphics.pose().popPose();*/
    }

    static void translate(GuiGraphicsExtractor guiGraphics, float x, float y) {
        //? if >=1.21.11
        guiGraphics.pose().translate(x, y);
        //? if <1.21.11
        /*guiGraphics.pose().translate(x, y, 0.0F);*/
    }

    static void scale(GuiGraphicsExtractor guiGraphics, float x, float y) {
        //? if >=1.21.11
        guiGraphics.pose().scale(x, y);
        //? if <1.21.11
        /*guiGraphics.pose().scale(x, y, 1.0F);*/
    }

    static void blit(
            GuiGraphicsExtractor guiGraphics,
            Identifier texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height,
            int textureWidth,
            int textureHeight
    ) {
        //? if >=1.21.11 {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
        //?} else
        /*guiGraphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);*/
    }

    static void blitRegion(
            GuiGraphicsExtractor guiGraphics,
            Identifier texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height,
            int regionWidth,
            int regionHeight,
            int textureWidth,
            int textureHeight
    ) {
        //? if >=1.21.11 {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight);
        //?} else
        /*guiGraphics.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);*/
    }

    static void blitTinted(
            GuiGraphicsExtractor guiGraphics,
            Identifier texture,
            int x,
            int y,
            int u,
            int v,
            int width,
            int height,
            int regionWidth,
            int regionHeight,
            int textureWidth,
            int textureHeight,
            int tint
    ) {
        //? if >=1.21.11 {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, tint);
        //?} else {
        /*float red = ((tint >> 16) & 0xFF) / 255.0F;
        float green = ((tint >> 8) & 0xFF) / 255.0F;
        float blue = (tint & 0xFF) / 255.0F;
        float alpha = ((tint >> 24) & 0xFF) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(red, green, blue, alpha);
        guiGraphics.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        *///?}
    }

    static void blitPlayerHead(GuiGraphicsExtractor guiGraphics, Identifier texture, int x, int y, int size, int tint) {
        //? if <1.21.11 {
        /*float alpha = ((tint >> 24) & 0xFF) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        *///?}
        blitTinted(guiGraphics, texture, x, y, 8, 8, size, size, 8, 8, 64, 64, tint);
        blitTinted(guiGraphics, texture, x, y, 40, 8, size, size, 8, 8, 64, 64, tint);
        //? if <1.21.11 {
        /*RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        *///?}
    }

    static void text(GuiGraphicsExtractor guiGraphics, String text, int x, int y, int color, boolean shadow) {
        guiGraphics.text(Minecraft.getInstance().font, text, x, y, color, shadow);
    }
}
