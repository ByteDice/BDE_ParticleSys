package com.bytedice.bde_particles.particle

import com.bytedice.bde_particles.DisplayEntity
import com.bytedice.bde_particles.DisplayEntityProperties
import com.bytedice.bde_particles.math.*
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import org.joml.Vector3f


class Particle(particleParams: ParticleParams) {
  val rot = Vector3f(
    randomFloatBetween(particleParams.rotRandom.first.x, particleParams.rotRandom.second.x),
    randomFloatBetween(particleParams.rotRandom.first.y, particleParams.rotRandom.second.x),
    randomFloatBetween(particleParams.rotRandom.first.z, particleParams.rotRandom.second.z)
  )
  val rotVel = Vector3f(
    randomFloatBetween(particleParams.rotVelRandom.first.x, particleParams.rotVelRandom.second.x),
    randomFloatBetween(particleParams.rotVelRandom.first.y, particleParams.rotVelRandom.second.x),
    randomFloatBetween(particleParams.rotVelRandom.first.z, particleParams.rotVelRandom.second.z)
  )
  val scale = Vector3f(
    randomFloatBetween(particleParams.sizeRandom.first.x, particleParams.sizeRandom.second.x),
    randomFloatBetween(particleParams.sizeRandom.first.y, particleParams.sizeRandom.second.x),
    randomFloatBetween(particleParams.sizeRandom.first.z, particleParams.sizeRandom.second.z)
  )
  var blockDisplay: DisplayEntity? = null
  val params = particleParams

  var isInit = false

  fun init(pos: Vec3d) {
    val offset = Vector3f(-0.5f, -0.5f, -0.5f)
    val quatRot = eulerToQuat(this.rot)

    val scaleOffset = transformOffsetByScale(offset, this.scale)
    val rotOffset = transformOffsetByQuat(scaleOffset, quatRot)

    val properties = DisplayEntityProperties(
      pos          = pos,
      blockType    = this.params.blockCurve[0],
      translation  = rotOffset,
      leftRotation = quatRot,
      scale        = this.scale
    )
    
    this.blockDisplay = DisplayEntity(properties)

    this.isInit = true
  }


  fun spawn(world: ServerWorld) {
    if (isInit) { blockDisplay?.spawn(world) }
    else { error("Particle is not initialized before being spawned. Please use Particle.init() to initialize it") }
  }


  fun tick() {

  }
}