package net.backupcup.stainedlenses.blocks

import net.backupcup.stainedlenses.registry.RegisterBlocks
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.concurrent.CopyOnWriteArrayList

class EtherealBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(RegisterBlocks.ETHEREAL_BLOCK_ENTITY, pos, state) {
    var transparency = 0f
    var connectedList = CopyOnWriteArrayList<Direction>()

    init {
        mutableListOf(Direction.DOWN, Direction.UP, Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH).forEach {
            direction -> this.connectedList.add(direction) }
    }
}