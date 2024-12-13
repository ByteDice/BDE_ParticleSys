package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.particles.EmitterParams
import com.bytedice.bde_particles.particles.ParamClasses
import com.bytedice.bde_particles.particles.SpawningShape
import com.bytedice.bde_particles.particles.updateParam
import com.bytedice.bde_particles.positiveFeedback
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import org.joml.Vector2f
import org.joml.Vector3f
import java.util.concurrent.CompletableFuture
import kotlin.reflect.*
import kotlin.reflect.full.memberProperties


typealias CommandArgumentBuilder = KFunction<ArgumentBuilder<ServerCommandSource, *>>


// TODO: doesn't apply shape & duration. Error applying shape other than circle

fun listConfigArgs(configArgs: Array<String>, rootArg: RequiredArgumentBuilder<ServerCommandSource, *>) : RequiredArgumentBuilder<ServerCommandSource, *> {
  configArgs.forEach { arg ->
    var configArg = CommandManager.literal(arg)

    val argAccess = parseArgNameToAccess(arg)!!
    val func = parseArgTypeToFunc(argAccess.returnType)

    if (func != null) {
      if (func.parameters.lastIndex == 0) { configArg.then(func.call(argAccess)) }
      else { configArg = func.call(argAccess, configArg) as LiteralArgumentBuilder<ServerCommandSource>? }
    }

    // Add the configArg to the root command builder
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


fun parseArgTypeToFunc(type: KType) : CommandArgumentBuilder? {
  when (type.classifier) {
    Int::class                           -> return ::intArg
    Float::class                         -> return ::floatArg
    String::class                        -> return ::stringArg
    ParamClasses.Duration::class         -> return ::durArg
    Vector3f::class                      -> return ::vec3fArg
    ParamClasses.PairInt::class          -> return ::pairIntArg
    SpawningShape::class                 -> return ::shapeArg
    ParamClasses.PairVec3f::class        -> println("Type is PairVec3f")
    ParamClasses.PairFloat::class        -> println("Type is PairFloat")
    ParamClasses.TransformWithVel::class -> println("Type is TransformWithVel")
    Array::class                         -> println("Type is Array")
    ParamClasses.LerpVal::class          -> println("Type is LerpVal")
    Pair::class                          -> println("Type is Pair")
    else                                 -> return null
  }

  return null
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
        .executes { context -> durArgExec(access, context); Command.SINGLE_SUCCESS }
      )
    )
    .then(CommandManager.literal("MultiBurst")
      .then(CommandManager.argument("loopDur", IntegerArgumentType.integer())
        .then(CommandManager.argument("loopDelay", IntegerArgumentType.integer())
          .then(CommandManager.argument("loopCount", IntegerArgumentType.integer())
            .executes { context -> durArgExec(access, context); Command.SINGLE_SUCCESS }
          )
        )
      )
    )
    .then(CommandManager.literal("InfiniteLoop")
      .then(CommandManager.argument("loopDur", IntegerArgumentType.integer())
        .then(CommandManager.argument("loopDelay", IntegerArgumentType.integer())
          .executes { context -> durArgExec(access, context); Command.SINGLE_SUCCESS }
        )
      )
    )
}

