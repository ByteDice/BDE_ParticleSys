package com.bytedice.bde_particles.commands
import com.bytedice.bde_particles.particleIdRegister.updateSingleParam
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import java.awt.Color


fun argUpdateEmitterParam(context: CommandContext<ServerCommandSource>, paramName: String, typeValue: Any) : MutableText {
  val emitterIdVal = StringArgumentType.getString(context, "Registered Emitter ID")

  val success = updateSingleParam(emitterIdVal, -1, "maxCount", typeValue)

  val feedback = if (success) {
    Text.literal("BPS - Successfully updated Emitter ID \"$emitterIdVal\" parameter \"$paramName\" to value \"$typeValue\"")
      .setStyle(Style.EMPTY.withColor(Color(0, 200, 0).rgb))
  }
  else {
    Text.literal("BPS - Failed to update Emitter ID \"$emitterIdVal\" parameter \"$paramName\" to value \"$typeValue\": Unknown Error")
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