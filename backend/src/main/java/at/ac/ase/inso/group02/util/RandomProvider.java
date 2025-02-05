package at.ac.ase.inso.group02.util;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.profile.UnlessBuildProfile;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

import java.util.Random;

@Dependent
public class RandomProvider {

    @Produces
    @UnlessBuildProfile("test") // this will be enabled for both prod and dev build time profiles
    public Random seededRandom() {
        return new Random(1);
    }

    @Produces
    @DefaultBean
    public Random noopTracer() {
        return new Random();
    }
}
