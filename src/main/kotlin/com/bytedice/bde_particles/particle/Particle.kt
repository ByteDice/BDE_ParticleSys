package com.bytedice.bde_particles.particle

import com.bytedice.bde_particles.DisplayEntity
import com.bytedice.bde_particles.DisplayEntityProperties
import com.bytedice.bde_particles.math.eulerToQuat
import com.bytedice.bde_particles.math.randomFloatBetween
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
  val size = Vector3f(
    randomFloatBetween(particleParams.sizeRandom.first.x, particleParams.sizeRandom.second.x),
    randomFloatBetween(particleParams.sizeRandom.first.y, particleParams.sizeRandom.second.x),
    randomFloatBetween(particleParams.sizeRandom.first.z, particleParams.sizeRandom.second.z)
  )
  var blockDisplay: DisplayEntity? = null
  val params = particleParams

  fun init(pos: Vec3d) {
    val properties = DisplayEntityProperties(
      pos          = pos,
      blockType    = this.params.blockCurve[0],
      leftRotation = eulerToQuat(rot),
      scale        = size
    )
    
    blockDisplay = DisplayEntity(properties)
  }


  fun spawn(world: ServerWorld) {
    blockDisplay?.spawn(world)
  }


  fun tick() {

  }
}