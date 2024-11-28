@file:Suppress("UNCHECKED_CAST")

package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.LerpCurves
import org.joml.Vector3f


val emitterIdRegister: MutableMap<String, EmitterParams> = mutableMapOf()
val forbiddenIds = arrayOf("", "DEFAULT", "NULL")


fun addToEmitterRegister(id: String, params: EmitterParams) : Pair<String, Boolean> {
  val newId = id.replace(" ", "_")

  if (newId in forbiddenIds && newId != "DEFAULT") { return Pair(newId, false) }
  if (emitterIdRegister.containsKey(newId))        { return Pair(newId, false) }

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


fun updateSingleEmitterParam(id: String, paramName: String, paramValue: Any) : Boolean {
  val emitter = getEmitterParams(id) ?: return false
  val updatedValues = updateSingleEmitterParam(emitter, paramName, paramValue)
  updateEmitterParams(id, updatedValues.first)

  return updatedValues.second
}


fun updateSingleParticleParam(emitterId: String, paramName: String, paramValue: Any) : Boolean {
  val emitter = getEmitterParams(emitterId) ?: return false
  val particle = emitter.particle

  val updatedValues = updateSingleParticleParam(particle, paramName, paramValue)

  emitter.particle = updatedValues.first
  updateEmitterParams(emitterId, emitter)

  return updatedValues.second
}


fun updateSingleEmitterParam(params: EmitterParams, paramName: String, paramValue: Any) : Pair<EmitterParams, Boolean> {
  val value: Any? = when (paramName) {
    "maxCount"      -> paramValue as Int
    "spawnsPerTick" -> paramValue as Int
    "loopDur"       -> paramValue as Int
    "loopDelay"     -> paramValue as Int
    "loopCount"     -> paramValue as Int
    else            -> null
  }

  if (value == null) { return Pair(params, false) }

  val newParams = params.copy()

  when (paramName) {
    "maxCount"      -> newParams.maxCount      = value as Int
    "spawnsPerTick" -> newParams.spawnsPerTick = value as Int
    "loopDur"       -> newParams.loopDur       = value as Int
    "loopDelay"     -> newParams.loopDelay     = value as Int
    "loopCount"     -> newParams.loopCount     = value as Int
    else            -> return Pair(params, false)
  }

  return Pair(newParams, true)
}


fun updateSingleParticleParam(params: ParticleParams, paramName: String, paramValue: Any) : Pair<ParticleParams, Boolean> {
  val value: Any? = when (paramName) {
    "shape"        -> paramValue as SpawningShape?
    "blockCurve"   -> paramValue as Array<String>
    "rotRandom"    -> paramValue as Pair<Vector3f, Vector3f>
    "rotVelRandom" -> paramValue as Pair<Vector3f, Vector3f>
    "sizeRandom"   -> paramValue as Pair<Vector3f, Vector3f>
    "uniformSize"  -> paramValue as Boolean
    "velRandom"    -> paramValue as Pair<Vector3f, Vector3f>
    "forceFields"  -> paramValue as Array<ForceField>
    "gravity"      -> paramValue as Vector3f
    "drag"         -> paramValue as Float
    "minVel"       -> paramValue as Float
    "lifeTime"     -> paramValue as Pair<Int, Int>
    "rotVelCurve"  -> paramValue as Triple<Vector3f, Vector3f, LerpCurves>
    "sizeCurve"    -> paramValue as Triple<Vector3f, Vector3f, LerpCurves>
    else           -> null
  }

  if (value == null) { return Pair(params, false) }

  val newParams = params.copy()

  when (paramName) {
    "shape"        -> newParams.shape        = value as SpawningShape
    "blockCurve"   -> newParams.blockCurve   = value as Array<String>
    "rotRandom"    -> newParams.rotRandom    = value as Pair<Vector3f, Vector3f>
    "rotVelRandom" -> newParams.rotVelRandom = value as Pair<Vector3f, Vector3f>
    "sizeRandom"   -> newParams.sizeRandom   = value as Pair<Vector3f, Vector3f>
    "uniformSize"  -> newParams.uniformSize  = value as Boolean
    "velRandom"    -> newParams.velRandom    = value as Pair<Vector3f, Vector3f>
    "forceFields"  -> newParams.forceFields  = value as Array<ForceField>
    "gravity"      -> newParams.gravity      = value as Vector3f
    "drag"         -> newParams.drag         = value as Float
    "minVel"       -> newParams.minVel       = value as Float
    "lifeTime"     -> newParams.lifeTime     = value as Pair<Int, Int>
    "rotVelCurve"  -> newParams.rotVelCurve  = value as Triple<Vector3f, Vector3f, LerpCurves>
    "sizeCurve"    -> newParams.sizeCurve    = value as Triple<Vector3f, Vector3f, LerpCurves>
    else           -> return Pair(params, false)
  }

  return Pair(newParams, true)
}