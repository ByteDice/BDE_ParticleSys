package com.bytedice.bde_particles.particleIdRegister

import org.joml.Vector3f

/**
 * ParticleParams defines the parameters for particle behavior, including shape, size, velocity, and other physics properties.
 *
 * @param shape The shape of the particle's spawning area, `null` is a single point.
 * @param blockCurve An array of block names that gets cycled through during the particle's lifetime. Empty defaults to "minecraft:air".
 * @param rotRandom The random ***initial*** rotation range for the particle's rotation in X, Y, and Z axes.
 * @param rotVelRandom The random velocity for particle rotation in the X, Y, and Z axes.
 * @param rotVelCurve A curve which the rotation velocity gets multiplied by during the particle's lifetime.
 * @param sizeRandom The random ***initial*** size range for the particle. If `uniformSize` is true, this will be uniform (cubic).
 * @param uniformSize If true, particles will have a uniform (cubic) size. Otherwise, their sizes on different axis can vary.
 * @param sizeCurve A curve which the size gets multiplied by during the particle's lifetime.
 * @param velRandom The random ***initial*** velocity range for the particle in the X, Y, and Z axes.
 * @param forceFields An array of force fields applied to the particle.
 * @param gravity A constant force applied to the particle every tick. It's often used as gravity.
 * @param drag A drag force that affects the speed overtime. The formula is (velocity * (1.0 - drag))
 * @param minVel If particles travel slower than this their speed is automatically 0. Values less than or equal to 0 mean the particle can travel at any speed.
 * @param lifeTime A range for the particle's lifetime in ticks. A value below 1 will result in the particle not spawning.
 */
data class ParticleParams (
  var shape:        SpawningShape?           = SpawningShape.Circle(3.0f),
  var blockCurve:   Array<String>            = arrayOf("minecraft:shroomlight", "minecraft:orange_concrete", "minecraft:orange_stained_glass", "minecraft:gray_stained_glass", "minecraft:light_gray_stained_glass"),
  var rotRandom:    Pair<Vector3f, Vector3f> = Pair(Vector3f(0.0f, 0.0f, 0.0f), Vector3f(360.0f, 360.0f, 360.0f)),
  var rotVelRandom: Pair<Vector3f, Vector3f> = Pair(Vector3f(-0.2f, -0.2f, -0.2f), Vector3f(0.2f, 0.2f, 0.2f)),
  var rotVelCurve:  Array<Vector3f>          = arrayOf(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, 0.0f, 0.0f)),
  var sizeRandom:   Pair<Vector3f, Vector3f> = Pair(Vector3f(0.5f, 0.5f, 0.5f), Vector3f(1.0f, 1.0f, 1.0f)),
  var uniformSize:  Boolean                  = true,
  var sizeCurve:    Array<Vector3f>          = arrayOf(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.5f, 0.5f, 0.5f)),
  var velRandom:    Pair<Vector3f, Vector3f> = Pair(Vector3f(-0.1f, 0.4f, -0.1f), Vector3f(0.1f, 0.8f, 0.1f)),
  var forceFields:  Array<ForceField>        = arrayOf(ForceField()), // emptyArray(),
  var gravity:      Vector3f                 = Vector3f(0.0f, -0.01f, 0.0f),
  var drag:         Float                    = 0.075f,
  var minVel:       Float                    = 0.0f,
  var lifeTime:     Pair<Int, Int>           = Pair(15, 45)
) {
  companion object Presets {
    val DEFAULT = ParticleParams()
    val FIRE_GEYSER = ParticleParams(
      null,
      arrayOf("minecraft:shroomlight", "minecraft:orange_concrete", "minecraft:orange_stained_glass", "minecraft:gray_stained_glass", "minecraft:light_gray_stained_glass"),
      Pair(Vector3f(0.0f, 0.0f, 0.0f), Vector3f(360.0f, 360.0f, 360.0f)),
      Pair(Vector3f(-0.2f, -0.2f, -0.2f), Vector3f(0.2f, 0.2f, 0.2f)),
      arrayOf(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.0f, 0.0f, 0.0f)),
      Pair(Vector3f(0.5f, 0.5f, 0.5f), Vector3f(1.0f, 1.0f, 1.0f)),
      true,
      arrayOf(Vector3f(1.0f, 1.0f, 1.0f), Vector3f(0.5f, 0.5f, 0.5f)),
      Pair(Vector3f(-0.1f, 0.4f, -0.1f), Vector3f(0.1f, 0.8f, 0.1f)),
      emptyArray(),
      Vector3f(0.0f, -0.01f, 0.0f),
      0.075f,
      0.0f,
      Pair(15, 45)
    )
  }
}