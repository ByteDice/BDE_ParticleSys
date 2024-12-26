package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.*
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import org.joml.Vector2f
import org.joml.Vector3f
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt


class ParticleDebug(
  rot: Vector2f,
  eOrigin: Vec3d,
  pOrigin: Vector3f,
  pVel: Vector3f,
  pScale: Vector3f
) {
  private val pOriginDebug = POrigin(rot, eOrigin, pOrigin)
  private val pVelDebug = PVel(rot, eOrigin, pOrigin, pVel, pScale)

  fun spawn(world: ServerWorld) {
    pOriginDebug.spawn(world)
    pVelDebug.spawn(world)
  }

  fun update(
    rot: Vector2f,
    eOrigin: Vec3d,
    pOrigin: Vector3f,
    pVel: Vector3f,
    pScale: Vector3f
  ) {
    pOriginDebug.update(rot, eOrigin, pOrigin)
    pVelDebug.update(rot, eOrigin, pOrigin, pVel, pScale)
  }

  fun kill() {
    pOriginDebug.kill()
    pVelDebug.kill()
  }
}


class POrigin(rot: Vector2f, eOrigin: Vec3d, pOrigin: Vector3f) {
  val pOriginDE = DisplayEntity(DisplayEntityProperties(
    pos = eOrigin,
    rot = rot,
    model = "minecraft:lime_concrete",
    tags = arrayOf("DEBUG", "P_DEBUG", "pOrigin_DEBUG", "BPS_UUID", SESSION_UUID.toString()),
    brightnessOverride = 15,
    translation = Vector3f(pOrigin),
    scale = Vector3f(0.15f, 0.15f, 0.15f)
  ))

  fun spawn(world: ServerWorld) {
    pOriginDE.spawn(world)
  }

  fun update(rot: Vector2f, eOrigin: Vec3d, pOrigin: Vector3f) {
    val newProperties = pOriginDE.properties
    newProperties?.rot = rot
    newProperties?.pos = eOrigin
    newProperties?.translation = Vector3f(pOrigin)

    pOriginDE.updateProperties(newProperties!!)
  }

  fun kill() {
    pOriginDE.kill()
  }
}

class PVel(rot: Vector2f, eOrigin: Vec3d, pOrigin: Vector3f, pVel: Vector3f, pScale: Vector3f) {
  val pVelDE = DisplayEntity(DisplayEntityProperties(
    pos = eOrigin,
    rot = rot,
    model = "minecraft:red_nether_brick_slab", //"minecraft:red_stained_glass",
    tags = arrayOf("DEBUG", "P_DEBUG", "pVel_DEBUG", "BPS_UUID", SESSION_UUID.toString()),
    brightnessOverride = 15,
    translation = Vector3f(pOrigin),
    scale = Vector3f(0.05f, max(max(pScale.x, pScale.y), pScale.z) + (pVel.x + pVel.y + pVel.z), 0.05f)
  ))

  fun spawn(world: ServerWorld) {
    pVelDE.spawn(world)
  }

  fun update(rot: Vector2f, eOrigin: Vec3d, pOrigin: Vector3f, pVel: Vector3f, pScale: Vector3f) {
    val newProperties = pVelDE.properties
    val scale = (max(max(pScale.x, pScale.y), pScale.z) + (pVel.x + pVel.y + pVel.z)) * 2.0f
    val scaleVec3f = Vector3f(0.05f, scale, 0.05f)
    val leftRotEuler = velToRot(pVel)

    newProperties?.rot = rot
    newProperties?.pos = eOrigin
    newProperties?.translation = Vector3f(pOrigin)
    newProperties?.leftRotation = eulerToQuat(leftRotEuler)
    newProperties?.scale = scaleVec3f

    pVelDE.updateProperties(newProperties!!)
  }

  fun kill() {
    pVelDE.kill()
  }

  fun velToRot(velocity: Vector3f): Vector3f {
    val normalizedVelocity = Vector3f(velocity).normalize()

    val yaw = Math.toDegrees(atan2(normalizedVelocity.x.toDouble(), normalizedVelocity.z.toDouble())).toFloat()

    val horizontalMag = sqrt(normalizedVelocity.x.toDouble().pow(2) + normalizedVelocity.z.toDouble().pow(2))
    val pitch = Math.toDegrees(atan2(-normalizedVelocity.y.toDouble(), horizontalMag)).toFloat() + -90.0f

    return Vector3f(pitch, yaw, 0.0f)
  }
}