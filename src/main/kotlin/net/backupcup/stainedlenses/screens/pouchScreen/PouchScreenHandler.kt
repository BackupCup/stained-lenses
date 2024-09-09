package net.backupcup.stainedlenses.screens.pouchScreen

import net.backupcup.stainedlenses.items.LensItem
import net.backupcup.stainedlenses.items.ModifierUtil
import net.backupcup.stainedlenses.items.ModuleItem
import net.backupcup.stainedlenses.registry.RegisterScreenHandlers
import net.backupcup.stainedlenses.utils.DataHelper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerListener
import net.minecraft.screen.slot.Slot
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents


class PouchScreenHandler(
    syncId: Int,
    playerInventory: PlayerInventory,
    player: PlayerEntity,
    private val itemStack: ItemStack?
) : ScreenHandler(RegisterScreenHandlers.POUCH_SCREEN_HANDLER, syncId), ScreenHandlerListener {
    private val playerEntity: PlayerEntity
    private var inventory: Inventory = SimpleInventory(8)

    constructor(syncId: Int, playerInventory: PlayerInventory) :
            this(syncId, playerInventory, playerInventory.player, null) {
    }


    init {
        checkSize(inventory, 7)

        this.playerEntity = player
        this.inventory.onOpen(player)

        if (itemStack != null) for (i in 0..7) {
            this.inventory.setStack(i, DataHelper.retrieveStoredItemStack("Slot$i", this.itemStack))
        }

        for (m in 0 until 8) {
            this.addSlot(AttachementSlot(this.inventory, m, 13 + m * 18, 4))
        }

        for (m in 0 until 3) {
            for (l in 0 until 9) {
                this.addSlot(PouchLockedSlot(playerInventory, l + m * 9 + 9, 4 + l * 18, 33 + m * 19, itemStack))
            }
        }
        for (m in 0 until 9) {
            this.addSlot(PouchLockedSlot(playerInventory, m, 4 + m * 18, 97, itemStack))
        }

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

    override fun onSlotUpdate(handler: ScreenHandler, slotId: Int, stack: ItemStack) {
        if (this.itemStack != null && slotId >= 0 && slotId < 8) {
            DataHelper.storeItemStackInAnother("Slot$slotId", stack, this.itemStack)
        }
        this.slots[slotId].markDirty()
    }

    override fun onPropertyUpdate(handler: ScreenHandler, property: Int, value: Int) {}

    override fun onClosed(player: PlayerEntity) {
        player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 1.0f, 1.0f)
    }
}

class PouchLockedSlot(
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

class AttachementSlot(
    inventory: Inventory?,
    index: Int,
    x: Int, y: Int
) : Slot(inventory, index, x, y) {
    override fun canInsert(stack: ItemStack): Boolean {
        return stackMovementIsAllowed(stack)
    }

    private fun stackMovementIsAllowed(stack: ItemStack): Boolean {
        return stack.item is ModifierUtil
    }
}