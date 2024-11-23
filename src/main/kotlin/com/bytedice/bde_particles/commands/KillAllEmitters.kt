package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.ALL_PARTICLE_EMITTERS
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object KillAllEmitters {
  fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
    val command = CommandManager.literal("KillAllEmitters")
      .requires { source -> source.hasPermissionLevel(4) }

    dispatcher.register(
      command
        .executes { context ->
          for (emitter in ALL_PARTICLE_EMITTERS) {
            emitter.kill()
          }

          val feedback = Text.literal("BPS - Killed all living Emitters.")

          context.source.sendFeedback({ feedback }, false)
          Command.SINGLE_SUCCESS
        }
    )
  }
}