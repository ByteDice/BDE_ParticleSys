@file:Suppress("UNCHECKED_CAST")

package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.curveListArg
import com.bytedice.bde_particles.negativeFeedback
import com.bytedice.bde_particles.particles.*
import com.bytedice.bde_particles.positiveFeedback
import com.bytedice.bde_particles.stringToCurve
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.registry.Registries
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.joml.Vector2f
import org.joml.Vector3f
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties


typealias CommandArgumentBuilder = (
  access: KProperty1<EmitterParams, *>,
  config: LiteralArgumentBuilder<ServerCommandSource>,
  reg: CommandRegistryAccess
) -> LiteralArgumentBuilder<ServerCommandSource>?


fun listConfigArgs(configArgs: Array<String>,
                   rootArg: RequiredArgumentBuilder<ServerCommandSource, *>,
                   registryAccess: CommandRegistryAccess
                  ): RequiredArgumentBuilder<ServerCommandSource, *>
{
  configArgs.forEach { arg ->
    var configArg = CommandManager.literal(arg)

    val argAccess = parseArgNameToAccess(arg)!!
    val func = parseArgTypeToFunc(argAccess.returnType, configArg)

    if (func != null) {
      configArg = func(argAccess, configArg, registryAccess)
    }

    rootArg.then(configArg)
  }

  return rootArg
}



fun dataClassToArray(dataClass: KClass<*>): Array<String> {
  return dataClass.memberProperties.map { it.name }.toTypedArray()
}


fun parseArgNameToAccess(name: String) : KProperty1<EmitterParams, *>? {
  val access = EmitterParams::class.memberProperties
    .firstOrNull { it.name == name }

  return access
}

fun parseArgTypeToFunc(type: KType, configArg: LiteralArgumentBuilder<ServerCommandSource>): CommandArgumentBuilder? {
  return when (type.classifier) {
    Int::class -> { access, _, _ ->
      configArg.then(intArg(access as KProperty1<EmitterParams, Int>))
    }
    Float::class -> { access, _, _ ->
      configArg.then(floatArg(access as KProperty1<EmitterParams, Float>))
    }
    String::class -> { access, _, _ ->
      configArg.then(stringArg(access as KProperty1<EmitterParams, String>))
    }
    ParamClasses.Duration::class -> { access, config, _ ->
      durArg(access as KProperty1<EmitterParams, ParamClasses.Duration>, config)
    }
    Vector3f::class -> { access, _, _ ->
      configArg.then(vec3fArg(access as KProperty1<EmitterParams, Vector3f>))
    }
    ParamClasses.PairInt::class -> { access, _, _ ->
      configArg.then(pairIntArg(access as KProperty1<EmitterParams, ParamClasses.PairInt>))
    }
    SpawningShape::class -> { access, config, _ ->
      shapeArg(access as KProperty1<EmitterParams, SpawningShape>, config)
    }
    ParamClasses.PairVec3f::class -> { access, config, _ ->
      pairVec3fArg(access as KProperty1<EmitterParams, ParamClasses.PairVec3f>, config)
    }
    ParamClasses.PairFloat::class -> { access, _, _ ->
      configArg.then(pairFloatArg(access as KProperty1<EmitterParams, ParamClasses.PairFloat>))
    }
    ParamClasses.TransformWithVel::class -> { access, config, _ ->
      transformWithVelArg(access as KProperty1<EmitterParams, ParamClasses.TransformWithVel>, config)
    }
    ParamClasses.StringCurve::class -> { access, config, reg ->
      stringCurveArg(access as KProperty1<EmitterParams, ParamClasses.StringCurve>, config, reg)
    }
    ParamClasses.LerpValVec3f::class -> { access, config, _ ->
      lerpValVec3fArg(access as KProperty1<EmitterParams, ParamClasses.LerpValVec3f>, config)
    }
    ParamClasses.ForceFieldArray::class -> { access, config, _ ->
      forceFieldArg(access as KProperty1<EmitterParams, ParamClasses.ForceFieldArray>, config)
    }
    ParamClasses.LerpValInt::class -> { access, config, _ ->
      lerpValIntArg(access as KProperty1<EmitterParams, ParamClasses.LerpValInt>, config)
    }
    else -> null
  }
}


