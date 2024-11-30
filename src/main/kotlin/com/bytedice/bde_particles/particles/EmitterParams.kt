package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.LerpCurves
import org.joml.Vector3f


data class EmitterParams (
  // spawning
  var maxCount:       Int,
  var spawnRate:      Int,
  var spawnChance:    Float,
  var spawnDuration:  ParamClasses.Duration,
  var spawnPosOffset: Vector3f,
  var lifeTime:       ParamClasses.PairInt,
  var shape:          SpawningShape,
  // init transforms
  var offset:           ParamClasses.PairVec3f,
  var initRot:          ParamClasses.PairVec3f,
  var rotVel:           ParamClasses.PairVec3f,
  var initVel:          ParamClasses.PairVec3f,
  var initCenterVel:    ParamClasses.PairFloat,
  var initScale:        ParamClasses.PairVec3f,
  var transformWithVel: ParamClasses.TransformWithVel,
  // velocity
  var forceFields: Array<ForceField>,
  var constVel:    Vector3f,
  var drag:        Float,
  var minVel:      Float,
  // curves
  var offsetCurve: ParamClasses.LerpVal,
  var rotVelCurve: ParamClasses.LerpVal,
  var scaleCurve:  ParamClasses.LerpVal,
  var blockCurve:  Pair<Array<String>, LerpCurves>,
)
{
  companion object Presets {
    val DEFAULT = EmitterParams(
      maxCount = 400,
      spawnRate = 100,
      spawnChance = 1.0f,
      spawnDuration = ParamClasses.Duration.SingleBurst(4),
      spawnPosOffset = Vector3f(0.0f, 0.0f, 0.0f),
      lifeTime = ParamClasses.PairInt(25, 40),
      shape = SpawningShape.Sphere(1.0f, true),
      offset = ParamClasses.PairVec3f.Uniform(-0.5f, -0.5f),
      initRot = ParamClasses.PairVec3f.NonUniform(0.0f, 0.0f, 0.0f, 360.0f, 360.0f, 360.0f),
      rotVel = ParamClasses.PairVec3f.NonUniform(-0.2f, -0.2f, -0.2f, 0.2f, 0.2f, 0.2f),
      initVel = ParamClasses.PairVec3f.Null,
      initCenterVel = ParamClasses.PairFloat(0.4f, 0.5f),
      initScale = ParamClasses.PairVec3f.Uniform(1.5f, 1.0f),
      transformWithVel = ParamClasses.TransformWithVel.none,
      forceFields = emptyArray(),
      constVel = Vector3f(0.0f, 0.0f, 0.0f),
      drag = 0.075f,
      minVel = 0.0f,
      offsetCurve = ParamClasses.LerpVal.NoLerpVec3f,
      rotVelCurve = ParamClasses.LerpVal.LerpUniform(1.0f, 0.0f, LerpCurves.Sqrt),
      scaleCurve = ParamClasses.LerpVal.LerpUniform(0.0f, 2.0f, LerpCurves.Linear),
      blockCurve = Pair(
        arrayOf("shroomlight", "orange_concrete", "orange_stained_glass", "gray_wool", "gray_stained_glass", "light_gray_stained_glass"),
        LerpCurves.Sqrt
      ),
    )
  }
}