package epicsquid.roots.entity.ritual;

import epicsquid.roots.ritual.RitualRegistry;
import epicsquid.roots.ritual.RitualSpreadingForest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
public class SpreadingForestRitualEntity extends BaseRitualEntity {
  private RitualSpreadingForest ritual;

  private Set<BlockState> saplingBlocks = new HashSet<>();
  private Set<Block> saplings = new HashSet<>();

  private static int SAPLING_OREDICT = -1;

  public SpreadingForestRitualEntity(EntityType<?> entityTypeIn, World worldIn) {
    super(entityTypeIn, worldIn);
    ritual = (RitualSpreadingForest) RitualRegistry.ritual_spreading_forest;
  }

  @Override
  protected void registerData() {
    getDataManager().register(lifetime, RitualRegistry.ritual_spreading_forest.getDuration() + 20);
  }

  private boolean canTwoByTwo(World world, BlockPos pos1, int offsetX, int offsetZ) {
    BlockPos pos2 = pos1.add(offsetX, 0, offsetZ);
    BlockPos pos3 = pos1.add(offsetX + 1, 0, offsetZ);
    BlockPos pos4 = pos1.add(offsetX + 1, 0, offsetZ + 1);
    return validSpot(world, pos1) && validSpot(world, pos2) && validSpot(world, pos3) && validSpot(world, pos4);
  }

  private boolean validSpot(World world, BlockPos pos) {
    return validSpot(world, pos, null);
  }

  private boolean validSpot(World world, BlockPos pos, @Nullable BlockState plantableState) {
    BlockState state = world.getBlockState(pos);
    Block block = state.getBlock();
    // TODO: Add an additional whitelist block
    IPlantable plantable;
    if (plantableState == null || !(plantableState.getBlock() instanceof IPlantable)) {
      plantable = (IPlantable) Blocks.OAK_SAPLING;
    } else {
      plantable = (IPlantable) plantableState.getBlock();
    }
    return world.isAirBlock(pos.up()) && block.canSustainPlant(state, world, pos, Direction.UP, plantable);
  }