fun successText(access: KProperty1<EmitterParams, *>, value: String, id: String, context: CommandContext<ServerCommandSource>) {
  positiveFeedback("Successfully updated parameter \"${access.name}\" to \"$value\" of Emitter ID \"$id\"", context)
}


fun intArg(access: KProperty1<EmitterParams, Int>) : RequiredArgumentBuilder<ServerCommandSource, Int> {
  return CommandManager.argument("Int", IntegerArgumentType.integer())
    .executes { context ->
      val id = StringArgumentType.getString(context, "Emitter ID")
      val value = IntegerArgumentType.getInteger(context, "Int")

      updateParam(id, access, value)

      successText(access, value.toString(), id, context)
      Command.SINGLE_SUCCESS
    }
}

fun floatArg(access: KProperty1<EmitterParams, Float>) : RequiredArgumentBuilder<ServerCommandSource, Float> {
  return CommandManager.argument("Float", FloatArgumentType.floatArg())
    .executes { context ->
      val id = StringArgumentType.getString(context, "Emitter ID")
      val value = FloatArgumentType.getFloat(context, "Float")

      updateParam(id, access, value)

      successText(access, value.toString(), id, context)
      Command.SINGLE_SUCCESS
    }
}

fun stringArg(access: KProperty1<EmitterParams, String>) : RequiredArgumentBuilder<ServerCommandSource, String> {
  return CommandManager.argument("String", StringArgumentType.string())
    .executes { context ->
      val id = StringArgumentType.getString(context, "Emitter ID")
      val value = StringArgumentType.getString(context, "String")

      updateParam(id, access, value)

      successText(access, value.toString(), id, context)
      Command.SINGLE_SUCCESS
    }
}

fun durArg(access: KProperty1<EmitterParams, ParamClasses.Duration>,
           configArg: LiteralArgumentBuilder<ServerCommandSource>
          ) : LiteralArgumentBuilder<ServerCommandSource>
{
  return configArg
    .then(CommandManager.literal("SingleBurst")
      .then(CommandManager.argument("loopDur", IntegerArgumentType.integer())
        .executes { context -> durArgExec("SingleBurst", access, context); Command.SINGLE_SUCCESS }
      )
    )
    .then(CommandManager.literal("MultiBurst")
      .then(CommandManager.argument("loopDur", IntegerArgumentType.integer())
        .then(CommandManager.argument("loopDelay", IntegerArgumentType.integer())
          .then(CommandManager.argument("loopCount", IntegerArgumentType.integer())
            .executes { context -> durArgExec("MultiBurst", access, context); Command.SINGLE_SUCCESS }
          )
        )
      )
    )
    .then(CommandManager.literal("InfiniteLoop")
      .then(CommandManager.argument("loopDur", IntegerArgumentType.integer())
        .then(CommandManager.argument("loopDelay", IntegerArgumentType.integer())
          .executes { context -> durArgExec("InfiniteLoop", access, context); Command.SINGLE_SUCCESS }
        )
      )
    )
}

fun durArgExec(paramName: String, access: KProperty1<EmitterParams, ParamClasses.Duration>, context: CommandContext<ServerCommandSource>) {
  val id = StringArgumentType.getString(context, "Emitter ID")
  val loopDur = IntegerArgumentType.getInteger(context, "loopDur")


  when (paramName) {
    "SingleBurst"  -> {
      updateParam(id, access, ParamClasses.Duration.SingleBurst(loopDur))
    }
    "MultiBurst"   -> {
      val loopDelay = IntegerArgumentType.getInteger(context, "loopDelay")
      val loopCount = IntegerArgumentType.getInteger(context, "loopCount")
      updateParam(id, access, ParamClasses.Duration.MultiBurst(loopDur, loopDelay, loopCount))
    }
    "InfiniteLoop" -> {
      val loopDelay = IntegerArgumentType.getInteger(context, "loopDelay")
      updateParam(id, access, ParamClasses.Duration.InfiniteLoop(loopDur, loopDelay))
    }
  }

  successText(access, access::class.simpleName ?: "UNKNOWN", id, context)
}

