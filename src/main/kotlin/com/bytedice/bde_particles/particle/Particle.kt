package com.bytedice.bde_particles.particle

import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import org.joml.Vector4f

class Particle(
  // during init
      count:    Int      = 20,
      spawnDur: Int      = 20,
      lifetime: Int      = 20,
      offset:   Vector3f = Vector3f(-0.5f, -0.5f, -0.5f),
      rotation: Vector3f = Vector3f(0.0f, 0.0f, 0.0f),
      //shape: Shape, // add later, start with single point

  // during ticking
    // transformation
      forceFields: Array<Vector4f>     = arrayOf(Vector4f(0.0f, 0.0f, 0.0f, 0.0f)), // x y z force
      rotVel:     Vector3f             = Vector3f(0.0f, 0.0f, 0.0f),
      sizeCurve:  Array<Vector3f>      = arrayOf(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, 0.0f, 0.0f)),
    // physics
      gravity: Vec3d = Vec3d(0.0, -9.0, 0.0),
      drag:    Float = 0.2f,
      minVel:  Float = 0.003f,
    // rendering
      blockCurve: Array<String> = arrayOf("minecraft:black_concrete", "minecraft:green_concrete")
  ) {
  fun init() {

  }
}