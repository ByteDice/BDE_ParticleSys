package com.bytedice.bde_particles.particle

data class ParticleEmitterParams (
  val maxCount:      Int             = 100,
  val spawnsPerTick: Int             = 1,
  val particleTypes: Array<Particle> = emptyArray(),
  val loop:          Boolean         = false,
  val loopDur:       Int             = 1,
  val loopDelay:     Int             = 10,
  val loopCount:     Int             = 2,
)