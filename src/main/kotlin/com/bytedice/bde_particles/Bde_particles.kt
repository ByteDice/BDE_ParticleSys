package com.bytedice.bde_particles

import com.bytedice.bde_particles.commands.GiveParticleEmitter
import com.bytedice.bde_particles.items.getToolDetails
import com.bytedice.bde_particles.math.raycastFromPlayer
import com.bytedice.bde_particles.particle.*
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


var ALL_PARTICLE_EMITTERS: Array<ParticleEmitter> = emptyArray()

data class Config(
  val cooldown: Int = 1
)


val cfg = Config()


class Bde_particles : ModInitializer {

  override fun onInitialize() {
    ServerLifecycleEvents.SERVER_STARTED.register { _ ->
      init()
    }

    ServerTickEvents.START_SERVER_TICK.register { _ ->
      tick()
    }

    CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, _ ->
      GiveParticleEmitter.register(dispatcher, registryAccess)
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
  addToParticleRegister("DEBUG", ParticleEmitterParams())
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
  val (isEmitterTool, particleId) = getToolDetails(handItem)
  val hitResult = raycastFromPlayer(player as ServerPlayerEntity, 50.0)
  val emitterParams = getParticleEmitterParams(particleId)

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