package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.shaderpack.IdMap;
import net.coderbot.iris.texunits.TextureUnit;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

import java.util.Objects;

import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.ONCE;
import static net.coderbot.iris.gl.uniform.UniformUpdateFrequency.PER_FRAME;

public final class CommonUniforms {
	private static final MinecraftClient client = MinecraftClient.getInstance();

	private CommonUniforms() {
		// no construction allowed
	}

	public static void addCommonUniforms(ProgramBuilder builder, IdMap idMap) {
		CameraUniforms.addCameraUniforms(builder);
		ViewportUniforms.addViewportUniforms(builder);
		WorldTimeUniforms.addWorldTimeUniforms(builder);
		SystemTimeUniforms.addSystemTimeUniforms(builder);
		CelestialUniforms.addCelestialUniforms(builder);
		IdMapUniforms.addIdMapUniforms(builder, idMap);
		MatrixUniforms.addMatrixUniforms(builder);

		builder
			.uniform1i(ONCE, "texture", TextureUnit.TERRAIN::getSamplerId)
			.uniform1i(ONCE, "lightmap", TextureUnit.LIGHTMAP::getSamplerId)
			.uniform1b(PER_FRAME, "hideGUI", () -> client.options.hudHidden)
			.uniform1f(PER_FRAME, "eyeAltitude", () -> Objects.requireNonNull(client.getCameraEntity()).getY())
			.uniform1i(PER_FRAME, "isEyeInWater", CommonUniforms::isEyeInWater)
			.uniform1f(PER_FRAME, "blindness", CommonUniforms::getBlindness);
	}

	private static float getBlindness() {
		Entity cameraEntity = client.getCameraEntity();

		if (cameraEntity instanceof LivingEntity) {
			StatusEffectInstance blindness = ((LivingEntity) cameraEntity).getStatusEffect(StatusEffects.BLINDNESS);

			if (blindness != null) {
				// Guessing that this is what OF uses, based on how vanilla calculates the fog value in BackgroundRenderer
				// TODO: Add this to ShaderDoc
				return Math.min(1.0F, blindness.getDuration() / 20.0F);
			}
		}

		return 0.0F;
	}

	private static int isEyeInWater() {
		Entity cameraEntity = Objects.requireNonNull(client.getCameraEntity());

		if (cameraEntity.isSubmergedInWater()) {
			return 1;
		} else if (cameraEntity.isInLava()) {
			return 2;
		} else {
			return 0;
		}
	}
}
