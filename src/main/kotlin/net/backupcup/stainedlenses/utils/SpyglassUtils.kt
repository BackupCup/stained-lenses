package net.backupcup.stainedlenses.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.stream.Collectors
import kotlin.math.acos
import kotlin.math.tan


object SpyglassUtils {

    fun getVisibleEntitiesThroughSpyglass(player: PlayerEntity): List<LivingEntity> {
        val world = player.world
        val playerPos = player.pos
        val playerLook = player.getRotationVec(1.0f)

        val playerFov: Int = MinecraftClient.getInstance().options.fov.value

        val halfFovRadians = Math.toRadians(playerFov * ((player as ZoomUtil).getZoom()) / 2.0)
        val maxDistance = MinecraftClient.getInstance().options.entityDistanceScaling.value * 32

        val boundingBox: Box = player.boundingBox.expand(maxDistance)
        val entitiesInRange: List<LivingEntity> = world.getEntitiesByClass(
            LivingEntity::class.java, boundingBox) { entity: LivingEntity -> entity != player }

        return entitiesInRange.filter { entity -> isWithinSpyglassFOV(playerPos, playerLook, entity.pos, halfFovRadians) }
    }

    private fun isWithinSpyglassFOV(
        playerPos: Vec3d,
        playerLook: Vec3d,
        entityPos: Vec3d,
        halfFovRadians: Double
    ): Boolean {
        val toEntity = entityPos.subtract(playerPos).normalize()
        val angle = acos(playerLook.normalize().dotProduct(toEntity))

        return angle <= (halfFovRadians + Math.toRadians(6.0))
    }

    fun projectToPlayerView(
        targetX: Double,
        targetY: Double,
        targetZ: Double
    ): Triple<Float, Float, Float> {
        val camera = MinecraftClient.getInstance().gameRenderer.camera
        val cameraPos = camera.pos
        val cameraRotationConj = camera.rotation.clone()
        (cameraRotationConj as Quaternionf).conjugate()

        val result3f = Vector3f(
            (cameraPos.x - targetX).toFloat(),
            (cameraPos.y - targetY).toFloat(),
            (cameraPos.z - targetZ).toFloat())
        result3f.rotate(cameraRotationConj)

        val mc = MinecraftClient.getInstance()

        val fov = mc.gameRenderer.getFov(camera, 1f, true)
        val halfHeight = mc.window.scaledHeight.toFloat() / 2
        val scaleFactor: Float = (halfHeight / (result3f.z * tan(Math.toRadians((fov / 2.0))).toFloat())) * (mc.player as ZoomUtil).getZoom()
        return Triple(-result3f.x * scaleFactor, result3f.y * scaleFactor,
            calculateOutput(cameraPos.distanceTo(Vec3d(targetX, targetY, targetZ)), 3.0,
                MinecraftClient.getInstance().options.entityDistanceScaling.value * 32))
    }

    private fun calculateOutput(distance: Double, minDistance: Double, maxDistance: Double): Float {
        // Constants based on boundary conditions
        val a = 2.75 / (1 / minDistance - 1 / maxDistance)
        val b = 0.25 - a / maxDistance

        return ((a / distance) + b).toFloat()
    }
}

class Triple<A, B, C>(val first: A, val second: B, val third: C)