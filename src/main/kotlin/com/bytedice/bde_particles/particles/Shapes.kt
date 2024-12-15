package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.randomFloatBetween
import com.bytedice.bde_particles.randomIntBetween
import org.joml.Vector2f
import org.joml.Vector3f
import kotlin.math.*
import kotlin.random.Random

sealed class SpawningShape() {
  class Circle(private val radius: Float, val spawnOnEdge: Boolean) : SpawningShape() {
    fun randomWithin(): Vector3f {
      val randRadius = radius * sqrt(randomFloatBetween(0.0f, 1.0f))
      val randAngle = randomFloatBetween(0.0f, Math.PI.toFloat() * 2)

      val randomPos = Vector3f(randRadius * cos(randAngle), 0.0f, randRadius * sin(randAngle))
      return randomPos
    }

    fun randomOnEdge(): Vector3f {
      val randAngle = randomFloatBetween(0.0f, Math.PI.toFloat() * 2)
      return Vector3f(radius * cos(randAngle), 0.0f, radius * sin(randAngle))
    }
  }

  class Rect(val size: Vector2f, val spawnOnEdge: Boolean) : SpawningShape() {
    fun randomWithin(): Vector3f {
      return Vector3f(randomFloatBetween(0.0f, size.x), 0.0f, randomFloatBetween(0.0f, size.y))
    }

    fun randomOnEdge(): Vector3f {
      val perimeter = 2 * (size.x + size.y)
      val randPos = randomFloatBetween(0.0f, perimeter)

      return when {
        randPos <= size.x -> Vector3f(randPos, 0.0f, 0.0f)
        randPos <= size.x + size.y -> Vector3f(size.x, 0.0f, randPos - size.x)
        randPos <= 2 * size.x + size.y -> Vector3f(size.x - (randPos - size.x - size.y), 0.0f, size.y)
        else -> Vector3f(0.0f, 0.0f, size.y - (randPos - 2 * size.x - size.y))
      }
    }
  }

  class Cube(val size: Vector3f, val spawnOnEdge: Boolean) : SpawningShape() {
    fun randomWithin(): Vector3f {
      return Vector3f(
        randomFloatBetween(0.0f, size.x),
        randomFloatBetween(0.0f, size.y),
        randomFloatBetween(0.0f, size.z)
      )
    }

    fun randomOnEdge(): Vector3f {
      val edgeIndex = randomIntBetween(0, 11) // 12 edges
      val randPos = randomFloatBetween(0.0f, 1.0f)

      return when (edgeIndex) {
        0 -> Vector3f(randPos * size.x, 0.0f, 0.0f)
        1 -> Vector3f(size.x, 0.0f, randPos * size.z)
        2 -> Vector3f(size.x - randPos * size.x, 0.0f, size.z)
        3 -> Vector3f(0.0f, 0.0f, size.z - randPos * size.z)
        4 -> Vector3f(randPos * size.x, size.y, 0.0f)
        5 -> Vector3f(size.x, size.y, randPos * size.z)
        6 -> Vector3f(size.x - randPos * size.x, size.y, size.z)
        7 -> Vector3f(0.0f, size.y, size.z - randPos * size.z)
        8 -> Vector3f(0.0f, randPos * size.y, 0.0f)
        9 -> Vector3f(size.x, randPos * size.y, 0.0f)
        10 -> Vector3f(size.x, randPos * size.y, size.z)
        11 -> Vector3f(0.0f, randPos * size.y, size.z)

        else -> Vector3f(0.0f, 0.0f, 0.0f)
      }
    }
  }

  class Sphere(private val radius: Float, val spawnOnEdge: Boolean) : SpawningShape() {
    fun randomWithin(): Vector3f {
      val randRadius = radius * Random.nextDouble().pow(1.0 / 3.0)

      val phi = Random.nextDouble(0.0, 2 * Math.PI)

      val cosTheta = Random.nextDouble(-1.0, 1.0)
      val theta = acos(cosTheta)

      val x = randRadius * sin(theta) * cos(phi)
      val y = randRadius * sin(theta) * sin(phi)
      val z = randRadius * cos(theta)

      return Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
    }

    fun randomOnEdge(): Vector3f {
      val phi = randomFloatBetween(0.0f, Math.PI.toFloat() * 2)
      val cosTheta = randomFloatBetween(-1.0f, 1.0f)
      val sinTheta = sqrt(1 - cosTheta * cosTheta)

      val x = radius * sinTheta * cos(phi)
      val y = radius * sinTheta * sin(phi)
      val z = radius * cosTheta

      return Vector3f(x, y, z)
    }
  }

  data object Point : SpawningShape()

  fun random(shape: SpawningShape) : Vector3f {
    return when (shape) {
      is Circle -> if (!shape.spawnOnEdge) { shape.randomWithin() } else { shape.randomOnEdge() }
      is Sphere -> if (!shape.spawnOnEdge) { shape.randomWithin() } else { shape.randomOnEdge() }
      is Rect   -> if (!shape.spawnOnEdge) { shape.randomWithin() } else { shape.randomOnEdge() }.sub(Vector3f(shape.size.x, 0.0f, shape.size.y).div(2.0f))
      is Cube   -> if (!shape.spawnOnEdge) { shape.randomWithin() } else { shape.randomOnEdge() }.sub(Vector3f(shape.size.x, shape.size.y, shape.size.z).div(2.0f))
      is Point  -> Vector3f(0.0f, 0.0f, 0.0f)
    }
  }
}


sealed class ForceFieldShape {
  data class Sphere(
    val radius: Float,
    val force: Pair<Float, Float>,
  ) : ForceFieldShape()
  data class Cube(
    val size: Vector3f,
    val dir: Vector3f,
    val force: Float
  ) : ForceFieldShape()
}