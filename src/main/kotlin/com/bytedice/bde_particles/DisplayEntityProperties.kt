package com.bytedice.bde_particles

import net.minecraft.util.math.Vec3d
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f

data class DisplayEntityProperties (
  var pos: Vec3d    = Vec3d(0.0, 0.0, 0.0),
  var rot: Vector2f = Vector2f(0.0f, 0.0f),

  var model:              String        = "minecraft:purple_concrete",
  var customModel:        Int           = 0,
  var tags:               Array<String> = emptyArray(),
  var viewRange:          Float         = 2.0f,
  var customName:         String?       = null,
  var brightnessOverride: Int?          = null,
  var billboard:          String        = Billboard.FIXED,

  var translation:   Vector3f = Vector3f(0.0f, 0.0f, 0.0f),
  var leftRotation:  Vector4f = Vector4f(0.0f, 0.0f, 0.0f, 1.0f),
  var scale:         Vector3f = Vector3f(1.0f, 1.0f, 1.0f),
  var rightRotation: Vector4f = Vector4f(0.0f, 0.0f, 0.0f, 1.0f)
)

class Billboard () {
  companion object {
    const val FIXED:      String = "fixed"
    const val VERTICAL:   String = "vertical"
    const val HORIZONTAL: String = "horizontal"
    const val CENTER:     String = "center"
  }
}