package com.bytedice.bde_particles.particle

class ParticleEmitterParams {
  data class init (
    val maxCount:      Int             = 20,
    val spawnDur:      Int             = 20,
    val particleTypes: Array<Particle> = emptyArray(),
    val loop:          Boolean         = false,
    val loopDelay:     Int             = 10,
    val loopCount:     Int             = 2,
  )
}