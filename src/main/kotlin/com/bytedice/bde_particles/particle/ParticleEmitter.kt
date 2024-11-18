package com.bytedice.bde_particles.particle

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d

class ParticleEmitter(private val emitterPos: Vec3d, private val emitterWorld: ServerWorld, private val emitterParams: ParticleEmitterParams) {

  private var allParticles: Array<Particle> = emptyArray()

  private var timeAlive = 0
  private var loopCount = 0
  private var loopDelay = 0
  private var loopDur   = 0
  private var loopDelayActive = false

  private var isCounting = true

  var isDead = false


  fun tick() {
    if (isCounting) { count() }
    if (!isCounting && allParticles.isEmpty()) { isDead = true }

    addParticle()

    for (particle in allParticles) {
      if (particle.isDead) { allParticles = allParticles.toMutableList().apply { remove(particle) }.toTypedArray() }
      else { particle.tick() }
    }
  }


  private fun addParticle() {
    if (loopDelayActive) { return }

    for (i in emitterParams.particleTypes.indices) {
      repeat(this.emitterParams.spawnsPerTick) {
        if (allParticles.size < emitterParams.maxCount) { return }

        val particle = Particle(emitterParams.particleTypes[i])
        particle.init(this.emitterPos)
        particle.spawn(emitterWorld)

        allParticles += particle
      }
    }
  }


  private fun count() {
    // TODO: this is broken or something, no particles are spawning
    // this is the only section I have comments
    // because im so damn good at cooking spaghetti

    // if max duration is 0 or less, skip counting, its infinite
    if (emitterParams.loopDur <= 0) { return }

    // if the duration is greater than max duration, add 1 loopCount
    // if the delay is greater than 0 then enable it
    if (loopDur > emitterParams.loopDur) {
      loopCount += 1
      loopDur = 0
      if (emitterParams.loopDelay > 0) { loopDelayActive = true }
    }

    // if the delay is greater than max delay, disable the delay
    else if (loopDelay > emitterParams.loopDelay) {
      loopDelay = 0
      loopDelayActive = false
    }

    // if loopCount is greater than max loopCount then stop immediately
    // only loops once if max loopCount is 0
    // if max loopCount is below 0 then don't do anything, loop infinitely
    if (emitterParams.loopCount in 0..<loopCount) {
      isCounting = false
      return
    }

    // if delay isn't active, add 1 to duration
    // if delay is active, add 1 to delay
    if (!loopDelayActive) { loopDur += 1 }
    else { loopDelay += 1 }

    timeAlive += 1
  }


  fun kill() {
    for (particle in allParticles) {
      particle.kill()
      allParticles = allParticles.toMutableList().apply { remove(particle) }.toTypedArray()
    }
    isCounting = false
    isDead = true
  }
}