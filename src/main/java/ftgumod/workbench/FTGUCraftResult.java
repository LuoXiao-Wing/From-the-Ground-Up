package ftgumod.workbench;

import javax.annotation.Nullable;
import ftgumod.event.PlayerLockEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class FTGUCraftResult extends InventoryCraftResult {

	private final EntityPlayer player;

	public FTGUCraftResult(EntityPlayer player) {
		this.player = player;
	}

	public void setInventorySlotContents(int slot, @Nullable ItemStack stack) {
		PlayerLockEvent event = new PlayerLockEvent(player, stack);
		if (stack != null)
			MinecraftForge.EVENT_BUS.post(event);

		super.setInventorySlotContents(slot, event.willLock() ? null : stack);
	}

}