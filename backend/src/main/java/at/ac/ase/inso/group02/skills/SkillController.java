package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST controller for operations on  all types of skills (offers and demands simultaneously)
 */
@Path("/skills")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SkillController extends GenericSkillController<SkillDTO> {

}
