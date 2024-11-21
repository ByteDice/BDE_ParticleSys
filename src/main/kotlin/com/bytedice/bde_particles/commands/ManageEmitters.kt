package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.emitterParamsToJson
import com.bytedice.bde_particles.jsonToEmitterParams
import com.bytedice.bde_particles.particle.*
import com.bytedice.bde_particles.stringToMap
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
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
      // single param (if EMITTER, use emitter params instead) // TODO
        // [param key]
        // [new param value]
        // output -> update the selected parameter of the selected emitter id to the new value

      // JSON
        // [new params] (unspecified values is treated as current)
        // output -> parse the JSON and update all params

  // list
    // output -> all registered emitters

  // copy
    // [emitter id]
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

val emitterIdSuggestion: RequiredArgumentBuilder<ServerCommandSource, String> = CommandManager.argument("Registered Emitter ID", StringArgumentType.string())
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
              val emitterPresetParams = getEmitterParams(emitterPresetVal)

              var newEmitterId = "NULL"
              val emitterRegisterData: Pair<String, Boolean>

              if (emitterPresetParams != null) {
                emitterRegisterData = addToEmitterRegister(emitterIdVal, emitterPresetParams)
                newEmitterId = emitterRegisterData.first
              }
              else { emitterRegisterData = Pair("NULL", false) }

              val feedback = if (emitterRegisterData.second) {
                Text.literal("BPS - Created particle emitter with ID \"$newEmitterId\".\n" +
                        "BPS - This emitter will be removed on server restart! Use \"/ManageEmitters copy\" to save the data to your clipboard!")
                  .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(0, 200, 0).rgb)))
              }
              else {
                Text.literal("BPS - Emitter ID \"$newEmitterId\" already exists!\n" +
                        "BPS - Use \"/ManageEmitters list\" to view all Emitter ID's.")
                  .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(200, 0, 0).rgb)))
              }

              context.source.sendFeedback( { feedback }, false )
              Command.SINGLE_SUCCESS
            }
        )
    )
}


fun argRemove() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("remove")
    .then(
      // [emitter id]
      emitterIdSuggestion
        .executes { context ->
          val emitterIdVal = StringArgumentType.getString(context, "Registered Emitter ID") ?: "NULL"
          val isRemoved = removeFromEmitterRegister(emitterIdVal)

          val feedback = if (isRemoved) {
            Text.literal("BPS - Removed particle emitter with ID \"$emitterIdVal\".")
              .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(0, 200, 0).rgb)))
          }
          else {
            Text.literal("BPS - Emitter ID \"$emitterIdVal\" doesn't exist or is reserved!\n" +
                    "BPS - Use \"/ManageEmitters list\" to view all Emitter ID's.")
              .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(200, 0, 0).rgb)))
          }

          context.source.sendFeedback( { feedback }, false )
          Command.SINGLE_SUCCESS
        }
    )
}


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


fun argConfig() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("config")
    .then(
      // [emitter id]
      emitterIdSuggestion
        .then(
          // <[particle index] / EMITTER>
          makeIndexSuggestion()
            // <single param / JSON>
            //.then(argSingleParam())
            .then(argJson())
        )
    )
}

/*
fun argSingleParam() {
  CommandManager.literal("SingleParam")
    .then(
      // [param key]
    )
}
*/

fun argJson() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("JSON")
    .then(
      // [new params]
      CommandManager.argument("JSON Value", StringArgumentType.string())
        .executes { context ->
          val jsonString = StringArgumentType.getString(context, "JSON Value")
          val jsonMap = stringToMap(jsonString)
          val jsonParams = jsonToEmitterParams(jsonMap)

          val emitterIdVal = StringArgumentType.getString(context, "Registered Emitter ID")

          updateEmitterParams(emitterIdVal, jsonParams)

          val feedback = Text.literal("BPS - Updated parameters of Emitter ID \"$emitterIdVal\"!\n")
            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(200, 0, 0).rgb)))

          Command.SINGLE_SUCCESS
        }
    )
}


