package com.bytedice.bde_particles.particles

import org.joml.Vector3f

data class ForceField (
  val name:  String             = "DEFAULT",
  val pos:   Vector3f           = Vector3f(0.0f, 5.0f, 0.0f),
  val shape: ForceFieldShape = ForceFieldShape.Sphere()
)