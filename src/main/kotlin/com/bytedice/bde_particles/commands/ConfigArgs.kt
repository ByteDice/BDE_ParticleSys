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
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.joml.Vector2f
import org.joml.Vector3f
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties
import net.minecraft.server.command.GiveCommand


typealias CommandArgumentBuilder = KFunction<ArgumentBuilder<ServerCommandSource, *>>


// TODO: doesn't apply shape & duration. Error applying shape other than circle

fun listConfigArgs(configArgs: Array<String>,
                   rootArg: RequiredArgumentBuilder<ServerCommandSource, *>,
                   registryAccess: CommandRegistryAccess
                  ) : RequiredArgumentBuilder<ServerCommandSource, *>
{
  configArgs.forEach { arg ->
    var configArg = CommandManager.literal(arg)

    val argAccess = parseArgNameToAccess(arg)!!
    val func = parseArgTypeToFunc(argAccess.returnType)

    if (func != null) {
      if (func.parameters.lastIndex == 0) {
        configArg.then(func.call(argAccess))
      }
      else if (func.parameters.lastIndex == 1) {
        configArg = func.call(argAccess, configArg) as LiteralArgumentBuilder<ServerCommandSource>?
      }
      else {
        configArg = func.call(argAccess, configArg, registryAccess) as LiteralArgumentBuilder<ServerCommandSource>?
      }
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

// TODO: add forceField & implement rest of args
fun parseArgTypeToFunc(type: KType) : CommandArgumentBuilder? {
  return when (type.classifier) {
    Int::class                           -> ::intArg
    Float::class                         -> ::floatArg
    String::class                        -> ::stringArg
    ParamClasses.Duration::class         -> ::durArg
    Vector3f::class                      -> ::vec3fArg
    ParamClasses.PairInt::class          -> ::pairIntArg
    SpawningShape::class                 -> ::shapeArg
    ParamClasses.PairVec3f::class        -> ::pairVec3fArg
    ParamClasses.PairFloat::class        -> ::pairFloatArg
    ParamClasses.TransformWithVel::class -> ::transformWithVelArg
    ParamClasses.StringCurve::class      -> ::stringCurveArg
    ParamClasses.LerpVal::class          -> null
    else                                 -> null
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

          updateParam(id, access, Vector3f(valueX, valueY, valueX))

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
    }
    "Rect"   -> {
      val onEdge = BoolArgumentType.getBool(context, "Spawn On Edge")
      val x = FloatArgumentType.getFloat(context, "X")
      val y = FloatArgumentType.getFloat(context, "Y")
      updateParam(id, access, SpawningShape.Rect(Vector2f(x, y), onEdge))
    }
    "Cube"   -> {
      val onEdge = BoolArgumentType.getBool(context, "Spawn On Edge")
      val x = FloatArgumentType.getFloat(context, "X")
      val y = FloatArgumentType.getFloat(context, "Y")
      val z = FloatArgumentType.getFloat(context, "Z")
      updateParam(id, access, SpawningShape.Cube(Vector3f(x, y, z), onEdge))
    }
    "Sphere" -> {
      val radius = FloatArgumentType.getFloat(context, "Radius")
      val onEdge = BoolArgumentType.getBool(context, "Spawn On Edge")
      updateParam(id, access, SpawningShape.Sphere(radius, onEdge))
    }
    "Point"  -> {
      updateParam(id, access, SpawningShape.Point)
    }
  }

  successText(access, access::class.simpleName ?: "UNKNOWN", id, context)
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

fun pairFloatArg(access: KProperty1<EmitterParams, ParamClasses.PairInt>) : RequiredArgumentBuilder<ServerCommandSource, Float> {
  return CommandManager.argument("Min Float", FloatArgumentType.floatArg())
    .then(CommandManager.argument("Max Float", FloatArgumentType.floatArg())
      .executes { context ->
        val id = StringArgumentType.getString(context, "Emitter ID")
        val valueX = FloatArgumentType.getFloat(context, "Min Int")
        val valueY = FloatArgumentType.getFloat(context, "Max Int")

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
            negativeFeedback("Failed to update param \"modelCurve.curve\"! The curve is most likely invalid!", context)
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
  val item = ItemStackArgumentType.getItemStackArgument(context, "Item").item.name.string

  val modelCurve = getEmitterDataById(id)?.modelCurve

  if (index == -1 && modelCurve != null) {
    modelCurve.array.toMutableList().apply { add(item) }.toTypedArray()
    updateParam(id, access, modelCurve)
  }
  else if (modelCurve != null) {
    modelCurve.array.toMutableList().apply { add(index.coerceIn(0, modelCurve.array.lastIndex), item) }.toTypedArray()
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

  if (index == -1 && modelCurve != null) {
    modelCurve.array.toMutableList().apply { removeLast() }.toTypedArray()
    updateParam(id, access, modelCurve)
  }
  else if (modelCurve != null) {
    modelCurve.array.toMutableList().apply { removeAt(index.coerceIn(0, modelCurve.array.lastIndex)) }.toTypedArray()
    updateParam(id, access, modelCurve)
  }

  successText(access, "--item at $index", id, context)
}