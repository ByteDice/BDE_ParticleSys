package com.bytedice.bde_particles.particle

val emitterIdRegister: MutableMap<String, ParticleEmitterParams> = mutableMapOf()

fun returnParticleFunction(id: String) : ParticleEmitterParams? {
  if (emitterIdRegister.containsKey(id)) {
    return emitterIdRegister[id]
  }

  return null
}


fun addToEmitterRegister(id: String, params: ParticleEmitterParams) : Pair<String, String> {
  val newId = id.replace(" ", "_")

  if (!emitterIdRegister.containsKey(id)) {
    emitterIdRegister[id] = params

    val msg = "BPS - Successfully added Emitter ID \"$newId\" to register"
    println(msg)
    return Pair(msg, newId)
  }
  else {
    val msg = "BPS - Emitter ID \"$newId\" is already registered, please choose another ID!"
    println(msg)
    return Pair(msg, newId)
  }
}


fun getParticleEmitterParams(id: String) : ParticleEmitterParams? {
  return emitterIdRegister[id]
}