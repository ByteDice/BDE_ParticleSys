package com.bytedice.bde_particles.particles

import com.bytedice.bde_particles.Bde_particles
import com.bytedice.bde_particles.LIVING_PARTICLE_COUNT
import com.bytedice.bde_particles.randomFloatBetween
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import org.joml.Vector2f

class ParticleEmitter(
  private val pos: Vec3d,
  private val rot: Vector2f,
  private val world: ServerWorld,
  private val params: EmitterParams,
  private val debug: Boolean
) {
  private var allParticles: Array<Particle> = emptyArray()

  private var timeAlive = 0
  private var timePaused = 0
  private var timesLooped = 0
  private var isPaused = false

  private var isTicking = true

  var isDead = false


  suspend fun tick() {
    if (isTicking) { count() }
    if (isTicking) { addParticle() }
    if (!isTicking && allParticles.isEmpty()) { isDead = true }

    processParticlesInBatches(world, 100)
  }


  private suspend fun processParticlesInBatches(
    world: ServerWorld,
    batchSize: Int
  ) = coroutineScope {
    val batches = allParticles.toMutableList().chunked(batchSize)

    val jobs = batches.map { batch ->
      async {
        val toRemove = mutableListOf<Particle>()
        batch.forEach { particle ->
          if (particle.isDead) {
            toRemove.add(particle)
          } else {
            particle.tick(world)
          }
        }
        synchronized(allParticles) {
          allParticles = allParticles.toMutableList().apply { removeAll(toRemove) }.toTypedArray()
        }
      }
    }

    jobs.awaitAll()
  }


  private fun addParticle() {
    if (isPaused) { return }

    repeat(params.spawnRate) {
      if (allParticles.size >= params.maxCount) { return }

      val maxParticles = world.gameRules.getInt(Bde_particles.GLOBAL_MAX_PARTICLES)
      if (LIVING_PARTICLE_COUNT >= maxParticles) { return }

      val randomSpawnChance = randomFloatBetween(0.0f, params.spawnChance.coerceIn(0.0f, 1.0f))
      if (randomSpawnChance > params.spawnChance) { return }

      val particle = Particle(params, pos, rot, debug)
      particle.init()
      particle.spawn(world)

      allParticles += particle
    }
  }


  private fun count() {
    if (params.spawnDuration is ParamClasses.Duration.SingleBurst) {
      val loopDur = (params.spawnDuration as ParamClasses.Duration.SingleBurst).loopDur
      if (timeAlive >= loopDur) { isTicking = false; return }

      timeAlive += 1
    }

    else if (params.spawnDuration is ParamClasses.Duration.MultiBurst) {
      val loopDur = (params.spawnDuration as ParamClasses.Duration.MultiBurst).loopDur
      val loopDelay = (params.spawnDuration as ParamClasses.Duration.MultiBurst).loopDelay
      val loopCount = (params.spawnDuration as ParamClasses.Duration.MultiBurst).loopCount

      if (timeAlive >= loopDur && timesLooped + 1 >= loopCount) { isTicking = false; return }

      if (timePaused >= loopDelay) {
        isPaused = false
        timePaused = 0
        timesLooped += 1
      }
      if (timeAlive >= loopDur && loopDelay > 0) {
        isPaused = true
        timeAlive = 0
      }

      if (!isPaused) { timeAlive += 1 }
      else { timePaused += 1 }
    }

    else if (params.spawnDuration is ParamClasses.Duration.InfiniteLoop) {
      val loopDur = (params.spawnDuration as ParamClasses.Duration.InfiniteLoop).loopDur
      val loopDelay = (params.spawnDuration as ParamClasses.Duration.InfiniteLoop).loopDelay

      if (timePaused >= loopDelay) {
        isPaused = false
        timePaused = 0
      }
      if (timeAlive >= loopDur && loopDelay > 0) {
        isPaused = true
        timeAlive = 0
      }

      if (!isPaused) { timeAlive += 1 }
      else { timePaused += 1 }
    }
  }


  fun kill() {
    for (particle in allParticles) {
      particle.kill()
      allParticles = allParticles.toMutableList().apply { remove(particle) }.toTypedArray()
    }
    isTicking = false
    isDead = true
  }


  fun stopTicking() {
    isTicking = false
  }
}