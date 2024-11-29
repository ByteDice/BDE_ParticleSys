package com.bytedice.bde_particles.particles

import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import org.joml.Vector2f
import org.joml.Vector3f

class ParticleEmitter(private val pos: Vec3d, private val rot: Vector2f, private val world: ServerWorld, private val params: EmitterParams) {

  private var allParticles: Array<Particle> = emptyArray()

  private var timeAlive = 0
  private var loopCount = 0
  private var loopDelay = 0
  private var loopDur   = 0
  private var loopDelayActive = false

  private var isTicking = true

  var isDead = false


  fun tick() {
    if (isTicking) { count() }
    if (isTicking) { addParticle() }
    if (!isTicking && allParticles.isEmpty()) { isDead = true }

    for (particle in allParticles) {
      if (particle.isDead) { allParticles = allParticles.toMutableList().apply { remove(particle) }.toTypedArray() }
      else { particle.tick() }
    }
  }


  private fun addParticle() {
    if (loopDelayActive) { return }

    repeat(params.spawnRate) {
      if (allParticles.size >= params.maxCount) { return }

      val particle = Particle(params)
      particle.init(pos, rot)
      particle.spawn(world)

      allParticles += particle
    }
  }


  private fun count() {
    // this is the only section I have comments
    // because im so damn good at cooking spaghetti

    // if max duration is 0 or less, skip counting, its infinite
    if (params.loopDur <= 0) { return }

    // if the duration is greater than max duration, add 1 loopCount
    // return if max loopCount is 0
    // if the delay is greater than 0 then enable it
    if (loopDur >= params.loopDur) {
      if (loopCount == 0) {
        isTicking = false
        return
      }
      loopCount += 1
      loopDur = 0
      if (params.loopDelay > 0) { loopDelayActive = true }
    }

    // if the delay is greater than max delay, disable the delay
    else if (loopDelay >= params.loopDelay) {
      loopDelay = 0
      loopDelayActive = false
    }

    // if loopCount is greater than max loopCount then stop immediately
    // if max loopCount is below 0 then don't do anything, loop infinitely
    if (loopCount > 0 && loopCount >= params.loopCount) {
      isTicking = false
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
    isTicking = false
    isDead = true
  }
}