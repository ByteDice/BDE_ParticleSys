package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.particleIdRegister.SpawningShape
import com.bytedice.bde_particles.particleIdRegister.updateSingleParam
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.system.linux.X11
import java.awt.Color


fun argUpdateEmitterParam(context: CommandContext<ServerCommandSource>, paramName: String, typeValue: Any) : MutableText {
  val emitterIdVal = StringArgumentType.getString(context, "Registered Emitter ID")
  val success = updateSingleParam(emitterIdVal, -1, paramName, typeValue)

  val feedback = if (success) {
    Text.literal("BPS - Successfully updated Emitter ID \"$emitterIdVal\": Parameter \"$paramName\", value \"$typeValue\"")
      .setStyle(Style.EMPTY.withColor(Color(0, 200, 0).rgb))
  }
  else {
    Text.literal("BPS - Failed to update Emitter ID \"$emitterIdVal\": Parameter \"$paramName\", value \"$typeValue\": Unknown Error\n" +
            "Try checking if the emitter and parameter exist, and the value is the correct type.")
      .setStyle(Style.EMPTY.withColor(Color(200, 0, 0).rgb))
  }

  return feedback
}


fun argUpdateParticleParam(context: CommandContext<ServerCommandSource>, particleIndex: Int, paramName: String, typeValue: Any) : MutableText {
  val emitterIdVal = StringArgumentType.getString(context, "Registered Emitter ID")
  val success = updateSingleParam(emitterIdVal, particleIndex, paramName, typeValue)

  val feedback = if (success) {
    Text.literal("BPS - Successfully updated Emitter ID \"$emitterIdVal\": Index \"$particleIndex\", parameter \"$paramName\", value \"$typeValue\"")
      .setStyle(Style.EMPTY.withColor(Color(0, 200, 0).rgb))
  }
  else {
    Text.literal("BPS - Failed to update Emitter ID \"$emitterIdVal\": Index \"$particleIndex\", parameter \"$paramName\", value \"$typeValue\": Unknown Error\n" +
            "Try checking if the emitter, particle index, and parameter exist, and the value is the correct type.")
      .setStyle(Style.EMPTY.withColor(Color(200, 0, 0).rgb))
  }

  return feedback
}


class ArgConfigEmitterKeys {
  fun maxCount() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("maxCount")
      .then(
        CommandManager.argument("Int", IntegerArgumentType.integer())
          .executes { context ->
            val typeValue = IntegerArgumentType.getInteger(context, "Int")
            val feedback = argUpdateEmitterParam(context, "maxCount", typeValue)
            context.source.sendFeedback({ feedback }, false)
            Command.SINGLE_SUCCESS
          }
      )
  }
  fun spawnsPerTick() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("spawnsPerTick")
      .then(
        CommandManager.argument("Int", IntegerArgumentType.integer())
          .executes { context ->
            val typeValue = IntegerArgumentType.getInteger(context, "Int")
            val feedback = argUpdateEmitterParam(context, "spawnsPerTick", typeValue)
            context.source.sendFeedback({ feedback }, false)
            Command.SINGLE_SUCCESS
          }
      )
  }
  fun loopDur() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("loopDur")
      .then(
        CommandManager.argument("Int", IntegerArgumentType.integer())
          .executes { context ->
            val typeValue = IntegerArgumentType.getInteger(context, "Int")
            val feedback = argUpdateEmitterParam(context, "loopDur", typeValue)
            context.source.sendFeedback({ feedback }, false)
            Command.SINGLE_SUCCESS
          }
      )
  }
  fun loopDelay() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("loopDelay")
      .then(
        CommandManager.argument("Int", IntegerArgumentType.integer())
          .executes { context ->
            val typeValue = IntegerArgumentType.getInteger(context, "Int")
            val feedback = argUpdateEmitterParam(context, "loopDelay", typeValue)
            context.source.sendFeedback({ feedback }, false)
            Command.SINGLE_SUCCESS
          }
      )
  }
  fun loopCount() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("loopCount")
      .then(
        CommandManager.argument("Int", IntegerArgumentType.integer())
          .executes { context ->
            val typeValue = IntegerArgumentType.getInteger(context, "Int")
            val feedback = argUpdateEmitterParam(context, "loopCount", typeValue)
            context.source.sendFeedback({ feedback }, false)
            Command.SINGLE_SUCCESS
          }
      )
  }
}


