package com.bytedice.bde_particles

import net.minecraft.util.math.Vec3d
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

data class DisplayEntityProperties (
  val pos: Vec3d    = Vec3d(0.0, 0.0, 0.0),
  val rot: Vector2f = Vector2f(0.0f, 0.0f),

  val blockType: String = "minecraft:purple_concrete",

  val translation:   Vector3f = Vector3f(-0.5f, -0.5f, -0.5f),
  val leftRotation:  Vector4f = Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
  val scale:         Vector3f = Vector3f(1.0f, 1.0f, 1.0f),
  val rightRotation: Vector4f = Vector4f(0.0f, 0.0f, 0.0f, 1.0f)
)