fun argList() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("list")
    .executes { context ->
      val allEmittersString = allEmitterIds.joinToString(", ")

      val feedbackStatic = Text.literal("BPS - List of all available Emitter IDs:\n")
        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(0, 200, 0).rgb)))

      val feedbackVar = Text.literal(allEmittersString)
        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(255, 255, 255).rgb)))

      val feedback = feedbackStatic.append(feedbackVar)

      context.source.sendFeedback({ feedback }, false)
      Command.SINGLE_SUCCESS
    }
}


fun argCopy() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("copy")
    .then(
      // [emitter id]
      emitterIdSuggestion
        .executes { context ->
          val emitterIdVal = StringArgumentType.getString(context, "Registered Emitter ID")
          val commandBlock = makeCommandBlock(
            emitterParamsToJson(
              getEmitterParams(
                emitterIdVal
              )!!
            )
          )

          val feedback: MutableText

          if (context.source.server.areCommandBlocksEnabled()) {
            feedback = Text.literal("BPS - Copied Emitter data as a JSON. Place down the command block and copy it to your clipboard to use the value.")
              .setStyle(Style.EMPTY.withColor(Color(0, 200, 0).rgb))
          }
          else {
            feedback = Text.literal("BPS - Could not copy Emitter data: Command blocks aren't enabled. Go to server.properties and enable command blocks before retrying.")
              .setStyle(Style.EMPTY.withColor(Color(200, 0, 0).rgb))

            Command.SINGLE_SUCCESS
          }


          context.source.player?.inventory?.insertStack(commandBlock)
          context.source.sendFeedback({ feedback }, false)
          Command.SINGLE_SUCCESS
        }
    )
}


fun makeCommandBlock(emitterData: Map<String, Any>): ItemStack {
  val i = ItemStack(Items.COMMAND_BLOCK)

  val nbt = NbtCompound()
  nbt.putString("Command", emitterData.toString())
  nbt.putString("id", "minecraft:command_block")
  nbt.putByte("powered", 0)
  nbt.putByte("auto", 0)
  nbt.putByte("UpdateLastExecution", 1)
  nbt.putByte("conditionMet",  0)
  nbt.putByte("TrackOutput", 1)
  nbt.putInt("SuccessCount", 0)


  val component: NbtComponent = NbtComponent.of(nbt)

  i.set(DataComponentTypes.BLOCK_ENTITY_DATA, component)

  return i
}


fun makeIndexSuggestion() : RequiredArgumentBuilder<ServerCommandSource, String> {
  return CommandManager.argument("Particle Index", StringArgumentType.string())
    .suggests { context, builder ->
      val emitterParams = getEmitterParams(StringArgumentType.getString(context, "Registered Emitter ID"))!!
      val suggestionsBuilder = SuggestionsBuilder(builder.remaining, 0)

      emitterParams.particleTypes.indices.forEach { i ->
        suggestionsBuilder.suggest(i.toString())
      }

      suggestionsBuilder.suggest("EMITTER")

      CompletableFuture.completedFuture(suggestionsBuilder.build())
    }
}


/*
fun paramSuggestion(particleIndex: Int, emitterId: String) : RequiredArgumentBuilder<ServerCommandSource, String> {
  val emitterParams = getParticleEmitterParams(emitterId)

  val particleParams = if (emitterParams?.particleTypes!!.isNotEmpty()) {
    emitterParams.particleTypes[particleIndex]
  }
  else { null }

  val emitterProperties = emitterParams::class.memberProperties
  val emitterPropertyNames: MutableList<String> = mutableListOf()
  val emitterPropertyTypes: MutableList<KType> = mutableListOf()

  var particleProperties: Collection<KProperty1<out ParticleParams, *>>? = null

  if (particleParams != null) {
    particleProperties = particleParams::class.memberProperties
  }

  if (particleIndex != -1) {
    for (property in emitterProperties) {
      emitterPropertyNames.add(property.name)
      emitterPropertyTypes.add(property.returnType)
    }
  }
  else if (particleProperties != null) {
    for (property in particleProperties) {}
  }

  //
  return CommandManager.argument("Param Key", StringArgumentType.string())
    .suggests { context, builder ->
      val suggestionsBuilder = SuggestionsBuilder(builder.remaining, 0)

      CompletableFuture.completedFuture(suggestionsBuilder.build())
    }
}
*/