class ArgConfigParticleKeys {
  fun shape() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("shape")
      .then(
        CommandManager.literal("circle")
          .then(
            CommandManager.argument("Radius", FloatArgumentType.floatArg())
              .executes { context ->
                val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
                val radius = FloatArgumentType.getFloat(context, "Radius")
                val feedback = argUpdateParticleParam(context, particleIndex, "shape", SpawningShape.Circle(radius))
                context.source.sendFeedback( { feedback }, false)
                Command.SINGLE_SUCCESS
              }
          )
      )
      .then(
        CommandManager.literal("rect")
          .then(
            CommandManager.argument("Width", FloatArgumentType.floatArg())
              .then(
                CommandManager.argument("Height", FloatArgumentType.floatArg())
                  .executes { context ->
                    val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
                    val w = FloatArgumentType.getFloat(context, "Width")
                    val h = FloatArgumentType.getFloat(context, "Height")
                    val feedback = argUpdateParticleParam(context, particleIndex, "shape", SpawningShape.Rect(Vector2f(w, h)))
                    context.source.sendFeedback( { feedback }, false)
                    Command.SINGLE_SUCCESS
                  }
              )
          )
      )
      .then(
        CommandManager.literal("cube")
          .then(
            CommandManager.argument("X", FloatArgumentType.floatArg())
              .then(
                CommandManager.argument("Y", FloatArgumentType.floatArg())
                  .then(
                    CommandManager.argument("Z", FloatArgumentType.floatArg())
                      .executes { context ->
                        val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
                        val x = FloatArgumentType.getFloat(context, "X")
                        val y = FloatArgumentType.getFloat(context, "Y")
                        val z = FloatArgumentType.getFloat(context, "Z")
                        val feedback = argUpdateParticleParam(context, particleIndex, "shape", SpawningShape.Cube(Vector3f(x, y, z)))
                        context.source.sendFeedback( { feedback }, false)
                        Command.SINGLE_SUCCESS
                      }
                  )
              )
          )
      )
      .then(
        CommandManager.literal("sphere")
          .then(
            CommandManager.argument("Radius", FloatArgumentType.floatArg())
              .executes { context ->
                val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
                val radius = FloatArgumentType.getFloat(context, "Radius")
                val feedback = argUpdateParticleParam(context, particleIndex, "shape", SpawningShape.Sphere(radius))
                context.source.sendFeedback( { feedback }, false)
                Command.SINGLE_SUCCESS
              }
          )
      )
  }
  fun blockCurve() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("blockCurve")
      .then(
        CommandManager.argument("Array (separate by comma)", StringArgumentType.string())
          .executes { context ->
            val typeValue = StringArgumentType.getString(context, "Array (separate by comma)")
            val typeArray = typeValue.split(",").toTypedArray()
            val feedback = argUpdateEmitterParam(context, "blockCurve", typeArray)
            context.source.sendFeedback({ feedback }, false)
            Command.SINGLE_SUCCESS
          }
      )
  }
  fun rotRandom() : LiteralArgumentBuilder<ServerCommandSource> {
    // I'd like to introduce you to, drumroll please... *drumroll*... The pyramid of "X1,Y1,Z1,X2,Y2,Z2"
    return CommandManager.literal("rotRandom")
      .then(CommandManager.argument("X1", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Y1", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Z1", FloatArgumentType.floatArg())
            .then(CommandManager.argument("X2", FloatArgumentType.floatArg())
              .then(CommandManager.argument("Y2", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Z2", FloatArgumentType.floatArg())
                  .executes { context ->
                    val x1 = FloatArgumentType.getFloat(context, "X1")
                    val y1 = FloatArgumentType.getFloat(context, "Y1")
                    val z1 = FloatArgumentType.getFloat(context, "Z1")
                    val x2 = FloatArgumentType.getFloat(context, "X2")
                    val y2 = FloatArgumentType.getFloat(context, "Y2")
                    val z2 = FloatArgumentType.getFloat(context, "Z2")

                    val feedback = argUpdateEmitterParam(context, "rotRandom", Pair(Vector3f(x1, y1, z1), Vector3f(x2, y2, z2)))
                    context.source.sendFeedback({ feedback }, false)
                    Command.SINGLE_SUCCESS
                  }
                )
              )
            )
          )
        )
      )
  }
  fun rotVelRandom() : LiteralArgumentBuilder<ServerCommandSource> {
    // I'd like to introduce you to, drumroll please... *drumroll*... The pyramid of "X1,Y1,Z1,X2,Y2,Z2"
    return CommandManager.literal("rotVelRandom") // TODO: convert this to a reusable variable
      .then(CommandManager.argument("X1", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Y1", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Z1", FloatArgumentType.floatArg())
            .then(CommandManager.argument("X2", FloatArgumentType.floatArg())
              .then(CommandManager.argument("Y2", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Z2", FloatArgumentType.floatArg())
                  .executes { context ->
                    val x1 = FloatArgumentType.getFloat(context, "X1")
                    val y1 = FloatArgumentType.getFloat(context, "Y1")
                    val z1 = FloatArgumentType.getFloat(context, "Z1")
                    val x2 = FloatArgumentType.getFloat(context, "X2")
                    val y2 = FloatArgumentType.getFloat(context, "Y2")
                    val z2 = FloatArgumentType.getFloat(context, "Z2")

                    val feedback = argUpdateEmitterParam(context, "rotVelRandom", Pair(Vector3f(x1, y1, z1), Vector3f(x2, y2, z2)))
                    context.source.sendFeedback({ feedback }, false)
                    Command.SINGLE_SUCCESS
                  }
                )
              )
            )
          )
        )
      )
  }
}