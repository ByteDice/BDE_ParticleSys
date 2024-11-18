package com.bytedice.bde_particles.particle

val particleIdRegister: MutableMap<String, ParticleEmitterParams> = mutableMapOf()

fun returnParticleFunction(id: String) : ParticleEmitterParams? {
  if (particleIdRegister.containsKey(id)) {
    return particleIdRegister[id]
  }

  return null
}


fun addToParticleRegister(id: String, params: ParticleEmitterParams) : String {
  if (!particleIdRegister.containsKey(id)) {
    particleIdRegister[id] = params

    val msg = "Successfully added particle id \"$id\" to register"
    println(msg)
    return msg
  }
  else {
    val msg = "Particle id \"$id\" is already registered, please choose another id!"
    println(msg)
    return msg
  }
}


fun getParticleEmitterParams(id: String) : ParticleEmitterParams? {
  return particleIdRegister[id]
}