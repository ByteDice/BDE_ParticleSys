package com.bytedice.bde_particles.particle

val particleIdRegister: MutableMap<String, ParticleEmitterParams> = mutableMapOf()

fun returnParticleFunction(id: String) : ParticleEmitterParams? {
  if (particleIdRegister.containsKey(id)) {
    return particleIdRegister[id]
  }

  return null
}


fun addToParticleRegister(id: String, params: ParticleEmitterParams) {
  particleIdRegister[id] = params
}