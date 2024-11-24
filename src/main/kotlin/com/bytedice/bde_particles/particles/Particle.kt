package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.*
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.*


// TODO: consider moving most params to emitter (such as shape, and forceFields) to store less data


class Particle(private val particleParams: ParticleParams) {
  private val initOffset = Vector3f(-0.5f, -0.5f, -0.5f)
  private var offset     = Vector3f(0.0f, 0.0f, 0.0f)
  private var pos        = Vec3d(0.0, 0.0, 0.0)

  private var rot    = randomBetweenVector3f(particleParams.rotRandom.first, particleParams.rotRandom.second)
  private val rotVel = randomBetweenVector3f(particleParams.rotVelRandom.first, particleParams.rotVelRandom.second)
  private var vel    = randomBetweenVector3f(particleParams.velRandom.first, particleParams.velRandom.second)
  private var scale  = randomBetweenVector3f(particleParams.sizeRandom.first, particleParams.sizeRandom.second)

  private var blockDisplay: DisplayEntity? = null
  private var lifeTime = randomIntBetween(particleParams.lifeTime.first, particleParams.lifeTime.second)

  var isDead = false
  private var timeAlive = 0

  private var isInit = false


  fun init(pos: Vec3d) {
    this.pos = pos

    if ( particleParams.uniformSize ) {
      val randomUniform = randomFloatBetween(particleParams.sizeRandom.first.x, particleParams.sizeRandom.second.x)
      scale.x = randomUniform
      scale.y = randomUniform
      scale.z = randomUniform
    }

    val shape = particleParams.shape
    val newSpawnPos: Vector3f

    when (shape) {
      is SpawningShape.CIRCLE -> { val posInCircle = randomInCircle(shape.radius); newSpawnPos = Vector3f(posInCircle.x, 0.0f, posInCircle.y) }
      is SpawningShape.SPHERE -> { newSpawnPos = randomInSphere(shape.radius) }
      is SpawningShape.RECT ->   { val posInRect = randomInRect(shape.size); newSpawnPos = Vector3f(posInRect.x, 0.0f, posInRect.y) }
      is SpawningShape.CUBE ->   { newSpawnPos = randomInCube(shape.size) }
      is SpawningShape.POINT ->  { newSpawnPos = Vector3f(0.0f, 0.0f, 0.0f) }
    }

    offset = newSpawnPos

    val (quatRot, newOffset) = calcTransformOffset(rot, initOffset, scale)

    val properties = DisplayEntityProperties(
      pos          = this.pos,
      blockType    = particleParams.blockCurve[0],
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

    fun calcBlockCurve(timeAliveClamped: Float) : String {
      val newBlockIdx = round(lerp(0.0f, particleParams.blockCurve.lastIndex.toFloat(), timeAliveClamped)).toInt()
      return particleParams.blockCurve[newBlockIdx]
    }

    fun sphereSdfVel(forceField: ForceField) : Vector3f {
      if (forceField.shape !is ForceFieldShape.SPHERE) { return Vector3f(0.0f, 0.0f, 0.0f) }
      val shape = forceField.shape

      val sdfVal        = sdfSphere(forceField.pos, shape.radius, offset)
      val normalizedSdf = normalizeSdf(sdfVal, shape.radius)
      val velDir        = Vector3f(offset).sub(forceField.pos).normalize()
      val velMul = if (sdfVal > 0) { 0.0f }
      else { lerp(shape.force.second, shape.force.second, 1 - normalizedSdf) }

      return velDir.mul(velMul)
    }

    fun cubeSdfVel(forceField: ForceField) : Vector3f {
      if (forceField.shape !is ForceFieldShape.CUBE) { return Vector3f(0.0f, 0.0f, 0.0f) }
      val shape = forceField.shape

      val sdfVal = sdfCube(forceField.pos, shape.size, offset)
      val velDir = if (sdfVal < 0) { shape.forceDir }
      else { Vector3f(0.0f, 0.0f, 0.0f) }

      return velDir
    }

    fun calcRot(timeAliveClamped: Float) : Vector3f {
      val newRot = Vector3f(rot)
      val newVel = Vector3f(rotVel)
      val rotCurve = particleParams.rotVelCurve

      newVel.mul(interpolateCurve(rotCurve.first, rotCurve.second, timeAliveClamped, rotCurve.third))
      newRot.add(newVel)
      return newRot
    }

    fun calcOffset() : Pair<Vector3f, Vector3f> {
      var vel = Vector3f(vel)
      vel = vel.add(particleParams.gravity)
      vel = vel.mul(1.0f - particleParams.drag)

      if (abs(vel.x) < particleParams.minVel) { vel.x = 0.0f }
      if (abs(vel.y) < particleParams.minVel) { vel.y = 0.0f }
      if (abs(vel.z) < particleParams.minVel) { vel.z = 0.0f }

      var newOffset = Vector3f(offset)
      newOffset = newOffset.add(vel)
      return Pair(vel, newOffset)
    }

    fun calcSize(timeAliveClamped: Float) : Vector3f {
      val scale = Vector3f(scale)
      val sizeCurve = particleParams.sizeCurve
      scale.mul(interpolateCurve(sizeCurve.first, sizeCurve.second, timeAliveClamped, sizeCurve.third))
      return scale
    }

    val timeAliveClamped = (timeAlive.toFloat() / lifeTime.toFloat()).coerceIn(0.0f, 1.0f)

    val newBlock            = calcBlockCurve(timeAliveClamped)
    val (newVel, newOffset) = calcOffset()
    val newRot              = calcRot(timeAliveClamped)
    val newScale            = calcSize(timeAliveClamped)

    vel    = newVel
    offset = newOffset
    rot    = newRot

    for (forceField in particleParams.forceFields) {
      if (forceField.shape is ForceFieldShape.SPHERE) { vel.add(sphereSdfVel(forceField)) }
      else if (forceField.shape is ForceFieldShape.CUBE) { vel.add(cubeSdfVel(forceField)) }
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


  fun kill() {
    blockDisplay?.kill()
  }
}