package com.bytedice.bde_particles.particle

// TODO: order these correctly
// loop:          false means it will shoot as many particles as spawnsPerTick * loopDur, true means it will loop
// loopDur:       0 or below means no particles will spawn
// loopDelay:     if loop is not true then this won't do anything, negative values are treated as 0
// loopCount:     if loop is not true then this won't do anything, -1 is treated as infinite
// spawnsPerTick: 0 or below means no particles will spawn
// maxCount:      0 or below means no particles will spawn
// particleTypes: empty means no particles will spawn
data class ParticleEmitterParams (
  val maxCount:      Int             = 100,
  val spawnsPerTick: Int             = 1,
  val particleTypes: Array<Particle> = emptyArray(),
  val loop:          Boolean         = false,
  val loopDur:       Int             = 20,
  val loopDelay:     Int             = 10,
  val loopCount:     Int             = 2,
)