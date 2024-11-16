package com.bytedice.bde_particles.particle

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class ParticleEmitter(emitterPos: Vec3d, emitterWorld: ServerWorld, emitterParams: ParticleEmitterParams) {
  private val world  = emitterWorld
  private val pos    = emitterPos
  private val params = emitterParams

  private var allParticles: Array<Particle> = emptyArray()

  private var timeAlive = 0
  private var loopCount = 0
  private var loopDelay = 0
  private var loopDur   = 0
  private var loopDelayActive = false

  var isDead = false


  fun tick() {
    count()

    if (!loopDelayActive) {
      repeat(this.params.spawnsPerTick) {
        val particle = Particle(ParticleParams())
        particle.init(this.pos)
        particle.spawn(world)
        allParticles += particle
      }
    }

    for (particle in allParticles) {
      if (particle.isDead) { allParticles = allParticles.toMutableList().apply { remove(particle) }.toTypedArray() }
      else { particle.tick(world) }
    }
  }


  private fun count() {
    if (loopDur >= params.loopDur) {
      loopCount += 1
      loopDur = 0
      loopDelayActive = true
      if (!params.loop) { isDead = true }
    }
    else if (loopDelay >= params.loopDelay) {
      loopDelay = 0
      loopDelayActive = false
    }

    if (!loopDelayActive) { loopDur += 1 }
    else { loopDelay += 1 }
    timeAlive += 1
  }
}