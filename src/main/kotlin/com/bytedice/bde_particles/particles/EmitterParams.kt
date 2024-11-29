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
  var offset:         ParamClasses.PairVec3f,
  var initRot:        ParamClasses.PairVec3f,
  var rotVel:         ParamClasses.PairVec3f,
  var rotWithVel:     Boolean,
  var initVel:        ParamClasses.PairVec3f,
  var initCenterVel:  ParamClasses.PairFloat,
  var initScale:      ParamClasses.PairVec3f,
  var scaleWithVel:   Boolean,
  // velocity
  var forceFields:    Array<ForceField>,
  var constVel:       Vector3f,
  var drag:           Float,
  var minVel:         Float,
  // curves
  var offsetCurve:    ParamClasses.LerpVal,
  var rotVelCurve:    ParamClasses.LerpVal,
  var scaleCurve:     ParamClasses.LerpVal,
  var blockCurve:     Pair<Array<String>, LerpCurves>,
)
{
  companion object Presets {
    val DEFAULT = EmitterParams(
      maxCount = 200,
      spawnRate = 1,
      spawnChance = 1.0f,
      spawnDuration = ParamClasses.Duration.SingleBurst(25),
      spawnPosOffset = Vector3f(0.0f, 0.0f, 0.0f),
      lifeTime = ParamClasses.PairInt(25, 40),
      shape = SpawningShape.Circle(3.0f, false),
      offset = ParamClasses.PairVec3f.Uniform(-0.5f, -0.5f),
      initRot = ParamClasses.PairVec3f.Null,
      rotVel = ParamClasses.PairVec3f.NonUniform(-0.2f, -0.2f, -0.2f, 0.2f, 0.2f, 0.2f),
      rotWithVel = true,
      initVel = ParamClasses.PairVec3f.Null,
      initCenterVel = ParamClasses.PairFloat(0.1f, 0.3f),
      initScale = ParamClasses.PairVec3f.Uniform(1.0f, 0.2f),
      scaleWithVel = true,
      forceFields = emptyArray(),
      constVel = Vector3f(0.0f, -0.01f, 0.0f),
      drag = 0.075f,
      minVel = 0.0f,
      offsetCurve = ParamClasses.LerpVal.NoLerpVec3f(Vector3f(-0.5f, -0.5f, -0.5f)),
      rotVelCurve = ParamClasses.LerpVal.LerpUniform(1.0f, 0.0f, LerpCurves.Sqrt),
      scaleCurve = ParamClasses.LerpVal.LerpUniform(1.0f, 0.5f, LerpCurves.Linear),
      blockCurve = Pair(
        arrayOf("minecraft:purple_concrete", "minecraft:purple_stained_glass"),
        LerpCurves.Sqrt
      ),
    )
  }
}