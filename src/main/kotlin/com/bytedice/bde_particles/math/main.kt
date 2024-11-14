package com.bytedice.bde_particles.math

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


fun raycastFromPlayer(player: ServerPlayerEntity, maxDistance: Double): HitResult? {
  val world: World = player.world
  val eyePos: Vec3d = player.getCameraPosVec(1.0f)
  val lookVec: Vec3d = player.getRotationVec(1.0f)
  val targetPos: Vec3d = eyePos.add(lookVec.multiply(maxDistance))

  val blockHitResult = world.raycast(
    RaycastContext(
      eyePos,
      targetPos,
      RaycastContext.ShapeType.OUTLINE,
      RaycastContext.FluidHandling.NONE,
      player
    )
  )

  return if (blockHitResult.type == HitResult.Type.BLOCK) {
    blockHitResult
  } else {
    null
  }
}


fun randomFloatBetween(min: Float, max: Float) : Float {
  return Random.nextFloat() * (max - min) + min
}


fun eulerToQuat(euler: Vector3f): Vector4f {
  val roll  = euler.x
  val pitch = euler.y
  val yaw   = euler.z

  val cy = cos(yaw * 0.5)
  val sy = sin(yaw * 0.5)
  val cp = cos(pitch * 0.5)
  val sp = sin(pitch * 0.5)
  val cr = cos(roll * 0.5)
  val sr = sin(roll * 0.5)

  val w = cr * cp * cy + sr * sp * sy
  val x = sr * cp * cy - cr * sp * sy
  val y = cr * sp * cy + sr * cp * sy
  val z = cr * cp * sy - sr * sp * cy

  return Vector4f(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat())
}

fun quatToEuler(quat: Vector4f): Vector3f {
  val x = quat.x
  val y = quat.y
  val z = quat.z
  val w = quat.w

  // Roll (X-axis rotation)
  val sinr_cosp = 2 * (w * x + y * z)
  val cosr_cosp = 1 - 2 * (x * x + y * y)
  val roll = atan2(sinr_cosp, cosr_cosp)

  // Pitch (Y-axis rotation)
  val sinp = 2 * (w * y - z * x)
  val pitch = if (kotlin.math.abs(sinp) >= 1) {
    // Use 90 degrees if out of range
    kotlin.math.sign(sinp) * (Math.PI.toFloat() / 2)
  } else {
    asin(sinp)
  }

  // Yaw (Z-axis rotation)
  val siny_cosp = 2 * (w * z + x * y)
  val cosy_cosp = 1 - 2 * (y * y + z * z)
  val yaw = atan2(siny_cosp, cosy_cosp)

  return Vector3f(roll, pitch, yaw)
}