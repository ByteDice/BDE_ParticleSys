package com.bytedice.bde_particles

import com.bytedice.bde_particles.commands.GiveEmitterTool
import com.bytedice.bde_particles.commands.ManageEmitters
import com.bytedice.bde_particles.items.ParticleEmitterTool
import com.bytedice.bde_particles.particleIdRegister.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World


// TODO: finish the particle ticking
// TODO: save particle emitters in world file, or kill them on server restart (so they don't linger forever)
// TODO: make custom commands to create particles easier (only temporarily saved)
// TODO: add interpolation curves (easing)
// TODO: kill all particles command


var ALL_PARTICLE_EMITTERS: Array<ParticleEmitter> = emptyArray()


class Bde_particles : ModInitializer {

  override fun onInitialize() {
    ServerLifecycleEvents.SERVER_STARTED.register { _ ->
      init()
    }

    ServerTickEvents.START_SERVER_TICK.register { _ ->
      tick()
    }

    CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, _ ->
      GiveEmitterTool.register(dispatcher, registryAccess)
      ManageEmitters     .register(dispatcher)
    }

    UseItemCallback.EVENT.register(UseItemCallback { player: PlayerEntity, world: World, hand: Hand ->
      onRightClick(player, world as ServerWorld, hand)
    })

    ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { _ ->
      println("BDE_ParticleSys - Progress bar filled :3")
    })
  }
}


fun init() {
  addToEmitterRegister("DEFAULT", EmitterParams.DEFAULT)
  addToEmitterRegister("FIRE_GEYSER", EmitterParams.FIRE_GEYSER)
}


fun tick() {
  for (emitter in ALL_PARTICLE_EMITTERS) {
    if (emitter.isDead) {
      ALL_PARTICLE_EMITTERS = ALL_PARTICLE_EMITTERS.toMutableList().apply { remove(emitter) }.toTypedArray()
    }
    else { emitter.tick() }
  }
}


fun onRightClick(player: PlayerEntity, world: ServerWorld, hand: Hand) : TypedActionResult<ItemStack> {
  val handItem = player.getStackInHand(hand)
  val (isEmitterTool, emitterId) = ParticleEmitterTool.getToolDetails(handItem)
  val hitResult = raycastFromPlayer(player as ServerPlayerEntity, 50.0)
  val emitterParams = getEmitterParams(emitterId)

  if (
    !isEmitterTool
    || emitterParams == null
    || hitResult == null
  ) {
    return TypedActionResult(ActionResult.PASS, handItem)
  }

  val emitter = ParticleEmitter(hitResult.pos, world, emitterParams)
  ALL_PARTICLE_EMITTERS += emitter

  return TypedActionResult(ActionResult.PASS, handItem)
}


fun emitterParamsToJson(params: EmitterParams) : Map<String, Any> {
  val allParticleParamsJSON: MutableList<Map<String, Any?>> = mutableListOf()

  for (particle in params.particleTypes) {
    val particleParamsJSON = mapOf(
      "shape"        to particle.shape,
      "blockCurve"   to particle.blockCurve,
      "rotRandom"    to particle.rotRandom,
      "rotVelRandom" to particle.rotVelRandom,
      "rotVelCurve"  to particle.rotVelCurve,
      "sizeRandom"   to particle.sizeRandom,
      "uniformSize"  to particle.uniformSize,
      "sizeCurve"    to particle.sizeCurve,
      "velRandom"    to particle.velRandom,
      "forceFields"  to particle.forceFields,
      "gravity"      to particle.gravity,
      "drag"         to particle.drag,
      "minVel"       to particle.minVel,
      "lifeTime"     to particle.lifeTime
    )

    allParticleParamsJSON.add(particleParamsJSON)
  }

  val emitterParamsJSON = mapOf(
    "maxCount"      to params.maxCount,
    "spawnsPerTick" to params.spawnsPerTick,
    "loopDur"       to params.loopDur,
    "loopDelay"     to params.loopDelay,
    "loopCount"     to params.loopCount,
    "particleTypes" to allParticleParamsJSON
  )

  return emitterParamsJSON
}