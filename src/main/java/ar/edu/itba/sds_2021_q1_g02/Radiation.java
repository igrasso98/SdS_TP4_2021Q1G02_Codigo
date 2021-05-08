package ar.edu.itba.sds_2021_q1_g02;

import ar.edu.itba.sds_2021_q1_g02.models.*;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class Radiation extends Simulation {
    private final Particle[][] particlesMatrix;
    private final Collection<Particle> particles;
    private final Particle impactParticle;
    private final IntegrationAlgorithm integrationAlgorithm;
    private final double dt;

    public Radiation(Particle[][] particlesMatrix, Particle impactParticle, IntegrationAlgorithm integrationAlgorithm, double dt) {
        this.particlesMatrix = particlesMatrix;
        this.impactParticle = impactParticle;
        this.integrationAlgorithm = integrationAlgorithm;
        this.dt = dt;

        this.particles = new ArrayList<>(Constants.N_PARTICLES_TOTAL);
        for (Particle[] row : this.particlesMatrix) {
            this.particles.addAll(Arrays.asList(row));
        }
        this.particles.add(impactParticle);
    }

    @Override
    public void simulate() {
        this.serializeSystem(this.particles, this.integrationAlgorithm);
        Step step = this.calculateFirstStep();
        this.serialize(this.particles, step);

        while (step.getAbsoluteTime() < 5) {
            step = this.simulateStep(step);
            if (step.getAbsoluteTime() >= 5)
                step.setLastStep(true);

            this.serialize(this.particles, step);
        }
    }

    private Step simulateStep(Step previousStep) {
        Step newStep = new Step(
                this.particles.stream().collect(Collectors.toMap(particle -> particle, Particle::getPosition)),
                this.particles.stream().collect(Collectors.toMap(particle -> particle, Particle::getVelocity)),
                this.dt,
                previousStep.getAbsoluteTime() + this.dt,
                previousStep.getStep() + 1,
                this.integrationAlgorithm
        );

        for (Particle particle : this.particles) {
            Pair<Position, Velocity> newVelocityPositions = this.integrationAlgorithm.perform(particle, previousStep);

            particle.setPosition(newVelocityPositions.getKey());
            particle.setVelocity(newVelocityPositions.getValue());
        }

        return newStep;
    }

    private Step calculateFirstStep() {
        return new Step(
                this.particles.stream().collect(Collectors.toMap(particle -> particle, Particle::getPosition)),
                this.particles.stream().collect(Collectors.toMap(particle -> particle, Particle::getVelocity)),
                this.dt,
                0,
                0,
                this.integrationAlgorithm
        );
    }
}
