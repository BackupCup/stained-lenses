package net.backupcup.stainedlenses.screens.spyglassScreen

import net.backupcup.stainedlenses.items.LensItem
import net.backupcup.stainedlenses.items.ModifierUtil
import net.backupcup.stainedlenses.items.ModuleItem
import net.backupcup.stainedlenses.items.PouchItem
import net.backupcup.stainedlenses.registry.RegisterItems
import net.backupcup.stainedlenses.registry.RegisterPackets
import net.backupcup.stainedlenses.registry.RegisterScreenHandlers
import net.backupcup.stainedlenses.utils.DataHelper
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerListener
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand


class SpyglassScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    player: PlayerEntity,
    handlerContext: ScreenHandlerContext,
    itemStack: ItemStack?
) : ScreenHandler(RegisterScreenHandlers.SPYGLASS_SCREEN_HANDLER, syncId), ScreenHandlerListener {
    private val playerEntity: PlayerEntity
    private var inventory: Inventory = SimpleInventory(11)

    constructor(syncId: Int, playerInventory: PlayerInventory) : this(syncId, playerInventory, playerInventory.player, ScreenHandlerContext.EMPTY, null)

    init {
        checkSize(inventory, 10)

        this.playerEntity = player
        this.inventory.onOpen(player)

        if (itemStack != null) {
            this.inventory.setStack(0, DataHelper.getLensStack(itemStack))
            this.inventory.setStack(1, DataHelper.getModuleStack(itemStack))
            this.inventory.setStack(2, DataHelper.getPouchStack(itemStack))

            if (this.inventory.getStack(2).isOf(RegisterItems.ATTACHEMENT_POUCH)) {
                for (i in 0 until 8) this.inventory.setStack(3+i, DataHelper.retrieveStoredItemStack("Slot$i", this.inventory.getStack(2)))
            }
        }

        this.addSlot(object: Slot(this.inventory, 0, 116, 75) {
            override fun canInsert(stack: ItemStack): Boolean { return stack.item is LensItem }
            override fun getMaxItemCount(): Int { return 1 }
        })
        this.addSlot(object: Slot(this.inventory, 1, 142, 75) {
            override fun canInsert(stack: ItemStack): Boolean { return stack.item is ModuleItem }
            override fun getMaxItemCount(): Int { return 1 }
        })
        this.addSlot(object: Slot(this.inventory, 2, 90, 75) {
            override fun canInsert(stack: ItemStack): Boolean { return stack.item is PouchItem }
            override fun getMaxItemCount(): Int { return 1 }
        })

        for (m in 0 until 4) {
            for (l in 0 until 2) {
                this.addSlot(object: Slot(this.inventory, 3 + l + m * 2, 176 + 18 * l, 102 + 20 * m) {
                    override fun canInsert(stack: ItemStack): Boolean { return stack.item is ModifierUtil }
                    override fun getMaxItemCount(): Int { return 1 }
                    override fun isEnabled(): Boolean { return inventory.getStack(2).isOf(RegisterItems.ATTACHEMENT_POUCH) }
                })
            }
        }

        for (m in 0 until 3) {
            for (l in 0 until 9) {
                this.addSlot(SpyglassLockedSlot(playerInventory, l + m * 9 + 9, 6 + l * 18, 100 + m * 19, itemStack))
            }
        }
        for (m in 0 until 9) {
            this.addSlot(SpyglassLockedSlot(playerInventory, m, 6 + m * 18, 164, itemStack))
        }

        addListener(this.generatorListener(handlerContext, player))
        addListener(this)
        sendContentUpdates()
    }

    override fun quickMove(player: PlayerEntity?, invSlot: Int): ItemStack {
        var newStack: ItemStack = ItemStack.EMPTY
        val slot: Slot = this.slots[invSlot]

        if (slot.hasStack()) {
            val originalStack: ItemStack = slot.stack
            newStack = originalStack.copy()
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size, true)) {
                    return ItemStack.EMPTY
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY
            }

            if (originalStack.isEmpty) {
                slot.stack = ItemStack.EMPTY
            } else {
                slot.markDirty()
            }
        }

        return newStack
    }

    override fun canUse(player: PlayerEntity?): Boolean {
        return inventory.canPlayerUse(player)
    }

    private fun generatorListener(context: ScreenHandlerContext, player: PlayerEntity): ScreenHandlerListener {
        return object : ScreenHandlerListener {
            override fun onPropertyUpdate(handler: ScreenHandler, property: Int, value: Int) {}
            override fun onSlotUpdate(handler: ScreenHandler, slotId: Int, stack: ItemStack) {
                val item = handler.getSlot(1).stack
                if (!item.isEmpty) {
                    context.run { _, _ ->
                        sendItemPacket()
                        updateSpyglassData()
                    }
                }
            }
        }
    }

    override fun onSlotUpdate(handler: ScreenHandler, slotId: Int, stack: ItemStack) {
        if (slotId == 2 && this.inventory.getStack(2).isEmpty) {
            for (i in 0 until 8) this.inventory.setStack(i + 3, ItemStack.EMPTY)
        }

        if (slotId == 2 && this.inventory.getStack(2).isOf(RegisterItems.ATTACHEMENT_POUCH)) {
            for (i in 0 until 8) this.inventory.setStack(i + 3, DataHelper.retrieveStoredItemStack("Slot$i", this.inventory.getStack(2)))
        }

        if (this.inventory.getStack(2).isOf(RegisterItems.ATTACHEMENT_POUCH) && slotId > 2 &&slotId <= 10)
            DataHelper.storeItemStackInAnother("Slot${slotId-3}", this.inventory.getStack(slotId), this.inventory.getStack(2))

        sendItemPacket()
        updateSpyglassData()
    }

    override fun onPropertyUpdate(handler: ScreenHandler?, property: Int, value: Int) {}

    override fun onClosed(player: PlayerEntity) {
        player.playSound(SoundEvents.ITEM_SPYGLASS_STOP_USING, SoundCategory.PLAYERS, 1.0f, 1.0f)
    }

    private fun sendItemPacket() {
        if (playerEntity.entityWorld.isClient) return

        val buf = PacketByteBufs.create()
        buf.writeItemStack(this.inventory.getStack(0))
        buf.writeItemStack(this.inventory.getStack(1))
        buf.writeItemStack(this.inventory.getStack(2))

        ServerPlayNetworking.send(getServerPlayer(), RegisterPackets.SPYGLASS_UI_UPDATE, buf)
    }

    private fun updateSpyglassData() {
        DataHelper.storeItemStackInAnother("lensSlot", this.inventory.getStack(0), playerEntity.getStackInHand(Hand.MAIN_HAND))
        DataHelper.storeItemStackInAnother("moduleSlot", this.inventory.getStack(1), playerEntity.getStackInHand(Hand.MAIN_HAND))
        DataHelper.storeItemStackInAnother("pouchSlot", this.inventory.getStack(2), playerEntity.getStackInHand(Hand.MAIN_HAND))
    }

    private fun getServerPlayer(): ServerPlayerEntity? {
        return this.playerEntity.world.server?.playerManager?.getPlayer(playerEntity.uuid)
    }
}

class SpyglassLockedSlot(
    inventory: Inventory?,
    index: Int,
    x: Int, y: Int,
    private val itemStack: ItemStack?
) : Slot(inventory, index, x, y) {
    override fun canInsert(stack: ItemStack): Boolean {
        return stackMovementIsAllowed(stack)
    }

    override fun canTakeItems(playerEntity: PlayerEntity?): Boolean {
        return stackMovementIsAllowed(stack)
    }

    private fun stackMovementIsAllowed(stack: ItemStack): Boolean {
        return stack != itemStack
    }
}