fun vec3fArg(access: KProperty1<EmitterParams, Vector3f>) : RequiredArgumentBuilder<ServerCommandSource, Float> {
  return CommandManager.argument("X", FloatArgumentType.floatArg())
    .then(CommandManager.argument("Y", FloatArgumentType.floatArg())
      .then(CommandManager.argument("Z", FloatArgumentType.floatArg())
        .executes { context ->
          val id = StringArgumentType.getString(context, "Emitter ID")
          val valueX = FloatArgumentType.getFloat(context, "X")
          val valueY = FloatArgumentType.getFloat(context, "Y")
          val valueZ = FloatArgumentType.getFloat(context, "Z")

          updateParam(id, access, Vector3f(valueX, valueY, valueZ))

          successText(access, "[$valueX, $valueY, $valueZ]", id, context)
          Command.SINGLE_SUCCESS
        }
      )
    )
}

fun pairIntArg(access: KProperty1<EmitterParams, ParamClasses.PairInt>) : RequiredArgumentBuilder<ServerCommandSource, Int> {
  return CommandManager.argument("Min Int", IntegerArgumentType.integer())
    .then(CommandManager.argument("Max Int", IntegerArgumentType.integer())
      .executes { context ->
        val id = StringArgumentType.getString(context, "Emitter ID")
        val valueX = IntegerArgumentType.getInteger(context, "Min Int")
        val valueY = IntegerArgumentType.getInteger(context, "Max Int")

        updateParam(id, access, ParamClasses.PairInt(valueX, valueY))
        successText(access, "[$valueX, $valueY]", id, context)
        Command.SINGLE_SUCCESS
      }
    )
}

fun shapeArg(access: KProperty1<EmitterParams, SpawningShape>,
             configArg: LiteralArgumentBuilder<ServerCommandSource>
            ) : LiteralArgumentBuilder<ServerCommandSource>
{
  return configArg
    .then(CommandManager.literal("Circle")
      .then(CommandManager.argument("Radius", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Spawn On Edge", BoolArgumentType.bool())
          .executes { context -> shapeArgExec("Circle", access, context); Command.SINGLE_SUCCESS }
        )
      )
    )
    .then(CommandManager.literal("Rect")
      .then(CommandManager.argument("X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Spawn On Edge", BoolArgumentType.bool())
            .executes { context -> shapeArgExec("Rect", access, context); Command.SINGLE_SUCCESS }
          )
        )
      )
    )
    .then(CommandManager.literal("Cube")
      .then(CommandManager.argument("X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Z", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Spawn On Edge", BoolArgumentType.bool())
              .executes { context -> shapeArgExec("Cube", access, context); Command.SINGLE_SUCCESS }
            )
          )
        )
      )
    )
    .then(CommandManager.literal("Sphere")
      .then(CommandManager.argument("Radius", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Spawn On Edge", BoolArgumentType.bool())
          .executes { context -> shapeArgExec("Sphere", access, context); Command.SINGLE_SUCCESS }
        )
      )
    )
    .then(CommandManager.literal("Point")
      .executes { context -> shapeArgExec("Point", access, context); Command.SINGLE_SUCCESS }
    )
}

fun shapeArgExec(paramName: String, access: KProperty1<EmitterParams, SpawningShape>, context: CommandContext<ServerCommandSource>) {
  val id = StringArgumentType.getString(context, "Emitter ID")

  when (paramName) {
    "Circle" -> {
      val radius = FloatArgumentType.getFloat(context, "Radius")
      val onEdge = BoolArgumentType.getBool(context, "Spawn On Edge")
      updateParam(id, access, SpawningShape.Circle(radius, onEdge))
      successText(access, "Circle", id, context)
    }
    "Rect"   -> {
      val onEdge = BoolArgumentType.getBool(context, "Spawn On Edge")
      val x = FloatArgumentType.getFloat(context, "X")
      val y = FloatArgumentType.getFloat(context, "Y")
      updateParam(id, access, SpawningShape.Rect(Vector2f(x, y), onEdge))
      successText(access, "Rect", id, context)
    }
    "Cube"   -> {
      val onEdge = BoolArgumentType.getBool(context, "Spawn On Edge")
      val x = FloatArgumentType.getFloat(context, "X")
      val y = FloatArgumentType.getFloat(context, "Y")
      val z = FloatArgumentType.getFloat(context, "Z")
      updateParam(id, access, SpawningShape.Cube(Vector3f(x, y, z), onEdge))
      successText(access, "Cube", id, context)
    }
    "Sphere" -> {
      val radius = FloatArgumentType.getFloat(context, "Radius")
      val onEdge = BoolArgumentType.getBool(context, "Spawn On Edge")
      updateParam(id, access, SpawningShape.Sphere(radius, onEdge))
      successText(access, "Sphere", id, context)
    }
    "Point"  -> {
      updateParam(id, access, SpawningShape.Point)
      successText(access, "Point", id, context)
    }
  }
}

