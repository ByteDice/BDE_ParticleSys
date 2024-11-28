package com.bytedice.bde_particles.particles

import org.joml.Vector2f
import org.joml.Vector3f

sealed class SpawningShape {
  data class Circle(val radius: Float = 1.0f) : SpawningShape()
  data class Rect  (val size: Vector2f = Vector2f(1.0f, 1.0f)) : SpawningShape()
  data class Cube  (val size: Vector3f = Vector3f(1.0f, 1.0f, 1.0f)) : SpawningShape()
  data class Sphere(val radius: Float = 1.0f) : SpawningShape()
  data object Point : SpawningShape()
}

sealed class ForceFieldShape {
  data class Sphere(
    val radius: Float = 3.0f,
    val force: Pair<Float, Float> = Pair(0.0f, 0.02f),
  ) : ForceFieldShape()
  data class Cube(
    val size: Vector3f = Vector3f(1.0f, 1.0f, 1.0f),
    val dir: Vector3f = Vector3f(0.0f, 0.0f, -1.0f),
    val force: Pair<Float, Float> = Pair(0.0f, 0.02f)
  ) : ForceFieldShape()
  data class Cone(
    val size: Vector2f = Vector2f(1.0f, 1.0f),
    val dir: Vector3f = Vector3f(0.0f, 0.0f, -1.0f),
    val force: Pair<Float, Float> = Pair(0.0f, 0.02f)
  )
  data class Cylinder(
    val size: Vector3f = Vector3f(1.0f, 1.0f, 1.0f),
    val dir: Vector3f = Vector3f(0.0f, 0.0f, -1.0f),
    val force: Pair<Float, Float> = Pair(0.0f, 0.02f)
  )
}