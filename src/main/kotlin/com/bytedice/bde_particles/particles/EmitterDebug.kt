package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.Billboard
import com.bytedice.bde_particles.DisplayEntity
import com.bytedice.bde_particles.DisplayEntityProperties
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
  forceFields: ParamClasses.ForceFieldArray
) {
  private val eOrigin = EOrigin(rot, eOrigin)
  private val shapeDebug = ShapeDebug(rot, eOrigin, spawnPosOffset, shape)

  fun spawn(world: ServerWorld) {
    eOrigin.spawn(world)
    shapeDebug.spawn(world)
  }

  fun update(
    rot: Vector2f,
    eOrigin: Vec3d,
    pOrigin: Vector3f,
    pVel: Vector3f,
    pScale: Vector3f,
    spawnPosOffset: Vector3f,
    shape: SpawningShape,
  ) {

  }
}


class EOrigin(rot: Vector2f, eOrigin: Vec3d) {
  val eOriginDE = DisplayEntity(
    DisplayEntityProperties(
    pos = eOrigin,
    rot = rot,
    model = "minecraft:red_concrete",
    tags = arrayOf("DEBUG", "E_DEBUG", "eOrigin_DEBUG"),
    brightnessOverride = 15,
    scale = Vector3f(0.25f, 0.25f, 0.25f)
  )
  )

  fun spawn(world: ServerWorld) {
    eOriginDE.spawn(world)
  }

  fun update(rot: Vector2f, eOrigin: Vec3d) {
    val newProperties = eOriginDE.properties
    newProperties?.rot = rot
    newProperties?.pos = eOrigin

    eOriginDE.updateProperties(newProperties!!)
  }
}

class ShapeDebug(rot: Vector2f, eOrigin: Vec3d, spawnPosOffset: Vector3f, shape: SpawningShape) {
  val shapeScale: Vector3f? = when (shape) {
    is SpawningShape.Circle -> Vector3f(shape.radius * 1.75f, shape.radius * 1.75f, 0.0f)
    is SpawningShape.Sphere -> Vector3f(shape.radius * 1.75f, shape.radius * 1.75f, 0.0f)
    is SpawningShape.Rect -> Vector3f(shape.size.x, 0.0f, shape.size.y)
    is SpawningShape.Cube -> Vector3f(shape.size.x, shape.size.y, shape.size.z)
    else -> null
  }

  val shapeRot = if (shape is SpawningShape.Circle) { Vector4f(-0.707f, 0.0f, 0.0f, 0.707f) }
  else { Vector4f(0.0f, 0.0f, 0.0f, 1.0f) }


  val shapeDE: DisplayEntity? = if (shape !is SpawningShape.Point) {
    DisplayEntity(DisplayEntityProperties(
      pos = eOrigin.add(spawnPosOffset.x.toDouble(), spawnPosOffset.y.toDouble(), spawnPosOffset.z.toDouble()),
      rot = rot,
      model = if (shape is SpawningShape.Circle || shape is SpawningShape.Sphere) { "minecraft:sunflower" }
      else { "minecraft:yellow_stained_glass" },
      tags = arrayOf("DEBUG", "P_DEBUG", "shape_DEBUG"),
      brightnessOverride = 15,
      billboard = if (shape !is SpawningShape.Sphere) { Billboard.FIXED } else { Billboard.CENTER },
      translation = spawnPosOffset,
      leftRotation = shapeRot,
      scale = shapeScale!!
    ))
  }
  else { null }

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
}