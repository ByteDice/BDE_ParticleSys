package com.bytedice.bde_particles.particle

data class ForceField (
  val x:        Float           = 0.0f,
  val y:        Float           = 0.0f,
  val z:        Float           = 0.0f,
  val minForce: Float           = 0.0f,
  val maxForce: Float           = 0.0f,
  val shape:    ForceFieldShape = ForceFieldShape.Sphere()
)

