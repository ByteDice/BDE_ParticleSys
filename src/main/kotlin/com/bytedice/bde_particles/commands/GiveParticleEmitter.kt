package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.items.ParticleEmitter
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object GiveParticleEmitter {
  fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
    dispatcher.register(
      CommandManager.literal("giveParticleEmitter")
        .requires {  source -> source.hasPermissionLevel(2) }
        .then(CommandManager.argument("item name", StringArgumentType.string()))
        .then(CommandManager.argument("item type", ItemStackArgumentType.itemStack()))) // error here
        .then(CommandManager.argument("particle id", StringArgumentType.string()))

        .executes { context ->
          context.source.player?.inventory?.insertStack(ParticleEmitter().makeData()) // error here
          context.source.sendFeedback({ Text.literal("Gave you 500Kg bomb. Don\'t get too silly with it :3") }, false)
          Command.SINGLE_SUCCESS
        }
    )
  }
}