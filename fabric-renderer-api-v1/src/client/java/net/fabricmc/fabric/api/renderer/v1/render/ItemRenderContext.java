package net.fabricmc.fabric.api.renderer.v1.render;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

public interface ItemRenderContext extends RenderContext {
	ItemStack itemStack();

	Random random();
}
