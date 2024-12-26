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
  var forceFields: ParamClasses.ForceFieldArray,
  var constVel:    Vector3f,
  var drag:        Float,
  var minVel:      Float,
  // curves
  var offsetCurve:     ParamClasses.LerpValVec3f,
  var rotVelCurve:     ParamClasses.LerpValVec3f,
  var scaleCurve:      ParamClasses.LerpValVec3f,
  var modelCurve:      ParamClasses.StringCurve,
  var brightnessCurve: ParamClasses.LerpValInt,
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
      forceFields = ParamClasses.ForceFieldArray(emptyArray()),
      constVel = Vector3f(0.0f, 0.0f, 0.0f),
      drag = 0.075f,
      minVel = 0.0f,
      offsetCurve = ParamClasses.LerpValVec3f.Null,
      rotVelCurve = ParamClasses.LerpValVec3f.LerpUniform(1.0f, 0.0f, LerpCurves.Sqrt),
      scaleCurve = ParamClasses.LerpValVec3f.LerpUniform(1.0f, 2.5f, LerpCurves.Linear),
      modelCurve = ParamClasses.StringCurve(
        arrayOf(
          "shroomlight",
          "orange_concrete",
          "orange_stained_glass",
          "gray_wool",
          "gray_stained_glass",
          "light_gray_stained_glass"
        ),
        LerpCurves.Sqrt
      ),
      brightnessCurve = ParamClasses.LerpValInt.LerpInt(15, 0, LerpCurves.Sqrt)
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
      forceFields = ParamClasses.ForceFieldArray(emptyArray()),
      constVel = Vector3f(0.0f, -0.05f, 0.0f),
      drag = 0.075f,
      minVel = 0.0f,
      offsetCurve = ParamClasses.LerpValVec3f.Null,
      rotVelCurve = ParamClasses.LerpValVec3f.LerpUniform(1.0f, 0.0f, LerpCurves.Linear),
      scaleCurve = ParamClasses.LerpValVec3f.LerpUniform(1.0f, 1.0f, LerpCurves.Linear),
      modelCurve = ParamClasses.StringCurve(
        arrayOf(
          "shroomlight",
          "orange_concrete",
          "orange_stained_glass",
          "gray_wool",
          "gray_stained_glass",
          "light_gray_stained_glass"
        ),
        LerpCurves.Linear
      ),
      brightnessCurve = ParamClasses.LerpValInt.Null
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
      forceFields = ParamClasses.ForceFieldArray(
        arrayOf(ForceField(
          Vector3f(0.0f, 75.0f, 0.0f),
          ForceFieldShape.Sphere(15.0f, Pair(0.1f, 0.2f))
        ))
      ),
      constVel = Vector3f(0.0f, 0.00001f, 0.0f),
      drag = 0.075f,
      minVel = 0.0f,
      offsetCurve = ParamClasses.LerpValVec3f.Null,
      rotVelCurve = ParamClasses.LerpValVec3f.LerpUniform(1.0f, 0.0f, LerpCurves.Linear),
      scaleCurve = ParamClasses.LerpValVec3f.LerpUniform(1.0f, 1.5f, LerpCurves.Linear),
      modelCurve = ParamClasses.StringCurve(
        arrayOf(
          "shroomlight",
          "orange_concrete",
          "orange_stained_glass",
          "gray_wool",
          "gray_stained_glass",
          "light_gray_stained_glass"
        ),
        LerpCurves.Linear
      ),
      brightnessCurve = ParamClasses.LerpValInt.Null
    )
  }
}