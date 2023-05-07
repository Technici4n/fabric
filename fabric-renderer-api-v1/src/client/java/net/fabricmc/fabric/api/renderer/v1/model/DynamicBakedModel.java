package net.fabricmc.fabric.api.renderer.v1.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

import net.fabricmc.fabric.api.renderer.v1.render.BlockRenderContext;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderContext;

/**
 * Base interface for models that want to use advanced rendering provided by the Renderer API.
 * This interface re-abstracts the advanced methods, and provides default implementation for the vanilla simpler methods.
 */
public interface DynamicBakedModel extends BakedModel {
	/**
	 * Re-abstracted because modders must implement this.
	 */
	@Override
	void emitBlockQuads(BlockRenderContext blockContext);

	/**
	 * Re-abstracted because modders must implement this.
	 */
	@Override
	void emitItemQuads(ItemRenderContext itemContext);

	/**
	 * Should not be used by mods, it is entirely superseded by {@link #emitBlockQuads} and {@link #emitItemQuads}.
	 */
	@Override
	default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
		return List.of();
	}

	/**
	 * Never a builtin model if mods override this.
	 */
	@Override
	default boolean isBuiltin() {
		return false;
	}
}
