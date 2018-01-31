package ftgumod.item;

import ftgumod.Content;
import ftgumod.api.util.BlockSerializable;
import ftgumod.event.PlayerInspectEvent;
import ftgumod.technology.Technology;
import ftgumod.technology.TechnologyManager;
import ftgumod.util.StackUtils;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ItemMagnifyingGlass extends Item {

	public ItemMagnifyingGlass(String name) {
		setUnlocalizedName(name);
		setCreativeTab(CreativeTabs.TOOLS);
		setMaxStackSize(1);
	}

	public static List<BlockSerializable> getInspected(ItemStack item) {
		List<BlockSerializable> list = new LinkedList<>();
		NBTTagList blocks = StackUtils.INSTANCE.getItemData(item).getTagList("FTGU", NBT.TAG_COMPOUND);
		for (int i = 0; i < blocks.tagCount(); i++)
			list.add(new BlockSerializable(blocks.getCompoundTagAt(i)));
		return list;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float f1, float f2, float f3) {
		if (player.isSneaking()) {
			if (!world.isRemote) {
				ItemStack item = hand == EnumHand.MAIN_HAND ? player.getHeldItemMainhand() : player.getHeldItemOffhand();
				List<BlockSerializable> list = getInspected(item);

				IBlockState state = world.getBlockState(pos);
				BlockSerializable block = new BlockSerializable(world, pos, state, player);
				List<BlockSerializable> singleton = Collections.singletonList(block);

				PlayerInspectEvent event = new PlayerInspectEvent(player, hand, pos, state, face);
				event.setCanceled(true);

				loop:
				for (Technology tech : TechnologyManager.INSTANCE)
					if (tech.hasResearchRecipe() && tech.canResearch(player))
						for (int i = 0; i < 9; i++)
							if (tech.getResearchRecipe().testDecipher(i, singleton) && !tech.getResearchRecipe().testDecipher(i, list)) {
								event.setCanceled(false);
								break loop;
							}

				MinecraftForge.EVENT_BUS.post(event);
				Content.c_inspect.trigger((EntityPlayerMP) player, pos, state, !event.isCanceled());

				if (event.isCanceled()) {
					player.sendMessage(new TextComponentTranslation("technology.decipher.understand"));
					SoundType sound = state.getBlock().getSoundType(state, world, pos, player);
					world.playSound(null, pos, sound.getHitSound(), SoundCategory.NEUTRAL, (sound.getVolume() + 1.0F) / 4.0F, sound.getPitch() * 0.5F);
				} else {
					player.sendMessage(new TextComponentTranslation("technology.decipher.flawless"));
					world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 0.75F, 1.0F);

					NBTTagCompound tag = StackUtils.INSTANCE.getItemData(item);
					NBTTagList nbt = tag.getTagList("FTGU", NBT.TAG_COMPOUND);
					nbt.appendTag(block.serialize());
					tag.setTag("FTGU", nbt);
				}
			}
			return EnumActionResult.SUCCESS;
		} else
			return EnumActionResult.PASS;
	}

}