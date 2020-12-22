/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.lookup.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;

public class BlockApiCacheImpl<T, C> implements BlockApiCache<T, C> {
	private final BlockApiLookupImpl<T, C> lookup;
	private final ServerWorld world;
	private final BlockPos pos;
	// We always cache the block entity, even if it's null. We rely on BE load and unload events to invalidate the cache when necessary.
	// blockEntityCacheValid maintains whether the cache is valid or not.
	private boolean blockEntityCacheValid = false;
	private BlockEntity cachedBlockEntity = null;
	// We also cache the BlockApiProvider at the target position. We check if the block state has changed to invalidate the cache.
	// lastState maintains for which block state the cachedProvider is valid.
	private BlockState lastState = null;
	private BlockApiLookup.BlockApiProvider<T, C> cachedProvider = null;

	public BlockApiCacheImpl(BlockApiLookupImpl<T, C> lookup, ServerWorld world, BlockPos pos) {
		((ServerWorldCache) world).api_provider_registerCache(pos, this);
		this.lookup = lookup;
		this.world = world;
		this.pos = pos.toImmutable();
	}

	public void invalidate() {
		blockEntityCacheValid = false;
		cachedBlockEntity = null;
		lastState = null;
		cachedProvider = null;
	}

	@Nullable
	@Override
	public T get(@Nullable BlockState state, C context) {
		// Get block entity
		if (!blockEntityCacheValid) {
			cachedBlockEntity = world.getBlockEntity(pos);
			blockEntityCacheValid = true;
		}

		// Get block state
		if (state == null) {
			if (cachedBlockEntity != null) {
				state = cachedBlockEntity.getCachedState();
			} else {
				state = world.getBlockState(pos);
			}
		}

		// Get provider
		if (lastState != state) {
			cachedProvider = lookup.getProvider(state.getBlock());
			lastState = state;
		}

		// Query the provider
		T instance = null;

		if (cachedProvider != null) {
			if (cachedProvider instanceof BlockApiLookupImpl.WrappedBlockEntityProvider) {
				instance = ((BlockApiLookupImpl.WrappedBlockEntityProvider<T, C>) cachedProvider).blockEntityProvider.get(cachedBlockEntity, context);
			} else {
				instance = cachedProvider.get(world, pos, state, context);
			}
		}

		if (instance != null) {
			return instance;
		}

		// Query the fallback providers
		for (BlockApiLookup.FallbackApiProvider<T, C> fallbackProvider : lookup.getFallbackProviders()) {
			instance = fallbackProvider.get(world, pos, state, cachedBlockEntity, context);

			if (instance != null) {
				return instance;
			}
		}

		return null;
	}

	static {
		ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, world) -> {
			((ServerWorldCache) world).api_provider_invalidateCache(blockEntity.getPos());
		});
		ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, world) -> {
			((ServerWorldCache) world).api_provider_invalidateCache(blockEntity.getPos());
		});
	}
}
