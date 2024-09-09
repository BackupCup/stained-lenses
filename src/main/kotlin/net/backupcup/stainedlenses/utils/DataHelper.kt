package net.backupcup.stainedlenses.utils

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound

object DataHelper {
    /**
     * Converts the entirety of [itemStack] into [NbtCompound]
     * @param itemStack [ItemStack] that needs to be serialized
     * @return [NbtCompound] of the [itemStack]
     */
    private fun serializeItemStack(itemStack: ItemStack): NbtCompound {
        val nbtCompound = NbtCompound()
        itemStack.writeNbt(nbtCompound)
        return nbtCompound
    }

    /**
     * Deconverts the [nbtCompound] into an [ItemStack]
     * @param nbtCompound [NbtCompound] that needs to be deserialized
     * @return [ItemStack] made from [nbtCompound]
     */
    private fun deserializeItemStack(nbtCompound: NbtCompound?): ItemStack {
        return ItemStack.fromNbt(nbtCompound)
    }

    /**
     * Stores the entire [ItemStack] data of the [source] item into a special field in the [target] item's NBT
     * @param key Name of the special field
     * @param source [ItemStack] that needs to be stored
     * @param target [ItemStack] in which the data will be stored
     */
    fun storeItemStackInAnother(key: String, source: ItemStack, target: ItemStack) {
        val sourceNbt = serializeItemStack(source)
        val targetNbt = target.getOrCreateNbt()
        targetNbt.put(key, sourceNbt)
    }

    /**
     * Retrieves the stored [ItemStack] from the special field in the [target] item's NBT
     * @param key Name of the special field
     * @param target [ItemStack] in which the data is stored
     * @return [ItemStack] that was retrieved
     */
    fun retrieveStoredItemStack(key: String, target: ItemStack): ItemStack {
        if (target.hasNbt() && target.nbt!!.contains(key)) {
            val storedNbt = target.nbt!!.getCompound(key)
            return deserializeItemStack(storedNbt)
        }
        return ItemStack.EMPTY
    }

    fun getLensStack(stack: ItemStack): ItemStack = retrieveStoredItemStack("lensSlot", stack)
    fun getModuleStack(stack: ItemStack): ItemStack = retrieveStoredItemStack("moduleSlot", stack)
    fun getPouchStack(stack: ItemStack): ItemStack = retrieveStoredItemStack("pouchSlot", stack)
}