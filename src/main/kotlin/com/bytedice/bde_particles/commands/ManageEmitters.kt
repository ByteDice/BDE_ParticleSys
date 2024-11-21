package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.particle.addToEmitterRegister
import com.bytedice.bde_particles.particle.emitterIdRegister
import com.bytedice.bde_particles.particle.getParticleEmitterParams
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import java.awt.Color
import java.util.concurrent.CompletableFuture


// ManageEmitters
// <create / remove / config / list>
  // create
    // [emitter id]
    // [preset emitter id]
    // output -> register new emitter with a preset

  // remove
    // [emitter id]
    // output -> removes the particle with that id

  // config
    // [emitter id]
    // <[particle index] / EMITTER>

    // <single param / JSON>
      // single param (if EMITTER, use emitter params instead)
        // [param key]
        // [new param value]
        // output -> update the selected parameter of the selected emitter id to the new value

      // JSON
        // [new params] (unspecified values is treated as current)
        // output -> parse the JSON and update all params

  // list
    // output -> all registered emitters

  // copy
    // output -> give player a command block with the particle params (should be paste-able in kotlin)


object ManageEmitters {
  fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
    val command = CommandManager.literal("ManageEmitters")
      .requires { source -> source.hasPermissionLevel(4) }

    dispatcher.register(
      command
        // <create / remove / config / list / copy>
        .then(argCreate())
        .then(argRemove())
        .then(argConfig())
        .then(argList())
        .then(argCopy())
      )
  }
}


val allEmitterIds = emitterIdRegister.keys

val emitterIdSuggestion = CommandManager.argument("Registered Emitter ID", StringArgumentType.string())
  .suggests { _, builder ->
    val suggestionsBuilder = SuggestionsBuilder(builder.remaining, 0)
    allEmitterIds.forEach { key ->
      suggestionsBuilder.suggest(key)
    }
    CompletableFuture.completedFuture(suggestionsBuilder.build())
  }


fun argCreate() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("create")
    .then(
      // [emitter id]
      CommandManager.argument("Emitter ID", StringArgumentType.string())
        .then(
        // [preset emitter id]
        emitterIdSuggestion
            .executes { context ->
              val emitterIdVal = StringArgumentType.getString(context, "Emitter ID")
              val emitterPresetVal = StringArgumentType.getString(context, "Registered Emitter ID") ?: "DEFAULT"
              val emitterPresetParams = getParticleEmitterParams(emitterPresetVal)

              var newEmitterId = "NULL_EMITTER_ID"
              if (emitterPresetParams != null) {
                newEmitterId = addToEmitterRegister(emitterIdVal, emitterPresetParams).second
              }

              val feedback = if (newEmitterId in allEmitterIds) {
              Text.literal("BPS - Emitter ID \"$newEmitterId\" already exists!\n" +
                      "BPS - Use \"/ManageEmitters list\" to view all Emitter ID's.")
                .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(255, 0, 0).rgb)))
              }
              else {
                Text.literal("BPS - created particle emitter with ID \"$newEmitterId\".\n" +
                        "BPS - This emitter will be removed on server restart! Use \"/ManageEmitters copy\" to save the data to your clipboard!")
                  .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(0, 200, 0).rgb)))
              }

              context.source.sendFeedback( { feedback }, false )
              Command.SINGLE_SUCCESS
            }
        )
    )
}


fun argRemove() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("remove")
    .executes { context ->
      val feedback = Text.literal("remove")
      context.source.sendFeedback({ feedback }, false)
      Command.SINGLE_SUCCESS
    }
}


fun argConfig() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("config")
    .executes { context ->
      val feedback = Text.literal("remove")
      context.source.sendFeedback({ feedback }, false)
      Command.SINGLE_SUCCESS
    }
}


fun argList() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("list")
    .executes { context ->
      val feedback = Text.literal("remove")
      context.source.sendFeedback({ feedback }, false)
      Command.SINGLE_SUCCESS
    }
}


fun argCopy() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("copy")
    .executes { context ->
      val feedback = Text.literal("remove")
      context.source.sendFeedback({ feedback }, false)
      Command.SINGLE_SUCCESS
    }
}


/*
fun argCopyPasta() : LiteralArgumentBuilder<ServerCommandSource> {
  return
}
*/
