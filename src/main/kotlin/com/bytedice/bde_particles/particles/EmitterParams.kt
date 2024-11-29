package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.LerpCurves
import org.joml.Vector3f


/**
 * EmitterParams defines the parameters for controlling how particles are emitted,
 * including settings for looping, spawn frequency, and maximum particle count.
 *
 * @param maxCount The maximum number of particles that can exist at any time. If set to 0 or below, no particles will spawn.
 * @param spawnRate The number of particles to spawn each tick. If set to 0 or below, no particles will spawn.
 * @param spawnChance The chance for a particle to spawn. 0.5 means a particle has a 50% chance of spawning. Values are between 0.0 and 1.0.
 * @param loopDur The duration, in ticks, that each emission loop lasts. A value of 0 or below makes the loop infinite, ignoring `loopDelay` and `loopCount`.
 * @param loopDelay The delay, in ticks, between consecutive loops. Negative values are treated as 0 (no delay).
 * @param loopCount The number of times the loop repeats. A value of 0 results in a single burst of particles. Values below 0 make the loop repeat indefinitely.
 * @param shape The spawning shape for the particles, such as `SpawningShape.Sphere()`.
 * @param initRot Specifies the initial random rotation range for the particles, in degrees, for the X, Y, and Z axes.
 * @param rotVel Specifies the random range for rotational velocity, in degrees per tick, for the X, Y, and Z axes.
 * @param rotTowardVel If true, particles will face the direction of velocity. It uses `initRot` as an offset rotation and ignores `rotVel`.
 * @param initVel Specifies the random range for the initial velocity, in units per tick, for the X, Y, and Z axes.
 * @param initScale Defines the random range for the initial particle size.
 * @param scaleTowardVel If true, particles will scale to the direction of velocity.
 * @param forceFields An array of force fields applied to particles, affecting their motion during their lifetime.
 * @param constVel A constant velocity vector applied to particles each tick, useful for effects like gravity.
 * @param drag A drag coefficient that slows down particle velocity over time. Calculated as `velocity * (1.0 - drag)`.
 * @param minVel The minimum speed a particle must maintain. Particles slower than this value are stopped. Values <= 0 disable this check.
 * @param lifeTime Specifies the range for particle lifetime, in ticks. Values below 1 prevent particles from spawning.
 * @param rotVelCurve A curve defining how rotational velocity evolves during the particle's lifetime. Multiplies the `rotVel` values.
 * @param scaleCurve A curve defining how the particle's size evolves over its lifetime. Multiplies the `initScale` values.
 * @param blockCurve Specifies an array of block names that the particle transitions through during its lifetime, combined with a curve to control transitions. Defaults to "minecraft:air" if empty.
 */
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
      shape = SpawningShape.Circle(3.0f),
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