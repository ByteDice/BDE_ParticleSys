package com.bytedice.bde_particles.particle

val emitterIdRegister: MutableMap<String, ParticleEmitterParams> = mutableMapOf()
val forbiddenIds = arrayOf("", "DEFAULT", "NULL")


fun addToEmitterRegister(id: String, params: ParticleEmitterParams) : Pair<String, Boolean> {
  val newId = id.replace(" ", "_")

  if (newId in forbiddenIds && newId != "DEFAULT") {return Pair(newId, false) }
  if (emitterIdRegister.containsKey(newId))        {return Pair(newId, false) }

  emitterIdRegister[id] = params
  return Pair(newId, true)
}


fun removeFromEmitterRegister(id: String) : Boolean {
  if (id in forbiddenIds)                 { return false }
  if (!emitterIdRegister.containsKey(id)) { return false }

  emitterIdRegister.remove(id)
  return true
}


fun getParticleEmitterParams(id: String) : ParticleEmitterParams? {
  return emitterIdRegister[id]
}