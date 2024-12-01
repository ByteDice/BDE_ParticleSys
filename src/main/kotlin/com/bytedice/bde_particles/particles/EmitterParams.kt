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
  var modelCurve:  Pair<Array<String>, LerpCurves>,
)
{
  companion object Presets {
    val DEFAULT = EmitterParams(
      maxCount = 200,
      spawnRate = 50,
      spawnChance = 1.0f,
      spawnDuration = ParamClasses.Duration.SingleBurst(4),
      spawnPosOffset = Vector3f(0.0f, 0.0f, 0.0f),
      lifeTime = ParamClasses.PairInt(25, 40),
      shape = SpawningShape.Sphere(1.0f, true),
      offset = ParamClasses.PairVec3f.Uniform(0.0f, 0.0f),
      initRot = ParamClasses.PairVec3f.NonUniform(0.0f, 0.0f, 0.0f, 360.0f, 360.0f, 360.0f),
      rotVel = ParamClasses.PairVec3f.NonUniform(-0.2f, -0.2f, -0.2f, 0.2f, 0.2f, 0.2f),
      initVel = ParamClasses.PairVec3f.Null,
      initCenterVel = ParamClasses.PairFloat(0.4f, 0.5f),
      initScale = ParamClasses.PairVec3f.Uniform(0.5f, 1.5f),
      transformWithVel = ParamClasses.TransformWithVel.None,
      forceFields = emptyArray(),
      constVel = Vector3f(0.0f, 0.0f, 0.0f),
      drag = 0.075f,
      minVel = 0.0f,
      offsetCurve = ParamClasses.LerpVal.NoLerpVec3f,
      rotVelCurve = ParamClasses.LerpVal.LerpUniform(1.0f, 0.0f, LerpCurves.Sqrt),
      scaleCurve = ParamClasses.LerpVal.LerpUniform(1.0f, 2.5f, LerpCurves.Constant),
      modelCurve = Pair(
        arrayOf(
          "shroomlight",
          "orange_concrete",
          "orange_stained_glass",
          "gray_wool",
          "gray_stained_glass",
          "light_gray_stained_glass"
        ),
        LerpCurves.Sqrt
      )
    )
    val DEBUG = EmitterParams(
      maxCount = 50,
      spawnRate = 10,
      spawnChance = 1.0f,
      spawnDuration = ParamClasses.Duration.SingleBurst(5),
      spawnPosOffset = Vector3f(0.0f, 2.0f, 0.0f),
      lifeTime = ParamClasses.PairInt(25, 40),
      shape = SpawningShape.Circle(1.0f, true),
      offset = ParamClasses.PairVec3f.Uniform(0.0f, 0.0f),
      initRot = ParamClasses.PairVec3f.Null,
      rotVel = ParamClasses.PairVec3f.Null,
      initVel = ParamClasses.PairVec3f.NonUniform(0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f),
      initCenterVel = ParamClasses.PairFloat(1.5f, 1.5f),
      initScale = ParamClasses.PairVec3f.Uniform(2.0f, 2.0f),
      transformWithVel = ParamClasses.TransformWithVel.ScaleAndRot(0.75f),
      forceFields = emptyArray(),
      constVel = Vector3f(0.0f, -0.05f, 0.0f),
      drag = 0.075f,
      minVel = 0.0f,
      offsetCurve = ParamClasses.LerpVal.NoLerpVec3f,
      rotVelCurve = ParamClasses.LerpVal.LerpUniform(1.0f, 0.0f, LerpCurves.Constant),
      scaleCurve = ParamClasses.LerpVal.LerpUniform(1.0f, 1.0f, LerpCurves.Constant),
      modelCurve = Pair(
        arrayOf(
          "shroomlight",
          "orange_concrete",
          "orange_stained_glass",
          "gray_wool",
          "gray_stained_glass",
          "light_gray_stained_glass"
        ),
        LerpCurves.Constant
      )
    )
    val STRESS_TEST = EmitterParams(
      maxCount = 10000,
      spawnRate = 50,
      spawnChance = 1.0f,
      spawnDuration = ParamClasses.Duration.SingleBurst(100),
      spawnPosOffset = Vector3f(0.0f, 0.0f, 0.0f),
      lifeTime = ParamClasses.PairInt(75, 100),
      shape = SpawningShape.Circle(10.0f, true),
      offset = ParamClasses.PairVec3f.Uniform(0.0f, 0.0f),
      initRot = ParamClasses.PairVec3f.NonUniform(0.0f, 0.0f, 0.0f, 360.0f, 360.0f, 360.0f),
      rotVel = ParamClasses.PairVec3f.NonUniform(-0.2f, -0.2f, -0.2f, 0.2f, 0.2f, 0.2f),
      initVel = ParamClasses.PairVec3f.NonUniform(0.0f, 4.0f, 0.0f, 0.0f, 8.0f, 0.0f),
      initCenterVel = ParamClasses.PairFloat(-1.5f, -1.5f),
      initScale = ParamClasses.PairVec3f.Uniform(10.0f, 15.0f),
      transformWithVel = ParamClasses.TransformWithVel.None,
      forceFields = arrayOf(ForceField(
        "MUSHROOM_HEAD",
        Vector3f(0.0f, 75.0f, 0.0f),
        ForceFieldShape.Sphere(15.0f, Pair(0.1f, 0.2f))
      )),
      constVel = Vector3f(0.0f, 0.001f, 0.0f),
      drag = 0.075f,
      minVel = 0.0f,
      offsetCurve = ParamClasses.LerpVal.NoLerpVec3f,
      rotVelCurve = ParamClasses.LerpVal.LerpUniform(1.0f, 0.0f, LerpCurves.Constant),
      scaleCurve = ParamClasses.LerpVal.LerpUniform(1.0f, 1.5f, LerpCurves.Constant),
      modelCurve = Pair(
        arrayOf(
          "shroomlight",
          "orange_concrete",
          "orange_stained_glass",
          "gray_wool",
          "gray_stained_glass",
          "light_gray_stained_glass"
        ),
        LerpCurves.Constant
      )
    )
  }
}