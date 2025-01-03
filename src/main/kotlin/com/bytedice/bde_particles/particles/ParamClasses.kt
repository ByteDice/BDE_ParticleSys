package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.LerpCurves
import com.bytedice.bde_particles.lerpArray
import com.bytedice.bde_particles.randomFloatBetween
import com.bytedice.bde_particles.randomIntBetween
import org.joml.Vector3f
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class ParamClasses {
  sealed class PairVec3f {
    class Uniform (
      private val min: Float,
      private val max: Float,
    ) : PairVec3f() {
      fun randomize() : Vector3f {
        val x = randomFloatBetween(min, max)
        return Vector3f(x, x, x)
      }
    }
    class NonUniform (
      private val minX: Float,
      private val minY: Float,
      private val minZ: Float,
      private val maxX: Float,
      private val maxY: Float,
      private val maxZ: Float
    ) : PairVec3f() {
      fun randomize(): Vector3f {
        return Vector3f(
          randomFloatBetween(minX, maxX),
          randomFloatBetween(minY, maxY),
          randomFloatBetween(minZ, maxZ),
        )
      }
    }
    data class Constant(val value: Vector3f) : PairVec3f()
    data object Null : PairVec3f()
  }

  class PairInt (
    val min: Int,
    val max: Int,
  ) {
    fun randomize() : Int { return randomIntBetween(min, max) }
  }

  class PairFloat(
    val min: Float,
    val max: Float,
  ) {
    fun randomize() : Float { return randomFloatBetween(min, max) }
  }

  sealed class LerpValVec3f {
    class LerpVec3f(
      private val fromX: Float,
      private val fromY: Float,
      private val fromZ: Float,
      private val toX: Float,
      private val toY: Float,
      private val toZ: Float,
      private val curve: LerpCurves,
    ) : LerpValVec3f() {
      fun lerp(t: Float): Vector3f {
        val x = com.bytedice.bde_particles.lerp(fromX, toX, curve.function(t))
        val y = com.bytedice.bde_particles.lerp(fromY, toY, curve.function(t))
        val z = com.bytedice.bde_particles.lerp(fromZ, toZ, curve.function(t))
        return Vector3f(x, y, z)
      }
    }
    class MultiLerpVec3f(
      private val fromX: Float,
      private val fromY: Float,
      private val fromZ: Float,
      private val toX: Float,
      private val toY: Float,
      private val toZ: Float,
      private val curveX: LerpCurves,
      private val curveY: LerpCurves,
      private val curveZ: LerpCurves
    ) : LerpValVec3f() {
      fun lerp(t: Float): Vector3f {
        val x = com.bytedice.bde_particles.lerp(fromX, toX, curveX.function(t))
        val y = com.bytedice.bde_particles.lerp(fromY, toY, curveY.function(t))
        val z = com.bytedice.bde_particles.lerp(fromZ, toZ, curveZ.function(t))
        return Vector3f(x, y, z)
      }
    }
    class LerpUniform(
      private val from: Float,
      private val to: Float,
      private val curve: LerpCurves,
    ) : LerpValVec3f() {
      fun lerp(t: Float) : Float {
        return com.bytedice.bde_particles.lerp(from, to, curve.function(t))
      }
    }
    data class Constant(val value: Vector3f) : LerpValVec3f()
    data object Null : LerpValVec3f()

    fun lerpToVector3f(t: Float) : Vector3f {
      when (this) {
        is LerpVec3f -> return this.lerp(t)
        is MultiLerpVec3f -> return this.lerp(t)
        is LerpUniform -> {
          val x = this.lerp(t)
          return Vector3f(x, x, x)
        }
        is Constant -> return this.value
        is Null -> return Vector3f(1.0f, 1.0f, 1.0f)
      }
    }
  }

  sealed class LerpValInt {
    class LerpInt(
      private val from: Int,
      private val to: Int,
      private val curve: LerpCurves,
    ) : LerpValInt() {
      fun lerp(t: Float): Int {
        return Math.round(com.bytedice.bde_particles.lerp(from.toFloat(), to.toFloat(), curve.function(t)))
      }
    }
    data class Constant(val value: Int) : LerpValInt()
    data object Null: LerpValInt()

    fun lerpToInt(t: Float) : Int? {
      return when (this) {
        is LerpInt -> this.lerp(t)
        is Constant -> this.value
        is Null -> null
      }
    }
  }

  sealed class Duration {
    data class SingleBurst(
      val loopDur:        Int
    ) : Duration()
    data class MultiBurst(
      val loopDur:        Int,
      val loopDelay:      Int,
      val loopCount:      Int,
    ) : Duration()
    data class InfiniteLoop(
      val loopDur:        Int,
      val loopDelay:      Int
    ) : Duration()
  }

  sealed class TransformWithVel {
    data object RotOnly : TransformWithVel() {
      fun velToRot(velocity: Vector3f): Vector3f {
        val normalizedVelocity = Vector3f(velocity).normalize()

        val yaw = Math.toDegrees(atan2(normalizedVelocity.x.toDouble(), normalizedVelocity.z.toDouble())).toFloat()

        val horizontalMag = sqrt(normalizedVelocity.x.toDouble().pow(2) + normalizedVelocity.z.toDouble().pow(2))
        val pitch = Math.toDegrees(atan2(-normalizedVelocity.y.toDouble(), horizontalMag)).toFloat()

        return Vector3f(pitch, yaw, 0f)
      }
    }
    class ScaleAndRot(private val scaleMul: Float) : TransformWithVel() {
      fun velToRot(velocity: Vector3f) : Vector3f {
        val normalizedVelocity = Vector3f(velocity).normalize()

        val yaw = Math.toDegrees(atan2(normalizedVelocity.x.toDouble(), normalizedVelocity.z.toDouble())).toFloat()

        val horizontalMag = sqrt(normalizedVelocity.x.toDouble().pow(2) + normalizedVelocity.z.toDouble().pow(2))
        val pitch = Math.toDegrees(atan2(-normalizedVelocity.y.toDouble(), horizontalMag)).toFloat()

        return Vector3f(pitch, yaw, 0f)
      }
      fun velToScale(velocity: Vector3f): Vector3f {
        val velocityMagnitude = velocity.length()
        val stretchFactor = velocityMagnitude * scaleMul + 1.0f
        val contractFactor = 1.0f / stretchFactor

        return Vector3f(contractFactor, contractFactor, stretchFactor)
      }
    }
    data object Null : TransformWithVel()
  }

  class StringCurve (
    var array: Array<String>,
    var curve: LerpCurves
  ) {
    fun lerp(t: Float) : String { return lerpArray(array as Array<Any>, t, curve) as String }

    fun deepCopy(): StringCurve {
      return StringCurve(array.copyOf(), curve.deepCopy())
    }
  }

  data class ForceFieldArray (
    var array: Array<ForceField>
  ) {
    fun deepCopy(): ForceFieldArray {
      return ForceFieldArray(array.map { it.deepCopy() }.toTypedArray())
    }
  }
}