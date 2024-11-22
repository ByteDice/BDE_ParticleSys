package com.bytedice.bde_particles.particleIdRegister

import org.joml.Vector2f
import org.joml.Vector3f

sealed class SpawningShape {
  data class Circle(val radius: Float = 1.0f)                                 : SpawningShape()
  data class Rect  (val size: Vector2f = Vector2f(1.0f, 1.0f))          : SpawningShape()
  data class Cube  (val size: Vector3f = Vector3f(1.0f, 1.0f, 1.0f)) : SpawningShape()
  data class Sphere(val radius: Float = 1.0f)                                 : SpawningShape()
}

sealed class ForceFieldShape {
  data class Sphere(
    val pos: Vector3f = Vector3f(0.0f, 0.0f, 0.0f),
    val radius: Float = 1.0f
  ) : ForceFieldShape()

  data class Rect(
    val pos:      Vector3f = Vector3f(0.0f, 0.0f, 0.0f),
    val size:     Vector3f = Vector3f(1.0f, 1.0f, 1.0f),
    val forceDir: Vector3f = Vector3f(0.0f, 0.0f, 0.0f) // 0.0 on all means from center
  ) : ForceFieldShape()
}