fun pairVec3fArg(access: KProperty1<EmitterParams,ParamClasses.PairVec3f>,
                 configArg: LiteralArgumentBuilder<ServerCommandSource>
                ) : LiteralArgumentBuilder<ServerCommandSource>
{
  return configArg
    .then(CommandManager.literal("NonUniform")
      .then(CommandManager.argument("Min X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Min Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Min Z", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Max X", FloatArgumentType.floatArg())
              .then(CommandManager.argument("Max Y", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Max Z", FloatArgumentType.floatArg())
                  .executes { context ->
                    val id = StringArgumentType.getString(context, "Emitter ID")
                    val minX = FloatArgumentType.getFloat(context, "Min X")
                    val minY = FloatArgumentType.getFloat(context, "Min Y")
                    val minZ = FloatArgumentType.getFloat(context, "Min Z")
                    val maxX = FloatArgumentType.getFloat(context, "Max X")
                    val maxY = FloatArgumentType.getFloat(context, "Max Y")
                    val maxZ = FloatArgumentType.getFloat(context, "Max Z")

                    updateParam(id, access, ParamClasses.PairVec3f.NonUniform(minX, minY, minZ, maxX, maxY, maxZ))

                    successText(access, "NonUniform", id, context)
                    Command.SINGLE_SUCCESS
                  }
                )
              )
            )
          )
        )
      )
    )
    .then(CommandManager.literal("Uniform")
      .then(CommandManager.argument("X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Y", FloatArgumentType.floatArg())
          .executes { context ->
            val id = StringArgumentType.getString(context, "Emitter ID")
            val min = FloatArgumentType.getFloat(context, "X")
            val max = FloatArgumentType.getFloat(context, "Y")

            updateParam(id, access, ParamClasses.PairVec3f.Uniform(min, max))

            successText(access, "Uniform", id, context)
            Command.SINGLE_SUCCESS
          }
        )
      )
    )
    .then(CommandManager.literal("Null")
      .executes { context ->
        val id = StringArgumentType.getString(context, "Emitter ID")

        updateParam(id, access, ParamClasses.PairVec3f.Null)

        successText(access, "Null", id, context)
        Command.SINGLE_SUCCESS
      }
    )
}

fun pairFloatArg(access: KProperty1<EmitterParams, ParamClasses.PairFloat>) : RequiredArgumentBuilder<ServerCommandSource, Float> {
  return CommandManager.argument("Min Float", FloatArgumentType.floatArg())
    .then(CommandManager.argument("Max Float", FloatArgumentType.floatArg())
      .executes { context ->
        val id = StringArgumentType.getString(context, "Emitter ID")
        val valueX = FloatArgumentType.getFloat(context, "Min Float")
        val valueY = FloatArgumentType.getFloat(context, "Max Float")

        updateParam(id, access, ParamClasses.PairFloat(valueX, valueY))
        successText(access, "[$valueX, $valueY]", id, context)
        Command.SINGLE_SUCCESS
      }
    )
}

