package com.bytedice.bde_particles.client

import com.bytedice.bde_particles.commands.GiveEmitterTool
import com.bytedice.bde_particles.commands.KillAllEmitters
import com.bytedice.bde_particles.commands.ManageEmitters
import com.bytedice.bde_particles.commands.SpawnEmitter
//import com.bytedice.bde_particles.commands.ManageEmitters
import com.bytedice.bde_particles.init
import com.bytedice.bde_particles.onRightClick
import com.bytedice.bde_particles.tick
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class Bde_particlesClient : ClientModInitializer {
  override fun onInitializeClient() {
    if (FabricLoader.getInstance().environmentType != EnvType.CLIENT) {
      return
    }

    println("BPS - Initializing on ${FabricLoader.getInstance().environmentType}")

    ServerLifecycleEvents.SERVER_STARTED.register { _ ->
      init()
    }

    ServerTickEvents.START_SERVER_TICK.register { server ->
      tick(server)
    }

    CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, _ ->
      GiveEmitterTool.register(dispatcher, registryAccess)
      ManageEmitters .register(dispatcher)
      KillAllEmitters.register(dispatcher)
      SpawnEmitter   .register(dispatcher)
    }

    UseItemCallback.EVENT.register(UseItemCallback { player: PlayerEntity, world: World, hand: Hand ->
      if (world is ServerWorld) {
        onRightClick(player, world, hand)
      }
      TypedActionResult(ActionResult.PASS, player.getStackInHand(hand))
    })

    ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { _ ->
      println("BPS - Progress bar filled :3")
      println(
        "\n____________  _____              _____ \n" +
        "| ___ \\ ___ \\/  ___|         _  |____ |\n" +
        "| |_/ / |_/ /\\ `--.         (_)     / /\n" +
        "| ___ \\  __/  `--. \\                \\ \\\n" +
        "| |_/ / |    /\\__/ /         _  .___/ /\n" +
        "\\____/\\_|    \\____/         (_) \\____/ \n"
      )
    })
  }
}