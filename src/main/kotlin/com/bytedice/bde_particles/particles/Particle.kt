package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.*
import net.minecraft.particle.DustParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.*


class Particle(
  private val emitterParams: EmitterParams,
  private var entityPos: Vec3d,
  private val entityRot: Vector2f,
  private var debug: Boolean
) {
  private var pos = Vector3f(0.0f, 0.0f, 0.0f)
  private var timeAlive = 0
  private var isInit = false
  var isDead = false

  // params
  private val originOffset = when (val offset = emitterParams.offset) {
    is ParamClasses.PairVec3f.Uniform -> offset.randomize()
    is ParamClasses.PairVec3f.NonUniform -> offset.randomize()
    ParamClasses.PairVec3f.Null -> Vector3f(0.0f, 0.0f, 0.0f)
  }

  private var rot = when (val initRot = emitterParams.initRot) {
    is ParamClasses.PairVec3f.Uniform -> initRot.randomize()
    is ParamClasses.PairVec3f.NonUniform -> initRot.randomize()
    ParamClasses.PairVec3f.Null -> Vector3f(0.0f, 0.0f, 0.0f)
  }

  private var rotVel = when (val initRotVel = emitterParams.rotVel) {
    is ParamClasses.PairVec3f.Uniform -> initRotVel.randomize()
    is ParamClasses.PairVec3f.NonUniform -> initRotVel.randomize()
    ParamClasses.PairVec3f.Null -> Vector3f(0.0f, 0.0f, 0.0f)
  }

  private var vel = when (val initVel = emitterParams.initVel) {
    is ParamClasses.PairVec3f.Uniform -> initVel.randomize()
    is ParamClasses.PairVec3f.NonUniform -> initVel.randomize()
    ParamClasses.PairVec3f.Null -> Vector3f(0.0f, 0.0f, 0.0f)
  }

  private var scale = when (val initScale = emitterParams.initScale) {
    is ParamClasses.PairVec3f.Uniform -> initScale.randomize()
    is ParamClasses.PairVec3f.NonUniform -> initScale.randomize()
    ParamClasses.PairVec3f.Null -> Vector3f(0.0f, 0.0f, 0.0f)
  }

  private var lifeTime = emitterParams.lifeTime.randomize()

  private lateinit var particleEntity: DisplayEntity

  fun init() {
    val so = emitterParams.spawnPosOffset
    entityPos = entityPos.add(so.x.toDouble(), so.y.toDouble(), so.z.toDouble())

    val shape = emitterParams.shape
    val newSpawnPos = shape.random(shape)

    pos = newSpawnPos

    val posDouble = Vec3d(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())
    val dirFromCenter = posDouble.normalize()
    val forceFromCenter = dirFromCenter.multiply(emitterParams.initCenterVel.randomize().toDouble())
    vel.add(forceFromCenter.x.toFloat(), forceFromCenter.y.toFloat(), forceFromCenter.z.toFloat())

    val (quatRot, newPos) = calcTransformOffset(this.rot, originOffset, scale)

    val properties = DisplayEntityProperties(
      pos          = entityPos,
      rot          = entityRot,
      model        = lerpArray(emitterParams.modelCurve.first as Array<Any>, 0.0f, emitterParams.modelCurve.second) as String,
      translation  = newPos.add(pos),
      leftRotation = quatRot,
      scale        = Vector3f(scale).mul(emitterParams.scaleCurve.lerpToVector3f(0.0f)),
      tags         = arrayOf("BPS_Particle", "BPS_UUID", SESSION_UUID.toString())
    )

    particleEntity = DisplayEntity(properties)

    isInit = true
  }


  fun spawn(world: ServerWorld) {
    if (isInit) {
      particleEntity.spawn(world)
      LIVING_PARTICLE_COUNT += 1
    }
    else { error("Particle is not initialized before being spawned. Please use Particle.init() to initialize it.") }
  }


  fun tick(world: ServerWorld) {
    if (!isInit) { error("Particle is not initialized before being ticked. Please use Particle.init() to initialize it.") }

    val newProperties = calcNewProperties()

    particleEntity.updateProperties(newProperties)

    if (timeAlive > lifeTime) { kill() }

    if (debug) {
      tickDebug(world)
    }

    timeAlive += 1
  }


  private fun calcTransformOffset(eulerRot: Vector3f, offset: Vector3f, scale: Vector3f): Pair<Vector4f, Vector3f> {
    val eulerRotCopy = Vector3f(eulerRot)
    val offsetCopy = Vector3f(offset)
    val scaleCopy = Vector3f(scale)

    val quatRot = eulerToQuat(eulerRotCopy)
    val scaleOffset = transformOffsetByScale(offsetCopy, scaleCopy)
    val rotOffset = transformOffsetByQuat(scaleOffset, quatRot)

    return Pair(quatRot, rotOffset)
  }


  private fun calcNewProperties() : DisplayEntityProperties {
    val timeAliveClamped = (timeAlive.toFloat() / lifeTime.toFloat()).coerceIn(0.0f, 1.0f)

    val newModel         = lerpArray(emitterParams.modelCurve.first as Array<Any>, timeAliveClamped, emitterParams.modelCurve.second) as String
    val (newVel, newPos) = calcPos()
    val newOriginOffset  = Vector3f(originOffset.mul(emitterParams.offsetCurve.lerpToVector3f(timeAliveClamped)))

    val newScale: Vector3f
    val newRot: Vector3f

    when (emitterParams.transformWithVel) {
      is ParamClasses.TransformWithVel.RotOnly -> {
        newScale = calcScale(timeAliveClamped)
        newRot = (emitterParams.transformWithVel as ParamClasses.TransformWithVel.RotOnly).velToRot(vel)
      }
      is ParamClasses.TransformWithVel.ScaleAndRot -> {
        newScale = Vector3f(scale).mul((emitterParams.transformWithVel as ParamClasses.TransformWithVel.ScaleAndRot).velToScale(vel))
        newRot = (emitterParams.transformWithVel as ParamClasses.TransformWithVel.ScaleAndRot).velToRot(vel)
      }
      else -> {
        newScale = calcScale(timeAliveClamped)
        newRot = calcRot(timeAliveClamped)
        rot = newRot
      }
    }

    vel = newVel
    pos = newPos


    for (forceField in emitterParams.forceFields) {
      if (forceField.shape is ForceFieldShape.Sphere) { vel.add(sphereSdfVel(forceField)) }
      else if (forceField.shape is ForceFieldShape.Cube) { vel.add(cubeSdfVel(forceField)) }
    }

    val (quatRot, transformOffset) = calcTransformOffset(newRot, originOffset, newScale)
    val combinedOffset = transformOffset.add(newPos)

    val newProperties = DisplayEntityProperties(
      pos          = entityPos,
      model        = newModel,
      translation  = combinedOffset.add(newOriginOffset),
      leftRotation = quatRot,
      scale        = newScale,
      tags         = arrayOf("BPS_Particle", "BPS_UUID", SESSION_UUID.toString())
    )

    return newProperties
  }


  private fun sphereSdfVel(forceField: ForceField) : Vector3f {
    if (forceField.shape !is ForceFieldShape.Sphere) { return Vector3f(0.0f, 0.0f, 0.0f) }
    val shape = forceField.shape

    val sdfVal        = sdfSphere(forceField.pos, shape.radius, pos)
    val normalizedSdf = normalizeSdf(sdfVal, shape.radius)
    val velDir        = Vector3f(pos).sub(forceField.pos).normalize()
    val velMul        = if (sdfVal > 0) { 0.0f }
    else { lerp(shape.force.second, shape.force.second, 1 - normalizedSdf) }

    return velDir.mul(velMul)
  }


  private fun cubeSdfVel(forceField: ForceField) : Vector3f {
    if (forceField.shape !is ForceFieldShape.Cube) { return Vector3f(0.0f, 0.0f, 0.0f) }
    val shape = forceField.shape

    val sdfVal = sdfCube(forceField.pos, shape.size, pos)
    val velDir = if (sdfVal < 0) { shape.dir.mul(shape.force) }
    else { Vector3f(0.0f, 0.0f, 0.0f) }

    return velDir
  }


  private fun calcRot(t: Float) : Vector3f {
    val newRot = Vector3f(rot)
    val newVel = Vector3f(rotVel)

    newVel.mul(emitterParams.rotVelCurve.lerpToVector3f(t))
    return newRot.add(newVel)
  }


  private fun calcPos() : Pair<Vector3f, Vector3f> {
    var vel = Vector3f(vel)
    vel = vel.add(emitterParams.constVel)
    vel = vel.mul(1.0f - emitterParams.drag)

    if (abs(vel.x) < emitterParams.minVel) { vel.x = 0.0f }
    if (abs(vel.y) < emitterParams.minVel) { vel.y = 0.0f }
    if (abs(vel.z) < emitterParams.minVel) { vel.z = 0.0f }

    var newPos = Vector3f(pos)
    newPos = newPos.add(vel)
    return Pair(vel, newPos)
  }


  private fun calcScale(t: Float) : Vector3f {
    val newScale = Vector3f(scale)
    return newScale.mul(emitterParams.scaleCurve.lerpToVector3f(t))
  }


  private fun tickDebug(world: ServerWorld) {
    // origin debug
    val greenDustEffect = DustParticleEffect(Vector3f(0.0f, 1.0f, 0.0f), 2.0f)
    world.spawnParticles(
      greenDustEffect,
      entityPos.x + pos.x,
      entityPos.y + pos.y,
      entityPos.z + pos.z,
      1,
      0.0,
      0.0,
      0.0,
      0.0
    )

    // vel debug
    val redDustEffect = DustParticleEffect(Vector3f(1.0f, 0.0f, 0.0f), 1.0f)
    world.spawnParticles(
      redDustEffect,
      entityPos.x + pos.x + originOffset.x + vel.x * 10,
      entityPos.y + pos.y + originOffset.y + vel.y * 10,
      entityPos.z + pos.z + originOffset.z + vel.z * 10,
      1,
      0.0,
      0.0,
      0.0,
      0.0
    )
  }


  fun kill() {
    if (!isDead) {
      particleEntity.kill()
      LIVING_PARTICLE_COUNT -= 1
      isDead = true
    }
  }
}