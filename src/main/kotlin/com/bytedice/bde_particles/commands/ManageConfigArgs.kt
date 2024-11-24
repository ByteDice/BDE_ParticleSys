package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.InterpolationCurves
import com.bytedice.bde_particles.particles.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
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
import java.awt.Color
import java.util.concurrent.CompletableFuture


// CLOSE THIS FILE IMMEDIATELY, YOU ARE ENTERING EGYPTIAN (pyramid) AND ITALIAN (spaghetti) TERRITORY

val curves = arrayOf("LINEAR", "SQRT", "EXPONENT", "CUBIC", "SINE", "COSINE", "INVERSE", "LOG", "EXP", "BOUNCE")

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
      .then(CommandManager.literal("circle")
        .then(CommandManager.argument("Radius", FloatArgumentType.floatArg())
          .executes { context ->
            val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
            val radius = FloatArgumentType.getFloat(context, "Radius")
            val feedback = argUpdateParticleParam(context, particleIndex, "shape", SpawningShape.CIRCLE(radius))
            context.source.sendFeedback( { feedback }, false)
            Command.SINGLE_SUCCESS
          }
        )
      )
      .then(CommandManager.literal("rect")
        .then(CommandManager.argument("Width", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Height", FloatArgumentType.floatArg())
            .executes { context ->
              val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
              val w = FloatArgumentType.getFloat(context, "Width")
              val h = FloatArgumentType.getFloat(context, "Height")
              val feedback = argUpdateParticleParam(context, particleIndex, "shape", SpawningShape.RECT(Vector2f(w, h)))
              context.source.sendFeedback( { feedback }, false)
              Command.SINGLE_SUCCESS
            }
          )
        )
      )
      .then(CommandManager.literal("cube")
        .then(CommandManager.argument("X", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Y", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Z", FloatArgumentType.floatArg())
              .executes { context ->
                val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
                val x = FloatArgumentType.getFloat(context, "X")
                val y = FloatArgumentType.getFloat(context, "Y")
                val z = FloatArgumentType.getFloat(context, "Z")
                val feedback = argUpdateParticleParam(context, particleIndex, "shape", SpawningShape.CUBE(Vector3f(x, y, z)))
                context.source.sendFeedback( { feedback }, false)
                Command.SINGLE_SUCCESS
              }
            )
          )
        )
      )
      .then(CommandManager.literal("sphere")
        .then(CommandManager.argument("Radius", FloatArgumentType.floatArg())
          .executes { context ->
            val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
            val radius = FloatArgumentType.getFloat(context, "Radius")
            val feedback = argUpdateParticleParam(context, particleIndex, "shape", SpawningShape.SPHERE(radius))
            context.source.sendFeedback( { feedback }, false)
            Command.SINGLE_SUCCESS
          }
        )
      )
  }
  fun blockCurve() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("blockCurve")
      .then(CommandManager.argument("Array (separate by comma)", StringArgumentType.string())
        .executes { context ->
          val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
          val typeValue = StringArgumentType.getString(context, "Array (separate by comma)")
          val typeArray = typeValue.split(",").toTypedArray()
          val feedback = argUpdateParticleParam(context, particleIndex, "blockCurve", typeArray)
          context.source.sendFeedback({ feedback }, false)
          Command.SINGLE_SUCCESS
        }
      )
  }
  fun rotRandom() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("rotRandom")
      .then(CommandManager.argument("Min X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Min Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Min Z", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Max X", FloatArgumentType.floatArg())
              .then(CommandManager.argument("Max Y", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Max Z", FloatArgumentType.floatArg())
                  .executes { context ->
                    val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
                    val vectors = x1x2ToPairVector3f(context)

                    val feedback = argUpdateParticleParam(context, particleIndex, "rotRandom", vectors)
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
    return CommandManager.literal("rotVelRandom")
      .then(CommandManager.argument("Min X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Min Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Min Z", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Max X", FloatArgumentType.floatArg())
              .then(CommandManager.argument("Max Y", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Max Z", FloatArgumentType.floatArg())
                  .executes { context ->
                    val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
                    val vectors = x1x2ToPairVector3f(context)

                    val feedback = argUpdateParticleParam(context, particleIndex, "rotVelRandom", vectors)
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
  fun sizeRandom() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("sizeRandom")
      .then(CommandManager.argument("Min X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Min Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Min Z", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Max X", FloatArgumentType.floatArg())
              .then(CommandManager.argument("Max Y", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Max Z", FloatArgumentType.floatArg())
                  .executes { context ->
                    val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
                    val vectors = x1x2ToPairVector3f(context)

                    val feedback = argUpdateParticleParam(context, particleIndex, "sizeRandom", vectors)
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
  fun uniformSize() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("uniformSize")
      .then(CommandManager.argument("Bool", BoolArgumentType.bool())
        .executes { context ->
          val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
          val uniform = BoolArgumentType.getBool(context, "Bool")
          val feedback = argUpdateParticleParam(context, particleIndex, "uniformSize", uniform)
          context.source.sendFeedback( { feedback }, false)
          Command.SINGLE_SUCCESS
        }
      )
  }
  fun velRandom() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("velRandom")
      .then(CommandManager.argument("Min X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Min Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Min Z", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Max X", FloatArgumentType.floatArg())
              .then(CommandManager.argument("Max Y", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Max Z", FloatArgumentType.floatArg())
                  .executes { context ->
                    val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
                    val vectors = x1x2ToPairVector3f(context)

                    val feedback = argUpdateParticleParam(context, particleIndex, "velRandom", vectors)
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
  fun forceFields() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("ForceFields")
      .then(argAddForceField())
      .then(argRemoveForceField())
  }
  fun gravity() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("gravity")
      .then(CommandManager.argument("X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Z", FloatArgumentType.floatArg())
            .executes { context ->
              val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
              val x1 = FloatArgumentType.getFloat(context, "X")
              val y1 = FloatArgumentType.getFloat(context, "Y")
              val z1 = FloatArgumentType.getFloat(context, "Z")

              val feedback = argUpdateParticleParam(context, particleIndex, "gravity", Vector3f(x1, y1, z1))
              context.source.sendFeedback({ feedback }, false)
              Command.SINGLE_SUCCESS
            }
          )
        )
      )
  }
  fun drag() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("drag")
      .then(CommandManager.argument("Float", FloatArgumentType.floatArg())
        .executes { context ->
          val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
          val value = FloatArgumentType.getFloat(context, "Float")
          val feedback = argUpdateParticleParam(context, particleIndex, "drag", value)
          context.source.sendFeedback( { feedback }, false)
          Command.SINGLE_SUCCESS
        }
      )
  }
  fun minVel() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("minVel")
      .then(CommandManager.argument("Float", FloatArgumentType.floatArg())
        .executes { context ->
          val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
          val value = FloatArgumentType.getFloat(context, "Float")
          val feedback = argUpdateParticleParam(context, particleIndex, "minVel", value)
          context.source.sendFeedback( { feedback }, false)
          Command.SINGLE_SUCCESS
        }
      )
  }
  fun lifeTime() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("lifeTime")
      .then(CommandManager.argument("min", IntegerArgumentType.integer())
        .then(CommandManager.argument("max", IntegerArgumentType.integer())
          .executes { context ->
            val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
            val min = IntegerArgumentType.getInteger(context, "min")
            val max = IntegerArgumentType.getInteger(context, "max")
            val feedback = argUpdateParticleParam(context, particleIndex, "lifeTime", Pair(min, max))
            context.source.sendFeedback( { feedback }, false)
            Command.SINGLE_SUCCESS
          }
        )
      )
  }
  fun rotVelCurve() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("rotVelCurve")
      .then(CommandManager.argument("Min X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Min Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Min Z", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Max X", FloatArgumentType.floatArg())
              .then(CommandManager.argument("Max Y", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Max Z", FloatArgumentType.floatArg())
                  .then(CommandManager.argument("Curve", StringArgumentType.string())
                    .suggests { _, builder ->
                      curves.forEach { builder.suggest(it) }
                      CompletableFuture.completedFuture(builder.build())
                    }
                    .executes { context ->
                      val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
                      val vectors = x1x2ToPairVector3f(context)
                      val curveString = StringArgumentType.getString(context, "Curve")

                      val feedback = argUpdateParticleParam(
                        context,
                        particleIndex,
                        "rotVelCurve",
                        Triple(vectors.first, vectors.second, getCurveByString(curveString)))

                      context.source.sendFeedback({ feedback }, false)
                      Command.SINGLE_SUCCESS
                    }
                  )
                )
              )
            )
          )
        )
      )
  }
  fun sizeCurve() : LiteralArgumentBuilder<ServerCommandSource> {
    return CommandManager.literal("sizeCurve")
      .then(CommandManager.argument("Min X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Min Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Min Z", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Max X", FloatArgumentType.floatArg())
              .then(CommandManager.argument("Max Y", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Max Z", FloatArgumentType.floatArg())
                  .then(CommandManager.argument("Curve", StringArgumentType.string())
                    .suggests { _, builder ->
                      curves.forEach { builder.suggest(it) }
                      CompletableFuture.completedFuture(builder.build())
                    }
                    .executes { context ->
                      val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
                      val vectors = x1x2ToPairVector3f(context)
                      val curveString = StringArgumentType.getString(context, "Curve")

                      val feedback = argUpdateParticleParam(
                        context,
                        particleIndex,
                        "sizeCurve",
                        Triple(vectors.first, vectors.second, getCurveByString(curveString)))

                      context.source.sendFeedback({ feedback }, false)
                      Command.SINGLE_SUCCESS
                    }
                  )
                )
              )
            )
          )
        )
      )
  }
}


fun getCurveByString(str: String) : InterpolationCurves {
  return when (str) {
    "LINEAR"   -> InterpolationCurves.LINEAR
    "SQRT"     -> InterpolationCurves.SQRT
    "EXPONENT" -> InterpolationCurves.EXPONENT
    "CUBIC"    -> InterpolationCurves.CUBIC
    "SINE"     -> InterpolationCurves.SINE
    "COSINE"   -> InterpolationCurves.COSINE
    "INVERSE"  -> InterpolationCurves.INVERSE
    "LOG"      -> InterpolationCurves.LOG
    "EXP"      -> InterpolationCurves.EXP
    "BOUNCE"   -> InterpolationCurves.BOUNCE
    else       -> InterpolationCurves.LINEAR
  }
}


fun x1x2ToPairVector3f(context: CommandContext<ServerCommandSource>) : Pair<Vector3f, Vector3f> {
  val x1 = FloatArgumentType.getFloat(context, "Min X")
  val y1 = FloatArgumentType.getFloat(context, "Min Y")
  val z1 = FloatArgumentType.getFloat(context, "Min Z")
  val x2 = FloatArgumentType.getFloat(context, "Max X")
  val y2 = FloatArgumentType.getFloat(context, "Max Y")
  val z2 = FloatArgumentType.getFloat(context, "Max Z")
  return Pair(Vector3f(x1, y1, z1), Vector3f(x2, y2, z2))
}


fun argAddForceField() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("add")
    .then(CommandManager.argument("Name", StringArgumentType.string())
      .then(CommandManager.argument("X Pos", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Y Pos", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Z Pos", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Min Force", FloatArgumentType.floatArg())
              .then(CommandManager.argument("Max Force", FloatArgumentType.floatArg())
                .then(CommandManager.literal("sphere")
                  .then(CommandManager.argument("Radius", FloatArgumentType.floatArg())
                    .executes { context -> forceFieldAddSphereExec(context) }
                  )
                )
                .then(CommandManager.literal("cube")
                  .then(CommandManager.argument("X Size", FloatArgumentType.floatArg())
                    .then(CommandManager.argument("Y Size", FloatArgumentType.floatArg())
                      .then(CommandManager.argument("Z Size", FloatArgumentType.floatArg())
                        .then(CommandManager.argument("X Force", FloatArgumentType.floatArg())
                          .then(CommandManager.argument("Y Force", FloatArgumentType.floatArg())
                            .then(CommandManager.argument("Z Force", FloatArgumentType.floatArg())
                              .executes { context -> forceFieldAddCubeExec(context) }
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
            )
          )
        )
      )
    )
}


fun argRemoveForceField() : LiteralArgumentBuilder<ServerCommandSource> {
  return CommandManager.literal("remove")
    .then(CommandManager.argument("Force Field Name", StringArgumentType.string())
      .suggests { context, builder ->
        val particleId = StringArgumentType.getString(context, "Registered Emitter ID")
        val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
        val params = getEmitterParams(particleId)
        val forceFields = params!!.particleTypes[particleIndex].forceFields

        forceFields.iterator().forEach {
          builder.suggest(it.name)
        }

        CompletableFuture.completedFuture(builder.build())
      }
      .executes { context ->
        val ffName = StringArgumentType.getString(context, "Force Field Name")
        val particleId = StringArgumentType.getString(context, "Registered Emitter ID")
        val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")

        val params = getEmitterParams(particleId)
        val forceFields = params!!.particleTypes[particleIndex].forceFields
        val newForceFields: MutableList<ForceField> = forceFields.toMutableList()

        for (forceField in forceFields) {
          if (forceField.name == ffName) {
            newForceFields.remove(forceField)
          }
        }

        val feedback = argUpdateParticleParam(
          context,
          particleIndex,
          "forceFields",
          newForceFields
        )
        context.source.sendFeedback( { feedback }, false)
        Command.SINGLE_SUCCESS
      }
    )
}


fun forceFieldAddSphereExec(context: CommandContext<ServerCommandSource>) : Int {
  val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
  val radius = FloatArgumentType.getFloat(context, "Radius")
  val emitterIdVal = StringArgumentType.getString(context, "Registered Emitter ID")

  val shape = ForceFieldShape.SPHERE(radius)
  val forceField = forceFieldGenericParams(context, shape)

  val currentParams = getEmitterParams(emitterIdVal)
  val forceFields = currentParams!!.particleTypes[particleIndex].forceFields

  val feedback = argUpdateParticleParam(
    context,
    particleIndex,
    "forceFields",
    forceFields.toMutableList().add(forceField)
  )

  context.source.sendFeedback({ feedback }, false)
  Command.SINGLE_SUCCESS

  return Command.SINGLE_SUCCESS
}


fun forceFieldAddCubeExec(context: CommandContext<ServerCommandSource>) : Int {
  val particleIndex = IntegerArgumentType.getInteger(context, "Particle Index")
  val xSize = FloatArgumentType.getFloat(context, "X Size")
  val ySize = FloatArgumentType.getFloat(context, "Y Size")
  val zSize = FloatArgumentType.getFloat(context, "Z Size")
  val xForce = FloatArgumentType.getFloat(context, "X Force")
  val yForce = FloatArgumentType.getFloat(context, "Y Force")
  val zForce = FloatArgumentType.getFloat(context, "Z Force")
  val emitterIdVal = StringArgumentType.getString(context, "Registered Emitter ID")

  val shape = ForceFieldShape.CUBE(Vector3f(xSize, ySize, zSize), Vector3f(xForce, yForce, zForce))
  val forceField = forceFieldGenericParams(context, shape)

  val currentParams = getEmitterParams(emitterIdVal)
  val forceFields = currentParams!!.particleTypes[particleIndex].forceFields

  val feedback = argUpdateParticleParam(
    context,
    particleIndex,
    "forceFields",
    forceFields.toMutableList().add(forceField)
  )

  context.source.sendFeedback({ feedback }, false)
  Command.SINGLE_SUCCESS

  return Command.SINGLE_SUCCESS
}


fun forceFieldGenericParams(context: CommandContext<ServerCommandSource>, shape: ForceFieldShape) : ForceField {
  val name = StringArgumentType.getString(context, "Name")
  val posX = FloatArgumentType.getFloat(context, "X Pos")
  val posY = FloatArgumentType.getFloat(context, "Y Pos")
  val posZ = FloatArgumentType.getFloat(context, "Z Pos")
  val minForce = FloatArgumentType.getFloat(context, "Min Force")
  val maxForce = FloatArgumentType.getFloat(context, "Max Force")

  return ForceField(
    name.replace(" ", "_"),
    Vector3f(posX, posY, posZ),
    Pair(minForce, maxForce),
    shape
  )
}