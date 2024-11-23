package com.bytedice.bde_particles.particles

/**
 * EmitterParams defines the parameters for controlling how particles are emitted,
 * including settings for looping, spawn frequency, and maximum particle count.
 *
 * @param maxCount The maximum number of particles that can exist. If set to 0 or below, no particles will spawn.
 * @param spawnsPerTick The number of particles spawned per tick. If set to 0 or below, no particles will spawn.
 * @param loopDur The duration for which the loop runs, in ticks. A value 0 or below means the loop is infinite, and `loopDelay` and `loopCount` will be ignored.
 * @param loopDelay The delay between each loop cycle, in ticks. Negative values are treated as 0, meaning no delay.
 * @param loopCount The number of times the loop will repeat. 0 means it will shoot a single burst of particles. A value below 0 means it will repeat indefinitely.
 * @param particleTypes An array of `ParticleParams` defining the types of particles to spawn. If empty, no particles will spawn.
 */
data class EmitterParams (
  var maxCount:      Int                   = 200,
  var spawnsPerTick: Int                   = 1,
  var loopDur:       Int                   = 25,
  var loopDelay:     Int                   = 0,
  var loopCount:     Int                   = 0,
  var particleTypes: Array<ParticleParams> = arrayOf(ParticleParams()),
)
{
  companion object Presets {
    val DEFAULT = EmitterParams()
    val FIRE_GEYSER = EmitterParams(
      200,
      1,
      25,
      0,
      0,
      arrayOf(ParticleParams.DEFAULT)
    )
  }
}