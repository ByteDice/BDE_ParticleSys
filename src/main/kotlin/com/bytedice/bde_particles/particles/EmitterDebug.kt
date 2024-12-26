package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.Billboard
import com.bytedice.bde_particles.DisplayEntity
import com.bytedice.bde_particles.DisplayEntityProperties
import com.bytedice.bde_particles.SESSION_UUID
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

class EmitterDebug(
  rot: Vector2f,
  eOrigin: Vec3d,
  spawnPosOffset: Vector3f,
  shape: SpawningShape,
) {
  private val eOriginDebug = EOrigin(rot, eOrigin)
  private val shapeDebug = ShapeDebug(eOrigin, spawnPosOffset, shape)
  private val forceFieldDebug = ForceFieldDebug()

  fun spawn(world: ServerWorld, eOrigin: Vec3d, forceFields: ParamClasses.ForceFieldArray, spawnPosOffset: Vector3f) {
    eOriginDebug.spawn(world)
    shapeDebug.spawn(world)
    forceFieldDebug.makeArray(eOrigin, forceFields, spawnPosOffset)
    forceFieldDebug.spawn(world)
  }

  fun update(
    rot: Vector2f,
    eOrigin: Vec3d,
    spawnPosOffset: Vector3f
  ) {
    eOriginDebug.update(rot, eOrigin)
    shapeDebug.update(rot, eOrigin, spawnPosOffset)
  }

  fun kill() {
    eOriginDebug.kill()
    shapeDebug.kill()
    forceFieldDebug.kill()
  }
}


class EOrigin(rot: Vector2f, eOrigin: Vec3d) {
  val eOriginDE = DisplayEntity(
    DisplayEntityProperties(
    pos = eOrigin,
    rot = rot,
    model = "minecraft:red_concrete",
    tags = arrayOf("DEBUG", "E_DEBUG", "eOrigin_DEBUG", "BPS_UUID", SESSION_UUID.toString()),
    brightnessOverride = 15,
    scale = Vector3f(0.25f, 0.25f, 0.25f)
  ))

  fun spawn(world: ServerWorld) {
    eOriginDE.spawn(world)
  }

  fun update(rot: Vector2f, eOrigin: Vec3d) {
    val newProperties = eOriginDE.properties
    newProperties?.rot = rot
    newProperties?.pos = eOrigin

    eOriginDE.updateProperties(newProperties!!)
  }

  fun kill() {
    eOriginDE.kill()
  }
}

class ShapeDebug(eOrigin: Vec3d, spawnPosOffset: Vector3f, shape: SpawningShape) {
  val shapeScale: Vector3f? = when (shape) {
    is SpawningShape.Circle -> Vector3f(shape.radius * 1.75f * 2, shape.radius * 1.75f * 2, 0.0001f)
    is SpawningShape.Sphere -> Vector3f(shape.radius * 1.75f * 2, shape.radius * 1.75f * 2, 0.0001f)
    is SpawningShape.Rect   -> Vector3f(shape.size.x, 0.0001f, shape.size.y)
    is SpawningShape.Cube   -> Vector3f(shape.size.x, shape.size.y, shape.size.z)
    else -> null
  }

  val shapeRot = when (shape) {
    is SpawningShape.Circle -> Vector4f(-0.707f, 0.0f, 0.0f, 0.707f)
    else -> Vector4f(0.0f, 0.0f, 0.0f, 1.0f)
  }

  private fun determineModel(shape: SpawningShape): String =
    when (shape) {
      is SpawningShape.Circle, is SpawningShape.Sphere -> "minecraft:sunflower"
      else -> "minecraft:yellow_stained_glass"
    }

  private fun determineBillboard(shape: SpawningShape): String =
    if (shape !is SpawningShape.Sphere) Billboard.FIXED else Billboard.CENTER

  val shapeDE: DisplayEntity? = if (shape !is SpawningShape.Point) {
    val position = eOrigin.add(
      spawnPosOffset.x.toDouble(),
      spawnPosOffset.y.toDouble(),
      spawnPosOffset.z.toDouble()
    )

    DisplayEntity(DisplayEntityProperties(
      pos = position.add(spawnPosOffset.x.toDouble(), spawnPosOffset.y.toDouble(), spawnPosOffset.z.toDouble()),
      model = determineModel(shape),
      tags = arrayOf("DEBUG", "P_DEBUG", "shape_DEBUG", "BPS_UUID", SESSION_UUID.toString()),
      brightnessOverride = 15,
      billboard = determineBillboard(shape),
      leftRotation = shapeRot,
      scale = shapeScale ?: Vector3f(1f, 1f, 1f)
    ))
  } else null

  fun spawn(world: ServerWorld) {
    shapeDE?.spawn(world)
  }

  fun update(rot: Vector2f, eOrigin: Vec3d, spawnPosOffset: Vector3f) {
    val newProperties = shapeDE?.properties
    newProperties?.rot = rot
    newProperties?.pos = eOrigin
    newProperties?.translation = spawnPosOffset

    shapeDE?.updateProperties(newProperties!!)
  }

  fun kill() {
    shapeDE?.kill()
  }
}

class ForceFieldDebug {
  var FFDEArray: Array<DisplayEntity> = emptyArray()

  fun makeArray(eOrigin: Vec3d, forceFieldArray: ParamClasses.ForceFieldArray, spawnPosOffset: Vector3f) {
    forceFieldArray.array.forEach {
      val shapeScale = when (it.shape) {
        is ForceFieldShape.Sphere -> Vector3f(1.333f * it.shape.radius * 2, 1.333f * it.shape.radius * 2, 0.0001f)
        is ForceFieldShape.Cube   -> it.shape.size
      }

      val newOffset = Vector3f(spawnPosOffset).add(it.pos)

      val displayEntity = DisplayEntity(
        DisplayEntityProperties(
          pos = eOrigin.add(newOffset.x.toDouble(), newOffset.y.toDouble(), newOffset.z.toDouble()),
          model = if (it.shape is ForceFieldShape.Sphere) { "minecraft:snowball" } else { "minecraft:white_stained_glass" },
          tags = arrayOf("DEBUG", "E_DEBUG", "eOrigin_DEBUG", "BPS_UUID", SESSION_UUID.toString()),
          brightnessOverride = 15,
          billboard = if (it.shape is ForceFieldShape.Sphere) { Billboard.CENTER } else { Billboard.FIXED },
          scale = shapeScale
      ))

      FFDEArray += displayEntity
    }
  }

  fun spawn(world: ServerWorld) {
    FFDEArray.forEach { it.spawn(world) }
  }

  fun kill() {
    FFDEArray.forEach { it.kill() }
  }
}