package com.bytedice.bde_particles.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.command.SummonCommand


object SpawnEmitter {
  fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
    val command = CommandManager.literal("SpawnEmitter")
      .requires { source -> source.hasPermissionLevel(2) }

    dispatcher.register(
    )
  }
}