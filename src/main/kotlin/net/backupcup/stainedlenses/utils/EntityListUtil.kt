package net.backupcup.stainedlenses.utils

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import java.util.List;

interface EntityListUtil {
    fun getList(): List<LivingEntity>
    fun setList(list: List<LivingEntity>)
}