package com.bytedice.bde_particles.particle

import com.bytedice.bde_particles.DisplayEntity
import com.bytedice.bde_particles.DisplayEntityProperties
import com.bytedice.bde_particles.math.*
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import kotlin.math.round


class Particle(private val particleParams: ParticleParams) {
  private val initOffset = Vector3f(-0.5f, -0.5f, -0.5f)
  private var offset     = Vector3f(0.0f, 0.0f, 0.0f)
  private var pos        = Vec3d(0.0, 0.0, 0.0)

  private val rot    = randomBetweenVector3f(particleParams.rotRandom.first, particleParams.rotRandom.second)
  private val rotVel = randomBetweenVector3f(particleParams.rotVelRandom.first, particleParams.rotVelRandom.second)
  private val scale  = randomBetweenVector3f(particleParams.sizeRandom.first, particleParams.sizeRandom.second)
  private var vel    = randomBetweenVector3f(particleParams.velRandom.first, particleParams.velRandom.second)

  private var blockDisplay: DisplayEntity? = null

  private val quatRot     = eulerToQuat(rot)
  private val scaleOffset = transformOffsetByScale(initOffset, scale)
  private val rotOffset   = transformOffsetByQuat(scaleOffset, quatRot)

  var isDead = false
  private var timeAlive = 0

  private var isInit = false


  fun init(pos: Vec3d) {
    this.pos = pos

    val properties = DisplayEntityProperties(
      pos          = this.pos,
      blockType    = particleParams.blockCurve[0],
      translation  = rotOffset,
      leftRotation = quatRot,
      scale        = scale,
      tags         = arrayOf("BPS_Particle")
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

    val newProperties = calculateNewProperties()

    blockDisplay?.updateProperties(newProperties)

    if (timeAlive > particleParams.lifeTime) { isDead = true; blockDisplay?.kill() }

    timeAlive += 1
  }


  private fun calculateNewProperties() : DisplayEntityProperties {
    fun calculateBlockCurve() : String {
      val newBlockIdx = round(
        lerp(
          0.0f,
          particleParams.blockCurve.lastIndex.toFloat(),
          (timeAlive.toFloat() / particleParams.lifeTime.toFloat()).coerceIn(0.0f, 1.0f)
        )
      ).toInt()

      return particleParams.blockCurve[newBlockIdx]
    }

    fun calculateOffset() : Vector3f {
      vel = vel.add(particleParams.gravity)
      vel = vel.mul(1.0f - particleParams.drag)

      offset = offset.add(vel)
      return rotOffset.add(offset)
    }


    val newBlock  = calculateBlockCurve()
    val newOffset = calculateOffset()

    val newProperties = DisplayEntityProperties(
      pos          = pos,
      blockType    = newBlock,
      translation  = newOffset,
      leftRotation = quatRot,
      scale        = scale,
      tags         = arrayOf("BPS_Particle")
    )

    return newProperties
  }
}