fun durArgExec(access: KProperty1<EmitterParams, ParamClasses.Duration>, context: CommandContext<ServerCommandSource>) {
  val id = StringArgumentType.getString(context, "Emitter ID")
  val loopDur = IntegerArgumentType.getInteger(context, "loopDur")

  val eParams = EmitterParams.DEFAULT

  when (access.get(eParams)) {
    is ParamClasses.Duration.SingleBurst  -> {
      updateParam(id, access, ParamClasses.Duration.SingleBurst(loopDur))
    }
    is ParamClasses.Duration.MultiBurst   -> {
      val loopDelay = IntegerArgumentType.getInteger(context, "loopDelay")
      val loopCount = IntegerArgumentType.getInteger(context, "loopCount")
      updateParam(id, access, ParamClasses.Duration.MultiBurst(loopDur, loopDelay, loopCount))
    }
    is ParamClasses.Duration.InfiniteLoop -> {
      val loopDelay = IntegerArgumentType.getInteger(context, "loopDelay")
      updateParam(id, access, ParamClasses.Duration.InfiniteLoop(loopDur, loopDelay))
    }
  }

  successText(access, access.get(eParams)::class.simpleName ?: "UNKNOWN", id, context)
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
  return CommandManager.argument("First Int", IntegerArgumentType.integer())
    .then(CommandManager.argument("Second Int", IntegerArgumentType.integer())
      .executes { context ->
        val id = StringArgumentType.getString(context, "Emitter ID")
        val valueX = IntegerArgumentType.getInteger(context, "First Int")
        val valueY = IntegerArgumentType.getInteger(context, "Second Int")

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
          .executes { context -> shapeArgExec(access, context); Command.SINGLE_SUCCESS }
        )
      )
    )
    .then(CommandManager.literal("Rect")
      .then(CommandManager.argument("X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Spawn On Edge", BoolArgumentType.bool())
            .executes { context -> shapeArgExec(access, context); Command.SINGLE_SUCCESS }
          )
        )
      )
    )
    .then(CommandManager.literal("Cube")
      .then(CommandManager.argument("X", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Y", FloatArgumentType.floatArg())
          .then(CommandManager.argument("Z", FloatArgumentType.floatArg())
            .then(CommandManager.argument("Spawn On Edge", BoolArgumentType.bool())
              .executes { context -> shapeArgExec(access, context); Command.SINGLE_SUCCESS }
            )
          )
        )
      )
    )
    .then(CommandManager.literal("Sphere")
      .then(CommandManager.argument("Radius", FloatArgumentType.floatArg())
        .then(CommandManager.argument("Spawn On Edge", BoolArgumentType.bool())
          .executes { context -> shapeArgExec(access, context); Command.SINGLE_SUCCESS }
        )
      )
    )
    .then(CommandManager.literal("Point")
      .executes { context -> shapeArgExec(access, context); Command.SINGLE_SUCCESS }
    )
}

fun shapeArgExec(access: KProperty1<EmitterParams, SpawningShape>, context: CommandContext<ServerCommandSource>) {
  val id = StringArgumentType.getString(context, "Emitter ID")

  val eParams = EmitterParams.DEFAULT

  println(access.get(eParams)::class)

  when (access.get(eParams)) {
    is SpawningShape.Circle -> {
      val radius = FloatArgumentType.getFloat(context, "Radius")
      val onEdge = BoolArgumentType.getBool(context, "Spawn On Edge")
      updateParam(id, access, SpawningShape.Circle(radius, onEdge))
    }
    is SpawningShape.Rect   -> {
      val onEdge = BoolArgumentType.getBool(context, "Spawn On Edge")
      val x = FloatArgumentType.getFloat(context, "X")
      val y = FloatArgumentType.getFloat(context, "Y")
      updateParam(id, access, SpawningShape.Rect(Vector2f(x, y), onEdge))
    }
    is SpawningShape.Cube   -> {
      val onEdge = BoolArgumentType.getBool(context, "Spawn On Edge")
      val x = FloatArgumentType.getFloat(context, "X")
      val y = FloatArgumentType.getFloat(context, "Y")
      val z = FloatArgumentType.getFloat(context, "Z")
      updateParam(id, access, SpawningShape.Cube(Vector3f(x, y, z), onEdge))
    }
    is SpawningShape.Sphere -> {
      val radius = FloatArgumentType.getFloat(context, "Radius")
      val onEdge = BoolArgumentType.getBool(context, "Spawn On Edge")
      updateParam(id, access, SpawningShape.Sphere(radius, onEdge))
      println("$id, ${access.name}, $radius, $onEdge")
    }
    is SpawningShape.Point  -> {
      updateParam(id, access, SpawningShape.Point)
    }
  }

  successText(access, access.get(eParams)::class.simpleName ?: "UNKNOWN", id, context)
}