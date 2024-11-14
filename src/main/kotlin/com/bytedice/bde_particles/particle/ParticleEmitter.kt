package com.bytedice.bde_particles.particle

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class ParticleEmitter(emitterPos: Vec3d, emitterWorld: ServerWorld, emitterParams: ParticleEmitterParams) {
  val world  = emitterWorld
  val pos    = emitterPos
  val params = emitterParams
  val allParticles: Array<Particle> = emptyArray()
  val timeAlive = 0


  fun tick() {
    repeat(this.params.spawnsPerTick) {
      val particle = Particle(ParticleParams())
      particle.init(this.pos)
      particle.spawn(world)
    }
  }
}