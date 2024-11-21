package com.bytedice.bde_particles.particle

import org.joml.Vector3f

/**
 * ParticleEmitterParams defines the parameters for controlling how particles are emitted,
 * including settings for looping, spawn frequency, and maximum particle count.
 *
 * @param maxCount The maximum number of particles that can exist. If set to 0 or below, no particles will spawn.
 * @param spawnsPerTick The number of particles spawned per tick. If set to 0 or below, no particles will spawn.
 * @param loopDur The duration for which the loop runs, in ticks. A value 0 or below means the loop is infinite, and `loopDelay` and `loopCount` will be ignored.
 * @param loopDelay The delay between each loop cycle, in ticks. This only takes effect if `loop` is true. Negative values are treated as 0, meaning no delay.
 * @param loopCount The number of times the loop will repeat. 0 means it will shoot a single burst of particles. A value below 0 means it will repeat indefinitely.
 * @param particleTypes An array of `ParticleParams` defining the types of particles to spawn. If empty, no particles will spawn.
 */
data class ParticleEmitterParams (
  val maxCount:      Int                   = 200,
  val spawnsPerTick: Int                   = 2,
  val loopDur:       Int                   = 25,
  val loopDelay:     Int                   = 10,
  val loopCount:     Int                   = 3,
  val particleTypes: Array<ParticleParams> = arrayOf(ParticleParams()),
)
{
  companion object Presets {
    val DEFAULT = ParticleEmitterParams()
    val FIRE_GEYSER = ParticleEmitterParams(
      200,
      1,
      25,
      0,
      0,
      arrayOf(ParticleParams.FIRE_GEYSER)
    )
  }
}