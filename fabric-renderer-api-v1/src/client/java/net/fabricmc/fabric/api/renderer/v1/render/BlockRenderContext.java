package net.fabricmc.fabric.api.renderer.v1.render;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

public interface BlockRenderContext extends RenderContext {
	BlockRenderView blockView();

	BlockState blockState();
	void pushBlockState(BlockState newState);
	void popBlockState();

	BlockPos blockPos();

	Random random();
}
