package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.LerpCurves
import com.bytedice.bde_particles.randomFloatBetween
import com.bytedice.bde_particles.randomIntBetween
import org.joml.Vector3f

class ParamClasses {
  class PairVec3f (
    private val minX: Float = 0.0f,
    private val minY: Float = 0.0f,
    private val minZ: Float = 0.0f,
    private val maxX: Float = 1.0f,
    private val maxY: Float = 1.0f,
    private val maxZ: Float = 1.0f,
  ) {
    fun randomize() : Vector3f {
      return Vector3f(
        randomFloatBetween(minX, maxX),
        randomFloatBetween(minY, maxY),
        randomFloatBetween(minZ, maxZ),
      )
    }
  }
  sealed class RandScale {
    class Uniform (
      private val min: Float = 0.0f,
      private val max: Float = 1.0f,
    ) : RandScale() {
      fun randomize() : Vector3f {
        val x = randomFloatBetween(min, max)
        return Vector3f(x, x, x)
      }
    }
    class NonUniform (
      private val minX: Float = 0.0f,
      private val minY: Float = 0.0f,
      private val minZ: Float = 0.0f,
      private val maxX: Float = 1.0f,
      private val maxY: Float = 1.0f,
      private val maxZ: Float = 1.0f
    ) : RandScale() {
      fun randomize(): Vector3f {
        return Vector3f(
          randomFloatBetween(minX, maxX),
          randomFloatBetween(minY, maxY),
          randomFloatBetween(minZ, maxZ),
        )
      }
    }
  }
  class PairVec2i (
    private val minX: Int = 0,
    private val minY: Int = 0,
    private val maxX: Int = 1,
    private val maxY: Int = 1,
  ) {
    fun randomize() : Pair<Int, Int> {
      return Pair(
        randomIntBetween(minX, maxX),
        randomIntBetween(minY, maxY),
      )
    }
  }
  sealed class LerpVal {
    class LerpVec3f(
      private val fromX: Float = 0.0f,
      private val fromY: Float = 0.0f,
      private val fromZ: Float = 0.0f,
      private val toX: Float = 1.0f,
      private val toY: Float = 1.0f,
      private val toZ: Float = 1.0f,
      private val curve: LerpCurves = LerpCurves.Linear,
    ) : LerpVal() {
      fun lerp(t: Float): Vector3f {
        val x = com.bytedice.bde_particles.lerp(fromX, toX, t) * curve.function(t)
        val y = com.bytedice.bde_particles.lerp(fromY, toY, t) * curve.function(t)
        val z = com.bytedice.bde_particles.lerp(fromZ, toZ, t) * curve.function(t)
        return Vector3f(x, y, z)
      }
    }
    class MultiLerpVec3f(
      private val fromX: Float = 0.0f,
      private val fromY: Float = 0.0f,
      private val fromZ: Float = 0.0f,
      private val toX: Float = 1.0f,
      private val toY: Float = 1.0f,
      private val toZ: Float = 1.0f,
      private val curveX: LerpCurves = LerpCurves.Linear,
      private val curveY: LerpCurves = LerpCurves.Linear,
      private val curveZ: LerpCurves = LerpCurves.Linear
    ) : LerpVal() {
      fun lerp(t: Float): Vector3f {
        val x = com.bytedice.bde_particles.lerp(fromX, toX, t) * curveX.function(t)
        val y = com.bytedice.bde_particles.lerp(fromY, toY, t) * curveY.function(t)
        val z = com.bytedice.bde_particles.lerp(fromZ, toZ, t) * curveZ.function(t)
        return Vector3f(x, y, z)
      }
    }
    class LerpUniform(
      private val from: Float = 0.0f,
      private val to: Float = 1.0f,
      private val curve: LerpCurves = LerpCurves.Linear,
    ) : LerpVal() {
      fun lerp(t: Float): Float {
        return com.bytedice.bde_particles.lerp(from, to, t) * curve.function(t)
      }
    }
  }
}