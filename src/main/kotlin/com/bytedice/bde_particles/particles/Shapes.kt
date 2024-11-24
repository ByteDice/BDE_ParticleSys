package com.bytedice.bde_particles.particles

import org.joml.Vector2f
import org.joml.Vector3f

sealed class SpawningShape {
  data class CIRCLE(val radius: Float = 1.0f) : SpawningShape()
  data class RECT  (val size: Vector2f = Vector2f(1.0f, 1.0f)) : SpawningShape()
  data class CUBE  (val size: Vector3f = Vector3f(1.0f, 1.0f, 1.0f)) : SpawningShape()
  data class SPHERE(val radius: Float = 1.0f) : SpawningShape()
  data object POINT : SpawningShape()
}

sealed class ForceFieldShape {
  data class SPHERE(
    val radius: Float = 3.0f,
    val force: Pair<Float, Float> = Pair(0.0f, 0.02f),
  ) : ForceFieldShape()
  data class CUBE(
    val size:     Vector3f = Vector3f(1.0f, 1.0f, 1.0f),
    val forceDir: Vector3f = Vector3f(0.0f, 0.0f, 0.0f)
  ) : ForceFieldShape()
}