fun transformWithVelArg(access: KProperty1<EmitterParams, ParamClasses.TransformWithVel>,
                        configArg: LiteralArgumentBuilder<ServerCommandSource>
                       ) : LiteralArgumentBuilder<ServerCommandSource>
{
  return configArg
    .then(CommandManager.literal("RotOnly")
      .executes { context ->
        val id = StringArgumentType.getString(context, "Emitter ID")

        updateParam(id, access, ParamClasses.TransformWithVel.RotOnly)
        successText(access, "RotOnly", id, context)
        Command.SINGLE_SUCCESS
      }
    )
    .then(CommandManager.literal("ScaleAndRot")
      .then(CommandManager.argument("Scale Multiplier", FloatArgumentType.floatArg())
        .executes { context ->
          val id = StringArgumentType.getString(context, "Emitter ID")
          val scaleMul = FloatArgumentType.getFloat(context, "Scale Multiplier")

          updateParam(id, access, ParamClasses.TransformWithVel.ScaleAndRot(scaleMul))
          successText(access, "ScaleAndRot", id, context)
          Command.SINGLE_SUCCESS
        }
      )
    )
    .then(CommandManager.literal("None")
      .executes { context ->
        val id = StringArgumentType.getString(context, "Emitter ID")

        updateParam(id, access, ParamClasses.TransformWithVel.None)
        successText(access, "None", id, context)
        Command.SINGLE_SUCCESS
      }
    )
}


fun stringCurveArg(access: KProperty1<EmitterParams, ParamClasses.StringCurve>,
                   configArg: LiteralArgumentBuilder<ServerCommandSource>,
                   registryAccess: CommandRegistryAccess
                  ) : LiteralArgumentBuilder<ServerCommandSource>
{
  return configArg
    .then(CommandManager.literal("Add")
      .then(CommandManager.argument("Item", ItemStackArgumentType.itemStack(registryAccess))
        .then(CommandManager.argument("Index", IntegerArgumentType.integer())
          .executes { context ->
            val index = IntegerArgumentType.getInteger(context, "Index")
            stringCurveAddArg(context, access, index)
            Command.SINGLE_SUCCESS
          }
        )
        .executes { context ->
          stringCurveAddArg(context, access, -1)
          Command.SINGLE_SUCCESS
        }
      )
    )
    .then(CommandManager.literal("Remove")
      .then(CommandManager.argument("Index", IntegerArgumentType.integer())
        .executes { context ->
          val index = IntegerArgumentType.getInteger(context, "Index")
          stringCurveRemoveArg(context, access, index)
          Command.SINGLE_SUCCESS
        }
      )
      .executes { context ->
        stringCurveRemoveArg(context, access, -1)
        Command.SINGLE_SUCCESS
      }
    )
    .then(CommandManager.literal("Curve")
      .then(curveListArg("Curve")
        .executes { context ->
          val id = StringArgumentType.getString(context, "Emitter ID")
          val curveName = StringArgumentType.getString(context, "Curve")

          val curve = stringToCurve(curveName)
          val modelCurve = getEmitterDataById(id)?.modelCurve

          if (curve != null && modelCurve != null) {
            modelCurve.curve = curve
            updateParam(id, access, modelCurve)
            successText(access, curveName, id, context)
            Command.SINGLE_SUCCESS
          }
          else {
            println("$modelCurve, $curve, $curveName")
            negativeFeedback("Failed to update param \"modelCurve.curve\"! The curve is invalid!", context)
            Command.SINGLE_SUCCESS
          }
        }
      )
    )
}



fun stringCurveAddArg(context: CommandContext<ServerCommandSource>,
                      access: KProperty1<EmitterParams, ParamClasses.StringCurve>,
                      index: Int
) {
  val id = StringArgumentType.getString(context, "Emitter ID")
  val item = ItemStackArgumentType.getItemStackArgument(context, "Item").item
  val itemId = Registries.ITEM.getId(item).toString()

  val modelCurve = getEmitterDataById(id)?.modelCurve

  if (index == -1 && modelCurve != null) {
    modelCurve.array = modelCurve.array.toMutableList().apply { add(itemId) }.toTypedArray()
    updateParam(id, access, modelCurve)
  }
  else if (modelCurve != null) {
    modelCurve.array = modelCurve.array.toMutableList().apply { add(index.coerceIn(0, modelCurve.array.lastIndex), itemId) }.toTypedArray()
    updateParam(id, access, modelCurve)
  }

  successText(access, "++$item", id, context)
}

