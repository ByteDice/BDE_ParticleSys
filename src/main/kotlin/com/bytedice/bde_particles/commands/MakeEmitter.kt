package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.particle.particleIdRegister
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import java.util.concurrent.CompletableFuture


// ManageEmitters
// <create / config>
// create
  // [emitter id]
  // [preset emitter id] or DEFAULT
  // output -> register new emitter with a preset
// config
  // [emitter id]
  // [particle index]
  // [param key]
  // [new param value]
  // output -> update the selected parameter of the selected particle id to the new value

object ManageEmitters {
  fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
    val command = CommandManager.literal("ManageEmitters")
      .requires { source -> source.hasPermissionLevel(4) }

    val allParticleIds = particleIdRegister.keys

    val particleIdArg = CommandManager.argument("Particle ID", StringArgumentType.string())
      .suggests { _, builder ->
        val suggestionsBuilder = SuggestionsBuilder(builder.remaining, 0)
        allParticleIds.forEach { key ->
          suggestionsBuilder.suggest(key)
        }
        CompletableFuture.completedFuture(suggestionsBuilder.build())
      }

    dispatcher.register(
      command.then(
        particleIdArg.executes { context ->
          val particleId = StringArgumentType.getString(context, "Particle ID")

          val feedback = Text.literal("you chose Particle ID \"$particleId\"")

          context.source.sendFeedback({ feedback }, false)
          Command.SINGLE_SUCCESS
        }
      )
    )
  }
}