  @Override
  public void tick() {
    super.tick();
/*    if (SAPLING_OREDICT == -1) {
      SAPLING_OREDICT = OreDictionary.getOreID("treeSapling");
    }

    float alpha = (float) Math.min(40, (RitualRegistry.ritual_spreading_forest.getDuration() + 20) - getDataManager().get(lifetime)) / 40.0f;

    if (world.isRemote && getDataManager().get(lifetime) > 0) {
      ParticleUtil.spawnParticleStar(world, (float) posX, (float) posY, (float) posZ, 0, 0, 0, 150, 255, 100, 0.5f * alpha, 20.0f, 40);
      if (rand.nextInt(5) == 0) {
        ParticleUtil.spawnParticleSpark(world, (float) posX, (float) posY, (float) posZ, 0.125f * (rand.nextFloat() - 0.5f), 0.0625f * (rand.nextFloat()),
            0.125f * (rand.nextFloat() - 0.5f), 100, 255, 50, 1.0f * alpha, 1.0f + rand.nextFloat(), 160);
      }
      if (rand.nextInt(2) == 0) {
        for (float i = 0; i < 360; i += rand.nextFloat() * 120.0f) {
          float tx = (float) posX + 2.0f * (float) Math.sin(Math.toRadians(i));
          float ty = (float) posY;
          float tz = (float) posZ + 2.0f * (float) Math.cos(Math.toRadians(i));
          for (int j = 0; j < 4; j++) {
            ParticleUtil.spawnParticleStar(world, tx, ty + 0.2f, tz, 0, rand.nextFloat() * 0.125f, 0, 100, 255, 50, 0.5f * alpha, 5.0f + rand.nextFloat() * 5.0f, 90);
          }
        }
      }
    }
    if (saplingBlocks.isEmpty()) {
      List<BlockPos> positions = Util.getBlocksWithinRadius(world, getPosition(), ritual.radius_x, ritual.radius_y, ritual.radius_z, (BlockPos pos) -> world.getBlockState(pos).getBlock() instanceof LeavesBlock);
      if (!positions.isEmpty()) {
        for (BlockPos pos : positions) {
          BlockState state = world.getBlockState(pos);
          Block block = state.getBlock();
          List<ItemStack> drops = block.getDrops(world, pos, state, 0);
          for (ItemStack s : drops) {
            if (ConfigUtil.setContainsItemStack(GeneralConfig.getSaplingBlacklist(), s)) {
              continue;
            }

            if (s.getItem() instanceof BlockItem) {
              if (((BlockItem) s.getItem()).getBlock() instanceof SaplingBlock) {
                Block sap = ((BlockItem) s.getItem()).getBlock();
                saplings.add(sap);
                saplingBlocks.add(sap.getStateFromMeta(s.getMetadata()));
              } else {
                boolean isSapling = false;
                for (int id : OreDictionary.getOreIDs(s)) {
                  if (id == SAPLING_OREDICT && id != -1) {
                    isSapling = true;
                    break;
                  }
                }
                if (isSapling) {
                  Block sap = ((BlockItem) s.getItem()).getBlock();
                  saplingBlocks.add(sap.getStateFromMeta(s.getMetadata()));
                  saplings.add(sap);
                }
              }
            }
          }
        }
      }
      List<BlockPos> positions2 = Util.getBlocksWithinRadius(world, getPosition(), ritual.radius_x, ritual.radius_y, ritual.radius_z, (BlockPos pos) -> {
        if (world.isAirBlock(pos)) return false;
        return world.getBlockState(pos).getBlock() instanceof IGrowable;
      });
      if (!positions2.isEmpty()) {
        for (BlockPos pos : positions) {
          BlockState state = world.getBlockState(pos);
          Block block = state.getBlock();
          ItemStack stack = block.getItem(world, pos, state);
          if (ConfigUtil.setContainsItemStack(GeneralConfig.getSaplingBlacklist(), stack)) {
            continue;
          }
          if (state.getPropertyKeys().contains(SaplingBlock.STAGE)) {
            saplingBlocks.add(state);
            saplings.add(state.getBlock());
          } else {
            for (int id : OreDictionary.getOreIDs(stack)) {
              if (id == SAPLING_OREDICT && id != -1) {
                saplingBlocks.add(state);
                saplings.add(block);
              }
            }
          }
        }
      }
    }

    if (ticksExisted % ritual.place_interval == 0 && !saplingBlocks.isEmpty()) {
      BlockState state = Lists.newArrayList(saplingBlocks).get(rand.nextInt(saplingBlocks.size()));

      List<BlockPos> positions = Util.getBlocksWithinRadius(world, getPosition(), ritual.radius_x, ritual.radius_y, ritual.radius_z, pos -> this.validSpot(world, pos, state));
      if (!positions.isEmpty() && !world.isRemote) {
        boolean planted = false;
        boolean twoByTwo = false;
        BlockPlanks.EnumType type = null;
        if (state.getPropertyKeys().contains(SaplingBlock.TYPE)) {
          type = state.getValue(SaplingBlock.TYPE);
          if (type == BlockPlanks.EnumType.DARK_OAK || ((type == BlockPlanks.EnumType.JUNGLE || type == BlockPlanks.EnumType.SPRUCE) && rand.nextInt(ritual.double_chance) == 0)) {
            twoByTwo = true;
          }
        } else if (ConfigUtil.setContainsItemStack(GeneralConfig.getTwoByTwoSaplings(), state.getBlock().getItem(world, BlockPos.ORIGIN, state))) {
          twoByTwo = true;
        }
        if (twoByTwo) {
          // Plant 2-by-2
          boolean validPos = false;
          BlockPos pos = null;
          int count = 30;
          int i = 0;
          int j = 0;
          planter:
          while (count >= 0) {
            pos = positions.get(rand.nextInt(positions.size()));
            for (i = 0; i >= -1; --i) {
              for (j = 0; j >= -1; --j) {
                if (this.canTwoByTwo(world, pos, i, j)) {
                  validPos = true;
                  break planter;
                }
              }
            }
            count--;
          }
          if (!validPos && type != BlockPlanks.EnumType.DARK_OAK) {
            planted = true;
          }
          if (validPos) {
            world.setBlockState(pos.up(), state);
            BlockPos pos2 = pos.up().add(i, 0, j + 1);
            world.setBlockState(pos2, state);
            BlockPos pos3 = pos.up().add(i + 1, 0, j);
            world.setBlockState(pos3, state);
            BlockPos pos4 = pos.up().add(i + 1, 0, j + 1);
            world.setBlockState(pos4, state);
            planted = true;
          }
        }
        if (!planted) {
          BlockPos position = positions.get(rand.nextInt(positions.size()));
          world.setBlockState(position.up(), state);
        }
      }
    }
    if (ticksExisted % ritual.growth_interval == 0) {
      List<BlockPos> positions = Util.getBlocksWithinRadius(world, getPosition(), ritual.radius_x, ritual.radius_y, ritual.radius_z, (BlockPos pos) -> {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof SaplingBlock || saplings.contains(block)) return true;

        return state.getPropertyKeys().contains(SaplingBlock.STAGE);
      });
      if (!positions.isEmpty() && !world.isRemote) {
        BlockPos pos = positions.get(rand.nextInt(positions.size()));
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof IGrowable) {
          ((IGrowable) state.getBlock()).grow(world, rand, pos, state);
        } else {
          Roots.logger.error("'Sapling' at " + pos.toString() + " is not IGrowable!");
        }
      }
    }*/
  }
}