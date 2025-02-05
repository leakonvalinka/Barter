package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.skills.dto.CreateSkillDemandDTO;
import at.ac.ase.inso.group02.skills.dto.SkillDemandDTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST controller for operations exclusive to skill-demands
 */
@Path("/skills/demand")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SkillDemandController extends GenericSkillSubtypeController<SkillDemandDTO, CreateSkillDemandDTO> {
}
