package com.bytedice.bde_particles.particle

import com.bytedice.bde_particles.DisplayEntity
import com.bytedice.bde_particles.DisplayEntityProperties
import com.bytedice.bde_particles.math.*
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import kotlin.math.round


class Particle(private val particleParams: ParticleParams) {
  private val rot = Vector3f(
    randomFloatBetween(particleParams.rotRandom.first.x, particleParams.rotRandom.second.x),
    randomFloatBetween(particleParams.rotRandom.first.y, particleParams.rotRandom.second.x),
    randomFloatBetween(particleParams.rotRandom.first.z, particleParams.rotRandom.second.z)
  )
  private val rotVel = Vector3f(
    randomFloatBetween(particleParams.rotVelRandom.first.x, particleParams.rotVelRandom.second.x),
    randomFloatBetween(particleParams.rotVelRandom.first.y, particleParams.rotVelRandom.second.x),
    randomFloatBetween(particleParams.rotVelRandom.first.z, particleParams.rotVelRandom.second.z)
  )
  private val scale = Vector3f(
    randomFloatBetween(particleParams.sizeRandom.first.x, particleParams.sizeRandom.second.x),
    randomFloatBetween(particleParams.sizeRandom.first.y, particleParams.sizeRandom.second.x),
    randomFloatBetween(particleParams.sizeRandom.first.z, particleParams.sizeRandom.second.z)
  )
  private var blockDisplay: DisplayEntity? = null

  var isDead = false
  private var timeAlive = 0

  private var isInit = false

  fun init(pos: Vec3d) {
    val offset = Vector3f(-0.5f, -0.5f, -0.5f)
    val quatRot = eulerToQuat(rot)

    val scaleOffset = transformOffsetByScale(offset, scale)
    val rotOffset = transformOffsetByQuat(scaleOffset, quatRot)

    val properties = DisplayEntityProperties(
      pos          = pos,
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


  fun tick(world: ServerWorld) {
    if (!isInit) { error("Particle is not initialized before being ticked. Please use Particle.init() to initialize it.") }

    val properties = blockDisplay?.properties ?: return

    val newProperties = calculateNewProperties(properties, particleParams)

    blockDisplay?.updateProperties(world, newProperties)

    if (timeAlive > particleParams.lifeTime) { isDead = true; blockDisplay?.kill() }

    timeAlive += 1
  }


  private fun calculateNewProperties(propetries: DisplayEntityProperties, params: ParticleParams) : DisplayEntityProperties {
    val newBlockIdx = round(lerp(
      0.0f,
      params.blockCurve.lastIndex.toFloat(),
      (timeAlive.toFloat() / params.lifeTime.toFloat()).coerceIn(0.0f, 1.0f)
    )).toInt()

    val newBlock = params.blockCurve[newBlockIdx]


    val newProperties = DisplayEntityProperties(
      blockType = newBlock
    )

    return newProperties
  }
}