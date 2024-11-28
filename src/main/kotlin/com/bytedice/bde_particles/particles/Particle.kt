package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.*
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.*


// TODO: consider moving most params to emitter (such as shape, and forceFields) to store less data


class Particle(private val emitterParams: EmitterParams) {
  private val initOffset = Vector3f(-0.5f, -0.5f, -0.5f)
  private var offset     = Vector3f(0.0f, 0.0f, 0.0f)
  private var pos        = Vec3d(0.0, 0.0, 0.0)
  private var entityRot  = Vector2f(0.0f, 0.0f)

  private var rot    = emitterParams.initRot.randomize()
  private val rotVel = emitterParams.rotVel.randomize()
  private var vel    = emitterParams.initVel.randomize()

  private val scaleAny: Any = when (emitterParams.initScale) {
    is ParamClasses.RandScale.Uniform -> { (emitterParams.initScale as ParamClasses.RandScale.Uniform).randomize() }
    is ParamClasses.RandScale.NonUniform -> { (emitterParams.initScale as ParamClasses.RandScale.NonUniform).randomize() }
  }
  private var scale = if (scaleAny is Vector3f) { scaleAny } else { Vector3f(scaleAny as Float, scaleAny, scaleAny) }

  private var blockDisplay: DisplayEntity? = null
  private var lifeTime = emitterParams.lifeTime.randomize()

  var isDead = false
  private var timeAlive = 0

  private var isInit = false


  fun init(pos: Vec3d, rot: Vector2f) { // TODO: replace initOffset with originOffset param
    this.pos = pos
    this.entityRot = rot

    val shape = emitterParams.shape
    val newSpawnPos: Vector3f

    when (shape) {
      is SpawningShape.Circle -> { val posInCircle = randomInCircle(shape.radius); newSpawnPos = Vector3f(posInCircle.x, 0.0f, posInCircle.y) }
      is SpawningShape.Sphere -> { newSpawnPos = randomInSphere(shape.radius) }
      is SpawningShape.Rect ->   { val posInRect = randomInRect(shape.size); newSpawnPos = Vector3f(posInRect.x, 0.0f, posInRect.y) }
      is SpawningShape.Cube ->   { newSpawnPos = randomInCube(shape.size) }
      is SpawningShape.Point ->  { newSpawnPos = Vector3f(0.0f, 0.0f, 0.0f) }
    }

    offset = newSpawnPos

    val (quatRot, newOffset) = calcTransformOffset(this.rot, initOffset, scale)

    val properties = DisplayEntityProperties(
      pos          = this.pos,
      rot          = rot,
      blockType    = emitterParams.blockCurve.first[0], // TODO: curve this
      translation  = newOffset.add(offset),
      leftRotation = quatRot,
      scale        = scale,
      tags         = arrayOf("BPS_Particle", "BPS_UUID", sessionUuid.toString())
    )

    blockDisplay = DisplayEntity(properties)

    isInit = true
  }


  fun spawn(world: ServerWorld) {
    if (isInit) { blockDisplay?.spawn(world) }
    else { error("Particle is not initialized before being spawned. Please use Particle.init() to initialize it.") }
  }


  fun tick() {
    if (!isInit) { error("Particle is not initialized before being ticked. Please use Particle.init() to initialize it.") }

    val newProperties = calcNewProperties()

    blockDisplay?.updateProperties(newProperties)

    if (timeAlive > lifeTime) { isDead = true; blockDisplay?.kill() }

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

    val newBlock            = calcBlockCurve(timeAliveClamped)
    val (newVel, newOffset) = calcOffset()
    val newRot              = calcRot(timeAliveClamped)
    val newScale            = calcSize(timeAliveClamped)

    vel    = newVel
    offset = newOffset
    rot    = newRot

    for (forceField in emitterParams.forceFields) {
      if (forceField.shape is ForceFieldShape.Sphere) { vel.add(sphereSdfVel(forceField)) }
      else if (forceField.shape is ForceFieldShape.Cube) { vel.add(cubeSdfVel(forceField)) }
    }

    val (quatRot, transformOffset) = calcTransformOffset(newRot, initOffset, newScale)
    val combinedOffset = transformOffset.add(newOffset)

    val newProperties = DisplayEntityProperties(
      pos          = pos,
      blockType    = newBlock,
      translation  = combinedOffset,
      leftRotation = quatRot,
      scale        = newScale,
      tags         = arrayOf("BPS_Particle", "BPS_UUID", sessionUuid.toString())
    )

    return newProperties
  }


  private fun calcBlockCurve(t: Float) : String {
    val newBlockIdx = round(lerp(0.0f, emitterParams.blockCurve.first.lastIndex.toFloat(), t)).toInt()
    return emitterParams.blockCurve.first[newBlockIdx]
  }


  private fun sphereSdfVel(forceField: ForceField) : Vector3f {
    if (forceField.shape !is ForceFieldShape.Sphere) { return Vector3f(0.0f, 0.0f, 0.0f) }
    val shape = forceField.shape

    val sdfVal        = sdfSphere(forceField.pos, shape.radius, offset)
    val normalizedSdf = normalizeSdf(sdfVal, shape.radius)
    val velDir        = Vector3f(offset).sub(forceField.pos).normalize()
    val velMul = if (sdfVal > 0) { 0.0f }
    else { lerp(shape.force.second, shape.force.second, 1 - normalizedSdf) }

    return velDir.mul(velMul)
  }


  private fun cubeSdfVel(forceField: ForceField) : Vector3f {
    if (forceField.shape !is ForceFieldShape.Cube) { return Vector3f(0.0f, 0.0f, 0.0f) }
    val shape = forceField.shape

    val sdfVal = sdfCube(forceField.pos, shape.size, offset)
    val velDir = if (sdfVal < 0) { shape.dir.mul(shape.force) }
    else { Vector3f(0.0f, 0.0f, 0.0f) }

    return velDir
  }
  // TODO: cone & cylinder SDF


  private fun calcRot(t: Float) : Vector3f {
    val newRot = Vector3f(rot)
    val newVel = Vector3f(rotVel)

    newVel.mul(emitterParams.rotVelCurve.lerpToVector3f(t))
    newRot.add(newVel)
    return newRot
  }


  private fun calcOffset() : Pair<Vector3f, Vector3f> {
    var vel = Vector3f(vel)
    vel = vel.add(emitterParams.constVel)
    vel = vel.mul(1.0f - emitterParams.drag)

    if (abs(vel.x) < emitterParams.minVel) { vel.x = 0.0f }
    if (abs(vel.y) < emitterParams.minVel) { vel.y = 0.0f }
    if (abs(vel.z) < emitterParams.minVel) { vel.z = 0.0f }

    var newOffset = Vector3f(offset)
    newOffset = newOffset.add(vel)
    return Pair(vel, newOffset)
  }


  private fun calcSize(t: Float) : Vector3f {
    val newScale = Vector3f(scale)
    newScale.mul(emitterParams.scaleCurve.lerpToVector3f(t))
    return scale
  }


  fun kill() {
    if (!isDead) { blockDisplay?.kill() }
  }
}