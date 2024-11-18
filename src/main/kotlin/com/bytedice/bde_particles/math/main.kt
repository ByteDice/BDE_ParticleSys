package com.bytedice.bde_particles.math

import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.*
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


fun randomIntBetween(min: Int, max: Int) : Int {
  return Random.nextInt(min, max)
}


fun randomBetweenVector3f(min: Vector3f, max: Vector3f) : Vector3f {
  return Vector3f(
    randomFloatBetween(min.x, max.x),
    randomFloatBetween(min.y, max.y),
    randomFloatBetween(min.z, max.z)
  )
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

  val sinRCosP = 2 * (w * x + y * z)
  val cosRCosP = 1 - 2 * (x * x + y * y)
  val roll = atan2(sinRCosP, cosRCosP)

  val sinP = 2 * (w * y - z * x)
  val pitch = if (abs(sinP) >= 1) {
    sign(sinP) * (Math.PI.toFloat() / 2)
  } else {
    asin(sinP)
  }

  val sinYCosP = 2 * (w * z + x * y)
  val cosYCosP = 1 - 2 * (y * y + z * z)
  val yaw = atan2(sinYCosP, cosYCosP)

  return Vector3f(roll, pitch, yaw)
}


fun randomInCircle(r: Float) : Vector2f {
  val randRadius = r * sqrt(randomFloatBetween(0.0f, 1.0f))
  val randAngle  = randomFloatBetween(0.0f, Math.PI.toFloat() * 2)

  val randomPos = Vector2f(randRadius * cos(randAngle), randRadius * sin(randAngle))
  return randomPos
}


fun randomInSphere(r: Float) : Vector3f {
  val randRadius = r * Random.nextDouble().pow(1.0 / 3.0)

  val phi = Random.nextDouble(0.0, 2 * Math.PI)

  val cosTheta = Random.nextDouble(-1.0, 1.0)
  val theta = acos(cosTheta)

  val x = randRadius * sin(theta) * cos(phi)
  val y = randRadius * sin(theta) * sin(phi)
  val z = randRadius * cos(theta)

  return Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
}


fun randomInRect(size: Vector2f) : Vector2f {
  return Vector2f(randomFloatBetween(0.0f, size.x), randomFloatBetween(0.0f, size.y))
}


fun randomInCube(size: Vector3f) : Vector3f {
  return Vector3f(
    randomFloatBetween(0.0f, size.x),
    randomFloatBetween(0.0f, size.y),
    randomFloatBetween(0.0f, size.z)
  )
}


fun transformOffsetByQuat(offset: Vector3f, rotation: Vector4f): Vector3f {
  val x = offset.x
  val y = offset.y
  val z = offset.z

  val qx = rotation.x.toDouble()
  val qy = rotation.y.toDouble()
  val qz = rotation.z.toDouble()
  val qw = rotation.w.toDouble()

  val t2 = qw * x + qy * z - qz * y
  val t3 = qw * y + qz * x - qx * z
  val t4 = qw * z + qx * y - qy * x
  val t5 = -qx * x - qy * y - qz * z

  val newX = t2 * qw + t5 * -qx + t3 * -qz - t4 * -qy
  val newY = t3 * qw + t5 * -qy + t4 * -qx - t2 * -qz
  val newZ = t4 * qw + t5 * -qz + t2 * -qy - t3 * -qx

  return Vector3f(newX.toFloat(), newY.toFloat(), newZ.toFloat())
}


fun transformOffsetByScale(offset: Vector3f, scale: Vector3f): Vector3f {
  return Vector3f(offset).mul(scale)
}


fun lerp(x: Float, y: Float, t: Float) : Float {
  return x + (y - x) * t
}


fun interpolateCurve(curve: Array<Vector3f>, t: Float): Vector3f {
  if (curve.size < 2) { return curve[0] }

  val clampedT = t.coerceIn(0.0f, 1.0f)

  val scaledT = clampedT * (curve.size - 1)
  val segmentIndex = scaledT.toInt()
  val segmentT = scaledT - segmentIndex

  val start = curve[segmentIndex]
  val end = curve[(segmentIndex + 1).coerceAtMost(curve.size - 1)] // Ensure bounds

  return Vector3f(
    start.x + (end.x - start.x) * segmentT,
    start.y + (end.y - start.y) * segmentT,
    start.z + (end.z - start.z) * segmentT
  )
}


fun sdfSphere(point: Vec3d, radius: Double): Double {
  val x = point.x
  val y = point.y
  val z = point.z

  val distanceFromCenter = sqrt(x * x + y * y + z * z)
  return distanceFromCenter - radius
}


fun sdfCube(point: Vec3d, size: Vec3d): Double {
  val px = point.x
  val py = point.y
  val pz = point.z

  val sx = size.x
  val sy = size.y
  val sz = size.z

  val halfSize = Triple(sx / 2, sy / 2, sz / 2)

  val absPoint = Triple(abs(px), abs(py), abs(pz))

  val dx = absPoint.first - halfSize.first
  val dy = absPoint.second - halfSize.second
  val dz = absPoint.third - halfSize.third

  val outside = Triple(max(dx, 0.0), max(dy, 0.0), max(dz, 0.0))
  val outsideDistance = sqrt(outside.first * outside.first + outside.second * outside.second + outside.third * outside.third)

  val insideDistance = min(max(dx, max(dy, dz)), 0.0)

  return outsideDistance + insideDistance
}