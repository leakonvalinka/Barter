package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.skills.dto.CreateSkillOfferDTO;
import at.ac.ase.inso.group02.skills.dto.SkillOfferDTO;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST controller for operations exclusive to skill-offers
 */
@Path("/skills/offer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SkillOfferController extends GenericSkillSubtypeController<SkillOfferDTO, CreateSkillOfferDTO> {
}
