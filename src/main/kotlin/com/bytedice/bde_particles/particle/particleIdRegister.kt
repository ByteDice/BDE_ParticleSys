package com.bytedice.bde_particles.particle

val emitterIdRegister: MutableMap<String, EmitterParams> = mutableMapOf()
val forbiddenIds = arrayOf("", "DEFAULT", "NULL")


fun addToEmitterRegister(id: String, params: EmitterParams) : Pair<String, Boolean> {
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


fun getEmitterParams(id: String) : EmitterParams? {
  return emitterIdRegister[id]
}


fun updateEmitterParams(id: String, newParams: EmitterParams) {
  emitterIdRegister[id] = newParams
}