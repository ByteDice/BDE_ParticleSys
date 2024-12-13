package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.emitterListArg
import com.bytedice.bde_particles.negativeFeedback
import com.bytedice.bde_particles.particles.*
import com.bytedice.bde_particles.positiveFeedback
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.awt.Color


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
// <create / remove / config / list / copy>
  // create
    // <emitter id>
    // <preset emitter id>
    // output -> register new emitter with a preset

  // remove
    // <emitter id>
    // output -> removes the particle with that id

  // config
    // <emitter id>
    // <param key>
    // <new param value>
    // output -> update the selected parameter of the selected emitter id to the new value

  // list
    // output -> all registered emitters

  // copy
    // <emitter id>
    // output -> give player a command block with the particle params (should be paste-able in kotlin)


object ManageEmitters {

  fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
    val command = CommandManager.literal("ManageEmitters")
      .requires { source -> source.hasPermissionLevel(2) }

    dispatcher.register(
      command
        .then(CommandManager.literal("create").then(create()))
        .then(CommandManager.literal("remove").then(remove()))
        .then(CommandManager.literal("config").then(config()))
        .then(CommandManager.literal("list")
          .executes { context ->
            val emitterIdList = idRegister.keys.sorted().joinToString("   ")

            val feedback = Text.literal("List of all registered Emitter IDs:\n$emitterIdList")
              .setStyle(Style.EMPTY.withColor(Color(0, 200, 0).rgb))

            context.source.sendFeedback({ feedback }, false)
            Command.SINGLE_SUCCESS
          }
        )
        //.then(CommandManager.literal("copy"))
    )
  }

  private fun create() : RequiredArgumentBuilder<ServerCommandSource, String> {
    return CommandManager.argument("New Emitter ID", StringArgumentType.string())
      .then(emitterListArg("Preset Emitter ID")
        .executes { context ->
          val newEmitterId = StringArgumentType.getString(context, "New Emitter ID")
          val presetEmitterId = StringArgumentType.getString(context, "Preset Emitter ID")

          createExec(context, newEmitterId, presetEmitterId)
          Command.SINGLE_SUCCESS
        }
      )
      .executes { context ->
        val newEmitterId = StringArgumentType.getString(context, "New Emitter ID")

        createExec(context, newEmitterId, "")
        Command.SINGLE_SUCCESS
      }
  }

  private fun createExec(context: CommandContext<ServerCommandSource>, newEmitterId: String, presetEmitterId: String) {
    val params = getEmitterDataById(presetEmitterId)
    val usedDefault = params == null

    val result = addToRegister(
      newEmitterId,
      if (usedDefault) { EmitterParams.DEFAULT } else { params!! },
    )

    if (usedDefault && result.second) {
      positiveFeedback(
        "Successfully added new Emitter with ID \"${result.first}\" to register.\n(DEFAULT params were used as a preset because the specified Preset Emitter ID wasn't valid.)",
        context
      )
    }
    else if (!usedDefault && result.second) {
      positiveFeedback(
        "Successfully added new Emitter with ID \"${result.first}\" and Preset Emitter ID \"$presetEmitterId\" to register.",
        context
      )
    }
    else {
      negativeFeedback(
        "Failed to add new Emitter with ID \"${result.first}\" to register.\nThe provided Emitter ID is either reserved for system use or already in use.",
        context
      )
    }
  }

  private fun remove() : RequiredArgumentBuilder<ServerCommandSource, String> {
    return emitterListArg("Emitter ID")
      .executes { context ->
        val emitterId = StringArgumentType.getString(context, "Emitter ID")

        val result = removeFromRegister(emitterId)

        if (result) {
          positiveFeedback(
            "Successfully removed Emitter ID \"$emitterId\" from register.",
            context
          )
        }
        else {
          negativeFeedback(
            "Failed to remove Emitter ID \"$emitterId\" from register.\nThe provided Emitter ID is either reserved for system use or already in use.",
            context
          )
        }

        Command.SINGLE_SUCCESS
      }
  }

  private fun config() : RequiredArgumentBuilder<ServerCommandSource, String> {
    var emitterList = emitterListArg("Emitter ID")

    emitterList = listConfigArgs(dataClassToArray(EmitterParams::class), emitterList) as RequiredArgumentBuilder<ServerCommandSource, String>

    return emitterList
  }

  // I am not going to torture myself by converting this to a json.
  /*
  private fun copy() : RequiredArgumentBuilder<ServerCommandSource, String> {
    return emitterList("Emitter ID").executes { context ->
      val emitterId = StringArgumentType.getString(context, "Emitter ID")

      Command.SINGLE_SUCCESS
    }
  }
  */
}