package svenhjol.strange.runestones.gen;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class GeodesConfig implements IFeatureConfig {
   public final BlockState state;

   public GeodesConfig(BlockState state) {
      this.state = state;
   }

   public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
      return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(ops.createString("state"), BlockState.serialize(ops, this.state).getValue())));
   }

   public static <T> GeodesConfig deserialize(Dynamic<T> p_214712_0_) {
      BlockState blockstate = p_214712_0_.get("state").map(BlockState::deserialize).orElse(Blocks.END_STONE.getDefaultState());
      return new GeodesConfig(blockstate);
   }
}