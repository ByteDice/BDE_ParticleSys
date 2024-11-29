package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.emitterParamsToJson
import com.bytedice.bde_particles.particles.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
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


// TODO: completely redesign


/*
// spawning
maxCount:       Int,
spawnRate:      Int,
spawnChance:    Float,
spawnDuration:  ParamClasses.Duration,
spawnPosOffset: Vector3f,
lifeTime:       ParamClasses.PairInt,
shape:          SpawningShape,
// init transforms
offset:         ParamClasses.PairVec3f,
initRot:        ParamClasses.PairVec3f,
rotVel:         ParamClasses.PairVec3f,
rotWithVel:     Boolean,
initVel:        ParamClasses.PairVec3f,
initCenterVel:  ParamClasses.PairFloat,
initScale:      ParamClasses.PairVec3f,
scaleWithVel:   Boolean,
// velocity
forceFields:    Array<ForceField>,
constVel:       Vector3f,
drag:           Float,
minVel:         Float,
// curves
offsetCurve:    ParamClasses.LerpVal,
rotVelCurve:    ParamClasses.LerpVal,
scaleCurve:     ParamClasses.LerpVal,
blockCurve:     Pair<Array<String>, LerpCurves>,
*/


// ManageEmitters
// <create / remove / config / list>
  // create
    // <emitter id>
    // <preset emitter id>
    // output -> register new emitter with a preset

  // remove
    // <emitter id>
    // output -> removes the particle with that id

  // config
    // <emitter id>
    // <PARTICLE / EMITTER>
    // <param key>                    // HARDCODED KEYS, if EMITTER use emitter params.
    // <new param value>              // HARDCODED TYPES
    // output -> update the selected parameter of the selected emitter id to the new value

  // list
    // output -> all registered emitters

  // copy
    // <emitter id>
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

// TODO: perhaps make this a Command.literal() if possible
val emitterIdSuggestion: RequiredArgumentBuilder<ServerCommandSource, String> = CommandManager.argument("Registered Emitter ID", StringArgumentType.string())
  .suggests { _, builder ->
    allEmitterIds.forEach { key ->
      builder.suggest(key)
    }
    CompletableFuture.completedFuture(builder.build())
  }

val emitterConfigKeys = ArgConfigEmitterKeys()
val particleConfigKeys = ArgConfigParticleKeys()


// create
  // <emitter id>
  // <preset emitter id>
  // output -> register new emitter with a preset
fun argCreate() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("create")
    .then(CommandManager.argument("Emitter ID", StringArgumentType.string())
      .then(emitterIdSuggestion
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
          else if (newEmitterId !in allEmitterIds) {
            Text.literal("BPS - Preset Emitter ID \"$emitterPresetVal\" doesn't exist!\n" +
                    "BPS - Use \"/ManageEmitters list\" to view all Emitter ID's.")
              .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(200, 0, 0).rgb)))
          }
          else {
            Text.literal("BPS - Emitter ID \"$newEmitterId\" already exists\n" +
                    "BPS - Use \"/ManageEmitters list\" to view all Emitter ID's.")
              .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(200, 0, 0).rgb)))
          }

          context.source.sendFeedback( { feedback }, false )
          Command.SINGLE_SUCCESS
        }
      )
    )
}

// remove
  // <emitter id>
  // output -> removes the particle with that id
fun argRemove() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("remove")
    .then(emitterIdSuggestion
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

// list
  // output -> all registered emitters
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

// copy
  // <emitter id>
  // output -> give player a command block with the particle params (should be paste-able in kotlin)
fun argCopy() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("copy")
    .then(emitterIdSuggestion
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

// config
  // <emitter id>
  // <PARTICLE / EMITTER>
  // <param key>                    // HARDCODED KEYS, if EMITTER use emitter params.
  // <new param value>              // HARDCODED TYPES
  // output -> update the selected parameter of the selected emitter id to the new value
fun argConfig() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("config")
    .then(emitterIdSuggestion
      .then(CommandManager.literal("emitter")
        .then(emitterConfigKeys.maxCount())
        .then(emitterConfigKeys.spawnsPerTick())
        .then(emitterConfigKeys.loopDur())
        .then(emitterConfigKeys.loopDelay())
        .then(emitterConfigKeys.loopCount())
      )
      .then(CommandManager.literal("particle")
        .then(particleConfigKeys.shape())
        .then(particleConfigKeys.blockCurve())
        .then(particleConfigKeys.rotRandom())
        .then(particleConfigKeys.rotVelRandom())
        .then(particleConfigKeys.sizeRandom())
        .then(particleConfigKeys.uniformSize())
        .then(particleConfigKeys.velRandom())
        .then(particleConfigKeys.forceFields())
        .then(particleConfigKeys.gravity())
        .then(particleConfigKeys.drag())
        .then(particleConfigKeys.minVel())
        .then(particleConfigKeys.lifeTime())
        .then(particleConfigKeys.rotVelCurve())
        .then(particleConfigKeys.sizeCurve())
      )
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