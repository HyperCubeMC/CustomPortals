package dev.custom.portals.blocks;

import java.util.Random;

import dev.custom.portals.CustomPortals;
import dev.custom.portals.util.EntityMixinAccess;
import dev.custom.portals.data.Portal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.dimension.AreaHelper;
import net.minecraft.server.world.ServerWorld;

public class PortalBlock extends Block {
   public static final EnumProperty<Direction.Axis> AXIS;
   protected static final VoxelShape X_SHAPE;
   protected static final VoxelShape Z_SHAPE;
   protected static final VoxelShape Y_SHAPE;
    
   public PortalBlock(AbstractBlock.Settings settings) {
      super(settings);
      this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(AXIS, Direction.Axis.X));
   }

   @Override
   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
      switch((Direction.Axis)state.get(AXIS)) {
      case Z:
         return Z_SHAPE;
      case Y:
         return Y_SHAPE;
      case X:
      default:
         return X_SHAPE;
      }
   }

   @Override
   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
      Portal portal = CustomPortals.PORTALS.get(world).getPortalFromPos(pos);
      if(portal == null)
         return;
      if(portal.isInterdimensional()) {
         if (portal.getLinked().getDimensionId().equals("minecraft:the_nether") && world.getDimension().isNatural() && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && random.nextInt(2000) < world.getDifficulty().getId()) {
            while(world.getBlockState(pos).isOf(this)) {
               pos = pos.down();
            }
  
            if (world.getBlockState(pos).allowsSpawning(world, pos, EntityType.ZOMBIFIED_PIGLIN)) {
               Entity entity = EntityType.ZOMBIFIED_PIGLIN.spawn(world, (NbtCompound)null, (Text)null, (PlayerEntity)null, pos.up(), SpawnReason.STRUCTURE, false, false);
               if (entity != null) {
                  entity.resetNetherPortalCooldown();
               }
            }
         }
         if (portal.getLinked().getDimensionId().equals("minecraft:the_end") && world.getDimension().isNatural() && world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && random.nextInt(2000) < world.getDifficulty().getId()) {
            while(world.getBlockState(pos).isOf(this)) {
               pos = pos.down();
            }
  
            if (world.getBlockState(pos).allowsSpawning(world, pos, EntityType.ENDERMAN)) {
               Entity entity = EntityType.ENDERMAN.spawn(world, (NbtCompound)null, (Text)null, (PlayerEntity)null, pos.up(), SpawnReason.STRUCTURE, false, false);
               if (entity != null) {
                  entity.resetNetherPortalCooldown();
               }
            }
         }
      }
   }

   @Override
   public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
      super.onBreak(world, pos, state, player);
      Portal portal = CustomPortals.PORTALS.get(world).getPortalFromPos(pos);
      if(portal != null) {
         CustomPortals.PORTALS.get(world).unregisterPortal(portal);
         if(!world.isClient)
            CustomPortals.PORTALS.get(world).syncWithAll(((ServerWorld)world).getServer());
         //dropCatalyst(portal, world);
      }
   }

   private void dropCatalyst(Portal portal, World world) {
      Item catalyst;
      switch(this.getDefaultMapColor().id) {
         case 29: catalyst = CustomPortals.BLACK_PORTAL_CATALYST;
         break;
         case 25: catalyst = CustomPortals.BLUE_PORTAL_CATALYST;
         break;
         case 26: catalyst = CustomPortals.BROWN_PORTAL_CATALYST;
         break;
         case 23: catalyst = CustomPortals.CYAN_PORTAL_CATALYST;
         break;
         case 21: catalyst = CustomPortals.GRAY_PORTAL_CATALYST;
         break;
         case 27: catalyst = CustomPortals.GREEN_PORTAL_CATALYST;
         break;
         case 17: catalyst = CustomPortals.LIGHT_BLUE_PORTAL_CATALYST;
         break;
         case 22: catalyst = CustomPortals.LIGHT_GRAY_PORTAL_CATALYST;
         break;
         case 19: catalyst = CustomPortals.LIME_PORTAL_CATALYST;
         break;
         case 16: catalyst = CustomPortals.MAGENTA_PORTAL_CATALYST;
         break;
         case 15: catalyst = CustomPortals.ORANGE_PORTAL_CATALYST;
         break;
         case 20: catalyst = CustomPortals.PINK_PORTAL_CATALYST;
         break;
         case 24: catalyst = CustomPortals.PURPLE_PORTAL_CATALYST;
         break;
         case 28: catalyst = CustomPortals.RED_PORTAL_CATALYST;
         break;
         case 8: catalyst = CustomPortals.WHITE_PORTAL_CATALYST;
         break;
         case 18: 
         default: catalyst = CustomPortals.YELLOW_PORTAL_CATALYST;
      }
      ItemStack itemStack = new ItemStack(catalyst);
      Block.dropStack(world, portal.getSpawnPos(), itemStack);
   }
   
   @Override
   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
      Direction.Axis axis = direction.getAxis();
      Direction.Axis axis2 = (Direction.Axis)state.get(AXIS);
      boolean bl = axis2 == Direction.Axis.Y ? axis2 == axis && axis.isVertical() : axis2 != axis && axis.isHorizontal();
      if(!bl && !newState.isOf(this) && !(new AreaHelper(world, pos, axis2)).wasAlreadyValid()) {
         Portal portal = CustomPortals.PORTALS.get((World)world).getPortalFromPos(pos);
         if(portal != null) {
            CustomPortals.PORTALS.get((World)world).unregisterPortal(portal);
            if(!((World)world).isClient)
               CustomPortals.PORTALS.get((World)world).syncWithAll(((ServerWorld)world).getServer());
            dropCatalyst(portal, (World)world);
         }
         return Blocks.AIR.getDefaultState();
      }
      return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
   }
   
   @Override
   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
      Portal portal = CustomPortals.PORTALS.get(world).getPortalFromPos(pos);
      if(portal != null && entity.canUsePortals())
         ((EntityMixinAccess)entity).setInCustomPortal(portal);
      // For debugging purposes
      /*if(portal != null) {
         if(portal.hasLinked())
            System.out.println("Linked Portal at " + portal.getLinked().getSpawnPos().getX() + ", " + portal.getLinked().getSpawnPos().getY() + ", " + portal.getLinked().getSpawnPos().getZ());
         else System.out.println("Portal is not linked!");
      } else System.out.println("No portal found!");*/
   }
   
   @Environment(EnvType.CLIENT)
   public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
      if (random.nextInt(100) == 0) {
         world.playSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
      }

      Portal portal = CustomPortals.PORTALS.get(world).getPortalFromPos(pos);
      if(portal == null || !portal.hasLinked())
         return;
      for(int i = 0; i < 4; ++i) {
         double d = (double)pos.getX() + random.nextDouble();
         double e = (double)pos.getY() + random.nextDouble();
         double f = (double)pos.getZ() + random.nextDouble();
         double g = ((double)random.nextFloat() - 0.5D) * 0.5D;
         double h = ((double)random.nextFloat() - 0.5D) * 0.5D;
         double j = ((double)random.nextFloat() - 0.5D) * 0.5D;
         int k = random.nextInt(2) * 2 - 1;
         if (!world.getBlockState(pos.west()).isOf(this) && !world.getBlockState(pos.east()).isOf(this) && portal.length != 1) {
            d = (double)pos.getX() + 0.5D + 0.25D * (double)k;
            g = (double)(random.nextFloat() * 2.0F * (float)k);
         } 
         else if (!world.getBlockState(pos.up()).isOf(this) && !world.getBlockState(pos.down()).isOf(this)) {
            e = (double)pos.getY() + 0.5D + 0.25D * (double)k;
            h = (double)(random.nextFloat() * 2.0F * (float)k);
         } else {
            f = (double)pos.getZ() + 0.5D + 0.25D * (double)k;
            j = (double)(random.nextFloat() * 2.0F * (float)k);
         }
         switch(this.getDefaultMapColor().id) {
            case 29: world.addParticle(CustomPortals.BLACK_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 25: world.addParticle(CustomPortals.BLUE_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 26: world.addParticle(CustomPortals.BROWN_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 23: world.addParticle(CustomPortals.CYAN_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 21: world.addParticle(CustomPortals.GRAY_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 27: world.addParticle(CustomPortals.GREEN_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 17: world.addParticle(CustomPortals.LIGHT_BLUE_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 22: world.addParticle(CustomPortals.LIGHT_GRAY_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 19: world.addParticle(CustomPortals.LIME_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 16: world.addParticle(CustomPortals.MAGENTA_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 15: world.addParticle(CustomPortals.ORANGE_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 20: world.addParticle(CustomPortals.PINK_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 24: world.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, j);
            break;
            case 28: world.addParticle(CustomPortals.RED_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 8: world.addParticle(CustomPortals.WHITE_PORTAL_PARTICLE, d, e, f, g, h, j);
            break;
            case 18: world.addParticle(CustomPortals.YELLOW_PORTAL_PARTICLE, d, e, f, g, h, j);
         }
      }
   
   }
  
   @Environment(EnvType.CLIENT)
   public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
      return ItemStack.EMPTY;
   }
   
   @Override
   public BlockState rotate(BlockState state, BlockRotation rotation) {
      switch(rotation) {
         // use COUNTERCLOCKWISE_90 to rotate around Y axis
         case COUNTERCLOCKWISE_90:
            switch((Direction.Axis)state.get(AXIS)) {
               case Z:
               case X:
                  return (BlockState)state.with(AXIS, Direction.Axis.Y);
               case Y:
                  return (BlockState)state.with(AXIS, Direction.Axis.X);
               default:
                  return state;
            }
         case CLOCKWISE_90:
            switch((Direction.Axis)state.get(AXIS)) {
               case Z:
                  return (BlockState)state.with(AXIS, Direction.Axis.X);
               case X:
               case Y:
                  return (BlockState)state.with(AXIS, Direction.Axis.Z);
               default:
                  return state;
            }
      default:
         return state;
      }
   }
  
   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
      builder.add(AXIS);
   }

   static {
      AXIS = Properties.AXIS;
      X_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
      Z_SHAPE = Block.createCuboidShape(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
      Y_SHAPE = Block.createCuboidShape(0.0D, 6.0D, 0.0D, 16.0D, 10.0D, 16.0D);
   }
}