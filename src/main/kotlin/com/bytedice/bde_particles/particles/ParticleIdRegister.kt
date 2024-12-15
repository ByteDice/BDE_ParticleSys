package com.bytedice.bde_particles.particles

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf


val idRegister: MutableMap<String, EmitterParams> = mutableMapOf()
val forbiddenIds = arrayOf("", "NULL")


fun addToRegister(id: String, params: EmitterParams) : Pair<String, Boolean> {
  val newId = replaceForbiddenChars(id.replace(" ", "_"))

  if (idRegister.containsKey(newId)) {
    println("BPS - Failed to register Emitter ID \"$newId\" as it already exists!")
    return Pair(newId, false)
  }

  if (newId in forbiddenIds) {
    println("BPS - Failed to register Emitter ID \"$newId\" as it's been reserved for system use!")
    return Pair(newId, false)
  }

  val newModels: MutableList<String> = mutableListOf()

  for (model in params.modelCurve.array) {
    val modelModIdRegex = ".*:".toRegex()
    if (modelModIdRegex.containsMatchIn(model)) { newModels.add(model) }
    else { newModels.add("minecraft:$model") }
  }

  params.modelCurve.array = newModels.toTypedArray()

  idRegister[id] = params
  println("BPS - Registered Emitter ID \"$newId\".")
  return Pair(newId, true)
}


fun removeFromRegister(id: String) : Boolean {
  if (!idRegister.containsKey(id)) {
    println("BPS - Failed to remove Emitter ID \"$id\" as it doesn't exist!")
    return false
  }
  if (id in forbiddenIds) {
    println("BPS - Failed to remove Emitter ID \"$id\" as it's been reserved for system use!")
    return false
  }

  idRegister.remove(id)
  println("BPS - Removed Emitter ID \"$id\" from register.")
  return true
}


fun getEmitterDataById(id: String) : EmitterParams? {
  return idRegister[id]
}


fun updateRegistered(id: String, newParams: EmitterParams) {
  idRegister[id] = newParams
}


fun updateParam(id: String, paramAccess: KProperty1<EmitterParams, *>, paramValue: Any) : Boolean {
  val emitter = getEmitterDataById(id) ?: return false
  val updatedValues = updateParams(emitter, paramAccess as KProperty1<EmitterParams, Any>, paramValue)

  updateRegistered(id, updatedValues.first)

  return updatedValues.second
}


fun updateParams(params: EmitterParams, paramAccess: KProperty1<EmitterParams, Any>, paramValue: Any): Pair<EmitterParams, Boolean> {
  val newParams = params.copy()

  if (paramAccess.returnType.classifier == paramValue::class
    || (paramAccess.returnType.classifier as? KClass<*>)?.let { paramValue::class.isSubclassOf(it) } == true
    )
  {
    paramAccess as KMutableProperty1<EmitterParams, Any>
    paramAccess.set(newParams, paramValue)
    return Pair(newParams, true)
  }

  return Pair(params, false)
}


fun replaceForbiddenChars(input: String): String {
  val regex = "[^a-zA-Z0-9_-]".toRegex()
  return input.replace(regex, "-")
}