fun stringCurveRemoveArg(context: CommandContext<ServerCommandSource>,
                      access: KProperty1<EmitterParams, ParamClasses.StringCurve>,
                      index: Int
) {
  val id = StringArgumentType.getString(context, "Emitter ID")

  val modelCurve = getEmitterDataById(id)?.modelCurve

  val item: String

  if (index == -1 && modelCurve != null && modelCurve.array.isNotEmpty()) {
    item = modelCurve.array.last()
    modelCurve.array = modelCurve.array.toMutableList().apply { removeLast() }.toTypedArray()
    updateParam(id, access, modelCurve)
  }
  else if (modelCurve != null && modelCurve.array.isNotEmpty()) {
    item = modelCurve.array[index.coerceIn(0, modelCurve.array.lastIndex)]
    modelCurve.array = modelCurve.array.toMutableList().apply { removeAt(index.coerceIn(0, modelCurve.array.lastIndex)) }.toTypedArray()
    updateParam(id, access, modelCurve)
  }
  else {
    negativeFeedback("Couldn't remove model. There are no models in the Emitter.", context)
    return
  }

  successText(access, "--$item", id, context)
}

fun lerpValVec3fArg(access: KProperty1<EmitterParams,ParamClasses.LerpValVec3f>,
                    configArg: LiteralArgumentBuilder<ServerCommandSource>
              ) : LiteralArgumentBuilder<ServerCommandSource>
{
  return configArg
    .then(CommandManager.literal("LerpVec3f")
      .then(CommandManager.argument("Min X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Min Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Min Z", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Max X", FloatArgumentType.floatArg())
              .then(CommandManager.argument("Max Y", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Max Z", FloatArgumentType.floatArg())
                  .then(curveListArg("Curve")
                    .executes { context ->
                      val id = StringArgumentType.getString(context, "Emitter ID")
                      val minX = FloatArgumentType.getFloat(context, "Min X")
                      val minY = FloatArgumentType.getFloat(context, "Min Y")
                      val minZ = FloatArgumentType.getFloat(context, "Min Z")
                      val maxX = FloatArgumentType.getFloat(context, "Max X")
                      val maxY = FloatArgumentType.getFloat(context, "Max Y")
                      val maxZ = FloatArgumentType.getFloat(context, "Max Z")
                      val curveName = StringArgumentType.getString(context, "Curve")

                      val curve = stringToCurve(curveName)

                      updateParam(id, access, ParamClasses.LerpValVec3f.LerpVec3f(minX, minY, minZ, maxX, maxY, maxZ, curve!!))

                      successText(access, "LerpVec3f", id, context)
                      Command.SINGLE_SUCCESS
                    }
                  )
                )
              )
            )
          )
        )
      )
    )
    .then(CommandManager.literal("MultiLerpVec3f")
      .then(CommandManager.argument("Min X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Min Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Min Z", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Max X", FloatArgumentType.floatArg())
              .then(CommandManager.argument("Max Y", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Max Z", FloatArgumentType.floatArg())
                  .then(curveListArg("CurveX")
                    .then(curveListArg("CurveY")
                      .then(curveListArg("CurveZ")
                        .executes { context ->
                          val id = StringArgumentType.getString(context, "Emitter ID")
                          val minX = FloatArgumentType.getFloat(context, "Min X")
                          val minY = FloatArgumentType.getFloat(context, "Min Y")
                          val minZ = FloatArgumentType.getFloat(context, "Min Z")
                          val maxX = FloatArgumentType.getFloat(context, "Max X")
                          val maxY = FloatArgumentType.getFloat(context, "Max Y")
                          val maxZ = FloatArgumentType.getFloat(context, "Max Z")
                          val curveNameX = StringArgumentType.getString(context, "CurveX")
                          val curveNameY = StringArgumentType.getString(context, "CurveY")
                          val curveNameZ = StringArgumentType.getString(context, "CurveZ")

                          val curveX = stringToCurve(curveNameX)
                          val curveY = stringToCurve(curveNameY)
                          val curveZ = stringToCurve(curveNameZ)

                          updateParam(id, access, ParamClasses.LerpValVec3f.MultiLerpVec3f(minX, minY, minZ, maxX, maxY, maxZ, curveX!!, curveY!!, curveZ!!))

                          successText(access, "MultiLerpVec3f", id, context)
                          Command.SINGLE_SUCCESS
                        }
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
    .then(CommandManager.literal("LerpUniform")
      .then(CommandManager.argument("Min", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Max", FloatArgumentType.floatArg())
          .then(curveListArg("Curve")
            .executes { context ->
              val id = StringArgumentType.getString(context, "Emitter ID")
              val min = FloatArgumentType.getFloat(context, "Min")
              val max = FloatArgumentType.getFloat(context, "Max")
              val curveName = StringArgumentType.getString(context, "Curve")

              val curve = stringToCurve(curveName)

              updateParam(id, access, ParamClasses.LerpValVec3f.LerpUniform(min, max, curve!!))

              successText(access, "LerpUniform", id, context)
              Command.SINGLE_SUCCESS
            }
          )
        )
      )
    )
    .then(CommandManager.literal("Constant")
      .then(CommandManager.argument("X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Z", FloatArgumentType.floatArg())
            .executes { context ->
              val id = StringArgumentType.getString(context, "Emitter ID")
              val x = FloatArgumentType.getFloat(context, "X")
              val y = FloatArgumentType.getFloat(context, "Y")
              val z = FloatArgumentType.getFloat(context, "Z")

              updateParam(id, access, ParamClasses.LerpValVec3f.Constant(Vector3f(x, y, z)))

              successText(access, "Constant", id, context)
              Command.SINGLE_SUCCESS
            }
          )
        )
      )
    )
    .then(CommandManager.literal("Null")
      .executes { context ->
        val id = StringArgumentType.getString(context, "Emitter ID")

        updateParam(id, access, ParamClasses.LerpValVec3f.Null)

        successText(access, "Null", id, context)
        Command.SINGLE_SUCCESS
      }
    )
}


fun forceFieldArg(access: KProperty1<EmitterParams,ParamClasses.ForceFieldArray>,
                  configArg: LiteralArgumentBuilder<ServerCommandSource>
                 ) : LiteralArgumentBuilder<ServerCommandSource>
{
  return configArg
    .then(CommandManager.literal("Add")
      .then(CommandManager.argument("X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Z", FloatArgumentType.floatArg())
            .then(CommandManager.literal("Sphere")
              .then(CommandManager.argument("Radius", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Min Force", FloatArgumentType.floatArg())
                  .then(CommandManager.argument("Max Force", FloatArgumentType.floatArg())
                    .executes { context ->
                      forceFieldAddArg(context, access, "Sphere")
                      Command.SINGLE_SUCCESS
                    }
                  )
                )
              )
            )
            .then(CommandManager.literal("Cube")
              .then(CommandManager.argument("Size X", FloatArgumentType.floatArg())
                .then(CommandManager.argument("Size Y", FloatArgumentType.floatArg())
                  .then(CommandManager.argument("Size Z", FloatArgumentType.floatArg())
                    .then(CommandManager.argument("Dir X", FloatArgumentType.floatArg())
                      .then(CommandManager.argument("Dir Y", FloatArgumentType.floatArg())
                        .then(CommandManager.argument("Dir Z", FloatArgumentType.floatArg())
                          .then(CommandManager.argument("Force", FloatArgumentType.floatArg())
                            .executes { context ->
                              forceFieldAddArg(context, access, "Cube")
                              Command.SINGLE_SUCCESS
                            }
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



    .then(CommandManager.literal("Remove")
      .then(CommandManager.argument("Index", IntegerArgumentType.integer())
        .executes { context ->
          val index = IntegerArgumentType.getInteger(context, "Index")
          forceFieldRemoveArg(context, access, index)
          Command.SINGLE_SUCCESS
        }
      )
      .executes { context ->
        forceFieldRemoveArg(context, access, -1)
        Command.SINGLE_SUCCESS
      }
    )
}

fun forceFieldAddArg(context: CommandContext<ServerCommandSource>,
                     access: KProperty1<EmitterParams, ParamClasses.ForceFieldArray>,
                     shape: String
) {
  val id = StringArgumentType.getString(context, "Emitter ID")
  val x = FloatArgumentType.getFloat(context, "X")
  val y = FloatArgumentType.getFloat(context, "Y")
  val z = FloatArgumentType.getFloat(context, "Z")


  val forceFields = getEmitterDataById(id)?.forceFields
  var forceField: ForceField? = null

  if (shape == "Sphere") {
    val radius = FloatArgumentType.getFloat(context, "Radius")
    val minForce = FloatArgumentType.getFloat(context, "Min Force")
    val maxForce = FloatArgumentType.getFloat(context, "Max Force")

    forceField = ForceField(
      Vector3f(x, y, z),
      ForceFieldShape.Sphere(radius, Pair(minForce, maxForce))
    )
  }
  else if (shape == "Cube") {
    val sizeX = FloatArgumentType.getFloat(context, "Size X")
    val sizeY = FloatArgumentType.getFloat(context, "Size Y")
    val sizeZ = FloatArgumentType.getFloat(context, "Size Z")
    val dirX = FloatArgumentType.getFloat(context, "Dir X")
    val dirY = FloatArgumentType.getFloat(context, "Dir Y")
    val dirZ = FloatArgumentType.getFloat(context, "Dir Z")
    val force = FloatArgumentType.getFloat(context, "Force")

    forceField = ForceField(
      Vector3f(x, y, z),
      ForceFieldShape.Cube(Vector3f(sizeX, sizeY, sizeZ), Vector3f(dirX, dirY, dirZ), force)
    )
  }

  if (forceFields != null &&  forceField != null) {
    forceFields.array = forceFields.array.toMutableList().apply { add(forceField) }.toTypedArray()
    updateParam(id, access, forceFields)
    successText(access, "++ForceField", id, context)
  }
  else {
    negativeFeedback("Failed to update param \"forceFields\"! The Emitter ID is most likely invalid!", context)
  }
}

fun forceFieldRemoveArg(context: CommandContext<ServerCommandSource>,
                         access: KProperty1<EmitterParams, ParamClasses.ForceFieldArray>,
                         index: Int
) {
  val id = StringArgumentType.getString(context, "Emitter ID")

  val forceFields = getEmitterDataById(id)?.forceFields

  if (index == -1 && forceFields != null && forceFields.array.isNotEmpty()) {
    forceFields.array = forceFields.array.toMutableList().apply { removeLast() }.toTypedArray()
    updateParam(id, access, forceFields)
  }
  else if (forceFields != null && forceFields.array.isNotEmpty()) {
    forceFields.array = forceFields.array.toMutableList().apply { removeAt(index.coerceIn(0, forceFields.array.lastIndex)) }.toTypedArray()
    updateParam(id, access, forceFields)
  }
  else {
    negativeFeedback("Couldn't remove ForceField. There are no ForceFields in the Emitter.", context)
    return
  }

  val calcIndex = if (index == -1) { forceFields.array.size } else { index.coerceIn(0, forceFields.array.size) }

  successText(access, "--ForceField at $calcIndex", id, context)
}

fun lerpValIntArg(access: KProperty1<EmitterParams, ParamClasses.LerpValInt>,
               configArg: LiteralArgumentBuilder<ServerCommandSource>
              ) : LiteralArgumentBuilder<ServerCommandSource>
{
  return configArg
    .then(CommandManager.literal("LerpInt")
      .then(CommandManager.argument("Min", IntegerArgumentType.integer())
        .then(CommandManager.argument("Max", IntegerArgumentType.integer())
          .then(curveListArg("Curve")
            .executes { context ->
              val id = StringArgumentType.getString(context, "Emitter ID")
              val min = IntegerArgumentType.getInteger(context, "Min")
              val max = IntegerArgumentType.getInteger(context, "Max")
              val curveName = StringArgumentType.getString(context, "Curve")

              val curve = stringToCurve(curveName)

              updateParam(id, access, ParamClasses.LerpValInt.LerpInt(min, max, curve!!))

              successText(access, "LerpInt", id, context)
              Command.SINGLE_SUCCESS
            }
          )
        )
      )
    )
    .then(CommandManager.literal("Constant")
      .then(CommandManager.argument("Value", IntegerArgumentType.integer())
        .executes { context ->
          val id = StringArgumentType.getString(context, "Emitter ID")
          val value = IntegerArgumentType.getInteger(context, "Value")

          updateParam(id, access, ParamClasses.LerpValInt.Constant(value))

          successText(access, "Constant", id, context)
          Command.SINGLE_SUCCESS
        }
      )
    )
    .then(CommandManager.literal("Null")
      .executes { context ->
        val id = StringArgumentType.getString(context, "Emitter ID")

        updateParam(id, access, ParamClasses.LerpValInt.Null)

        successText(access, "Null", id, context)
        Command.SINGLE_SUCCESS
      }
    )
}