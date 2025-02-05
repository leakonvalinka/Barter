package at.ac.ase.inso.group02.skills;

import at.ac.ase.inso.group02.skills.dto.CreateSkillDTO;
import at.ac.ase.inso.group02.skills.dto.SkillDTO;
import at.ac.ase.inso.group02.views.Views;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestPath;

/**
 * generic REST Skill controller for actions applicable to specific types of skills
 * (offers or demands, but not both simultaneously)
 *
 * @param <D> the expected return-DTO type of the methods (e.g. SkillOfferDTO for skill-offers)
 *            (note: the "type" field is never included in the results, since it is implicit from the generic type)
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface GenericSkillSubtypeController<D extends SkillDTO, C extends CreateSkillDTO> extends GenericSkillController<D> {

    /**
     * creates a new skill and links it to the currently authenticated user
     *
     * @param createSkillDTO data of the skill
     * @return data representing the created skill
     */
    @POST
    @ResponseStatus(201)
    @RolesAllowed({"USER", "ADMIN"})
    @JsonView(Views.Brief.class)
    D createSkill(@Valid C createSkillDTO);

    /**
     * update an existing skill
     *
     * @param id             id of the skill to update
     * @param updateSkillDTO (full) data of the skill to update
     * @return data representing the updated skill
     */
    @PUT
    @ResponseStatus(200)
    @RolesAllowed({"USER", "ADMIN"})
    @Path("/{id}")
    @JsonView(Views.Brief.class)
    D updateSkill(@RestPath Long id, @Valid C updateSkillDTO);
}
