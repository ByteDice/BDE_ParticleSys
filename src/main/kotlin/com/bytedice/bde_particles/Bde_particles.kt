package com.bytedice.bde_particles

import com.bytedice.bde_particles.commands.GiveParticleEmitter
import com.bytedice.bde_particles.items.getToolDetails
import com.bytedice.bde_particles.particle.Particle
import com.bytedice.bde_particles.particle.returnParticleFunction
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult


// const val ALL_PARTICLE_EMITTERS: Array<> // ParticleEmitter

data class Config(
  val cooldown: Int = 1
)


val cfg = Config()


class Bde_particles : ModInitializer {

  override fun onInitialize() {
    ServerTickEvents.START_SERVER_TICK.register { _ ->
      tick()
    }

    CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, _ ->
      GiveParticleEmitter.register(dispatcher, registryAccess)
    }

    UseItemCallback.EVENT.register(UseItemCallback { player: PlayerEntity, _, hand: Hand ->
      onRightClick(player, hand)
    })

    ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server: MinecraftServer ->

      server.playerManager.broadcast(Text.of("BDAT - Loaded"), false)
      print("BDAT - Loaded")
    })
  }
}


fun tick() {

}


fun onRightClick(player: PlayerEntity, hand: Hand) : TypedActionResult<ItemStack> {
  val handItem = player.getStackInHand(hand)
  val (isEmitterTool, particleId) = getToolDetails(handItem)

  if (isEmitterTool) {
    val particleFunction = returnParticleFunction(particleId)
    if (particleFunction != null) { /* spawn particle with params */ }
  }

  return TypedActionResult(ActionResult.PASS, handItem)
}