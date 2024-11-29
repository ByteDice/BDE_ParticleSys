package com.bytedice.bde_particles.particles


val idRegister: MutableMap<String, EmitterParams> = mutableMapOf()
val forbiddenIds = arrayOf("", "NULL")


fun addToRegister(id: String, params: EmitterParams) : Pair<String, Boolean> {
  val newId = replaceForbiddenChars(id.replace(" ", "_"))

  if (idRegister.containsKey(newId)) {
    println("BPS - Failed to register Emitter ID \"$newId\" as it already exists!")
    return Pair(newId, false)
  }

  idRegister[id] = params
  println("BPS - Registered Emitter ID \"$newId\".")
  return Pair(newId, true)
}


fun removeFromRegister(id: String) : Boolean {
  if (id in forbiddenIds) { return false }
  if (!idRegister.containsKey(id)) { return false }

  idRegister.remove(id)
  return true
}


fun getParamsById(id: String) : EmitterParams? {
  return idRegister[id]
}


fun updateRegistered(id: String, newParams: EmitterParams) {
  idRegister[id] = newParams
}


fun updateParam(id: String, paramName: String, paramValue: Any) : Boolean {
  val emitter = getParamsById(id) ?: return false
  val updatedValues = updateParams(emitter, paramName, paramValue)

  return updatedValues.second
}


fun updateParams(params: EmitterParams, paramName: String, paramValue: Any) : Pair<EmitterParams, Boolean> { // TODO: map to new params
  val newParams = params.copy()

  when (paramName) {
    "maxCount"      -> newParams.maxCount = paramValue as Int
  }

  return Pair(newParams, true)
}


fun replaceForbiddenChars(input: String): String {
  val regex = "[^a-zA-Z0-9_-]".toRegex()
  return input.replace(regex, "-")
}